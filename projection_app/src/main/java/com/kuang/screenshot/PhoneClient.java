package com.kuang.screenshot;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

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
                        int x = ScreenUtils.getDisplaySize().x;
                        int y = ScreenUtils.getDisplaySize().y;

                        dataOutputStream.writeInt(x);
                        dataOutputStream.writeInt(y);

                        Bitmap bitmap = ScreenUtils.screenshot();
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
}
