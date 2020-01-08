package com.wise.wisescreenshot;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.IDisplayManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Build;
import android.os.IBinder;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.Surface;
import android.view.SurfaceControl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class PhoneClient {
    private static final String DOWN = "DOWN";
    private static final String UP = "UP";
    private static final String MOVE = "MOVE";

    private static final String MENU = "MENU";
    private static final String HOME = "HOME";
    private static final String BACK = "BACK";

    private static final String KEY_UP = "KEY_UP";
    private static final String KEY_DOWN = "KEY_DOWN";
    private static final String KEY_LEFT = "KEY_LEFT";
    private static final String KEY_RIGHT = "KEY_RIGHT";
    private static final String KEY_ENTER = "KEY_ENTER";
    private static final String KEY_ESC = "KEY_ESC";

    private static float scale = 0.5f;
    private static int rotation = 0;
    private static LocalServerSocket mLocalServerSocket;

    public static void main(String[] args) {
        startUnixSocket();
    }

    private static void startUnixSocket() {
        System.out.println("start unix socket");
        try {
            startLocalServerSocket();
        } catch (Exception e) {
            System.out.println("start unix socket error");
            System.out.println(e.getMessage());
        }
    }

    private static void startLocalServerSocket() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("start local server socket");
        mLocalServerSocket = new LocalServerSocket("wisescreenshot");
        HandleInputEvent.init();

        while (true) {
            System.out.println("local server socket listening...");
            try {
                LocalSocket localSocket = mLocalServerSocket.accept();
                handleLocalSocket(localSocket);
            } catch (Exception e) {
                System.out.println("local server socket listening error");
                System.out.println(e.getMessage());
                try {
                    mLocalServerSocket = new LocalServerSocket("wisescreenshot");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private static void handleLocalSocket(LocalSocket localSocket) {
        readSocket(localSocket);
        writeSocket(localSocket);
    }

    private static void writeSocket(final LocalSocket localSocket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(localSocket.getOutputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
                    while (true) {
                        int x = getCurrentDisplaySize().x;
                        int y = getCurrentDisplaySize().y;

                        dataOutputStream.writeInt(x);
                        dataOutputStream.writeInt(y);

                        Bitmap bitmap = screenshot();
                        if (bitmap == null) {
                            return;
                        }
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);

                        dataOutputStream.writeInt(byteArrayOutputStream.size());

                        bufferedOutputStream.write(byteArrayOutputStream.toByteArray());
                        bufferedOutputStream.flush();
                    }
                } catch (Exception e) {
                    System.out.println("write socket " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void readSocket(final LocalSocket localSocket) {
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
        } else if (line.startsWith(BACK) || line.startsWith(KEY_ESC)) {
            HandleInputEvent.pressBack();
        } else if (line.startsWith(KEY_UP)) {
            HandleInputEvent.pressUp();
        } else if (line.startsWith(KEY_DOWN)) {
            HandleInputEvent.pressDown();
        } else if (line.startsWith(KEY_LEFT)) {
            HandleInputEvent.pressLeft();
        } else if (line.startsWith(KEY_RIGHT)) {
            HandleInputEvent.pressRight();
        } else if (line.startsWith(KEY_ENTER)) {
            HandleInputEvent.pressEnter();
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
            return point;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行图像截取
     *
     * @throws Exception
     */
    private static Bitmap screenshot() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            String surfaceClassName;
            Point size = getCurrentDisplaySize();

            size.x *= scale;
            size.y *= scale;

            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT <= 17) {
                surfaceClassName = "android.view.Surface";
                bitmap = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", Integer.TYPE, Integer.TYPE).invoke(null, Integer.valueOf(size.x), Integer.valueOf(size.y));
            } else if (Build.VERSION.SDK_INT < 28) {
                bitmap = SurfaceControl.screenshot(size.x, size.y);
            } else {
                bitmap = SurfaceControl.screenshot(new Rect(0, 0, size.x, size.y), size.x, size.y, 0);
            }
            if (rotation == 0) {
                return bitmap;
            }
            Matrix m = new Matrix();
            if (Surface.ROTATION_90 == rotation) {
                m.postRotate(-90.0f);
            } else if (Surface.ROTATION_180 == rotation) {
                m.postRotate(-180.0f);
            } else if (Surface.ROTATION_270 == rotation) {
                m.postRotate(-270.0f);
            }
            return Bitmap.createBitmap(bitmap, 0, 0, size.x, size.y, m, false);
        } catch (Exception e) {
            System.out.println("screen shot error");
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * 获取屏幕的高度和宽度
     */
    private static Point getCurrentDisplaySize() {
        try {
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            Point point = new Point();
            IWindowManager wm;
            if (Build.VERSION.SDK_INT >= 18) {
                wm = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, "window"));
                wm.getInitialDisplaySize(0, point);
                if (Build.VERSION.SDK_INT < 26) {
                    rotation = wm.getRotation();
                } else {
                    rotation = wm.getDefaultDisplayRotation();
                }
            } else if (Build.VERSION.SDK_INT == 17) {
                DisplayInfo di = IDisplayManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, "display")).getDisplayInfo(0);
                point.x = ((Integer) DisplayInfo.class.getDeclaredField("logicalWidth").get(di)).intValue();
                point.y = ((Integer) DisplayInfo.class.getDeclaredField("logicalHeight").get(di)).intValue();
                rotation = ((Integer) DisplayInfo.class.getDeclaredField("rotation").get(di)).intValue();
            } else {
                wm = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, "window"));
                wm.getRealDisplaySize(point);
                rotation = wm.getRotation();
            }
            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
                int temp = point.x;
                point.x = point.y;
                point.y = temp;
            }
            return point;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
