package com.kuang.screenshot;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.LocalSocket;
import android.os.Looper;
import android.util.Base64;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class PhoneClientOkHttp {
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String MOVE = "MOVE";

    private static final String MENU = "MENU";
    private static final String HOME = "HOME";
    private static final String BACK = "BACK";

    private static float scale = 0.5f;

    public static void main(String[] args) throws Exception {
//        System.out.println(">>>>>> phone client start");
//
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                e.printStackTrace(System.out);
//            }
//        });
//
//        System.out.println(">>>>>> phone client init");
//
//        LocalServerSocket mLocalServerSocket = new LocalServerSocket("wisescreenshot");
//        HandleInputEvent.init();
//
//        while (true) {
//            System.out.println(">>>>>> phone client listen");
//            try {
//                LocalSocket localSocket = mLocalServerSocket.accept();
//                System.out.println(">>>>>> phone client accepted");
////                read(localSocket);
//                write(localSocket);
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//        }

//        AsyncHttpServer httpServer = new AsyncHttpServer() {
//            protected boolean onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
//                return super.onRequest(request, response);
//            }
//        };
//
        Looper.prepare();
        Looper.myLooper();
        System.out.println("phone client Andcast Main Entry!");
//
//        AsyncServer server = new AsyncServer();
//        httpServer.get("/screenshot.jpg", new AnonymousClass5());
//        httpServer.listen(server, 53516);
        initOkHttp();
//
        Looper.loop();

    }

    static class AnonymousClass5 implements HttpServerRequestCallback {

        public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
            try {
                System.out.println("phone client onRequest start");
                Bitmap bitmap = ScreenUtils.screenshot();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bout);
                bout.flush();
                response.send("image/jpeg", bout.toByteArray());
                System.out.println("phone client request");
                return;
            } catch (Exception e) {
                System.out.println("phone client onRequest error " + e.getMessage());
                response.code(500);
                response.send(e.toString());
                return;
            }
        }
    }

    private static void initOkHttp() throws Exception {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        OkHttpClient client = new OkHttpClient();
        System.out.println("phone client OKHTTP init");

        while (true) {
            try {
                Bitmap bitmap = ScreenUtils.screenshot();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bout);
                bout.flush();

                byte[] bitmapBytes = bout.toByteArray();
                String result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);


                //构建FormBody，传入要提交的参数
                FormBody formBody = new FormBody
                        .Builder()
                        .add("image", result)
                        .build();
                final Request request = new Request.Builder()
                        .url("http://192.168.50.209:8080/image")
                        .post(formBody)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        System.out.println(response.body().string());
                    }
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
//            }
//        }).start();
    }


    /**
     * 从电脑端读取数据，进行手机的操作
     *
     * @param localSocket
     */
    private static void read(final LocalSocket localSocket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
                    while (true) {
                        try {
                            String line = reader.readLine();
                            if (line != null) {
                                handleKeyEvents(line);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void handleKeyEvents(String line) throws InvocationTargetException, IllegalAccessException {
        if (line.startsWith(DOWN)) {
            handleDown(line.substring(DOWN.length()));
        } else if (line.startsWith(MOVE)) {
            handleMove(line.substring(MOVE.length()));
        } else if (line.startsWith(UP)) {
            handleUp(line.substring(UP.length()));
        } else if (line.startsWith(MENU)) {
            HandleInputEvent.pressMenu();
        } else if (line.startsWith(HOME)) {
            HandleInputEvent.pressHome();
        } else if (line.startsWith(BACK)) {
            HandleInputEvent.pressBack();
        }
    }

    private static void handleUp(String line) {
        Point point = getXY(line);
        if (point != null) {
            try {
                HandleInputEvent.touchUp(point.x, point.y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleMove(String line) {
        Point point = getXY(line);
        if (point != null) {
            try {
                HandleInputEvent.touchMove(point.x, point.y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleDown(String line) {
        Point point = getXY(line);
        if (point != null) {
            try {
                HandleInputEvent.touchDown(point.x, point.y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Point getXY(String input) {
        try {
            Point point = new Point();
            String[] s = input.split("#");
            point.x = Integer.parseInt(s[0]);
            point.y = Integer.parseInt(s[1]);
            System.out.println("phone client click x is " + point.x);
            System.out.println("phone client click y is " + point.y);
            return point;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将手机的屏幕传输给电脑端
     * <p>
     * 数据传输如下：
     * 手机宽 手机高 图像大小 图像字节数组
     * x     y      size    byte array
     *
     * @param localSocket
     */
    private static void write(final LocalSocket localSocket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(localSocket.getOutputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
                    while (true) {
                        // 获取手机图像并压缩
                        Bitmap bitmap = ScreenUtils.screenshot();
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (ScreenUtils.getDisplaySize().x * scale),
                                (int) (ScreenUtils.getDisplaySize().y * scale), true);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);

//                        int x = ScreenUtils.getDisplaySize().x;
//                        int y = ScreenUtils.getDisplaySize().y;
//
//                        // 输出手机屏幕的宽和高
//                        dataOutputStream.writeInt(x);
//                        dataOutputStream.writeInt(y);
//
//                        // 输出手机图像大小
//                        dataOutputStream.writeInt(byteArrayOutputStream.size());

                        // 输出图像
                        bufferedOutputStream.write(byteArrayOutputStream.toByteArray());
                        bufferedOutputStream.flush();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
