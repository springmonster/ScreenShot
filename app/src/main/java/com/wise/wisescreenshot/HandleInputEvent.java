package com.wise.wisescreenshot;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kuanghaochuan on 2017/8/6.
 */

public class HandleInputEvent {
    private static InputManager im;
    private static Method injectInputEventMethod;
    private static long downTime;

    public static void init() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        im = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null);
        MotionEvent.class.getDeclaredMethod("obtain").setAccessible(true);
        injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", InputEvent.class, Integer.TYPE);
    }

    public static void pressLeft() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_LEFT, false);
    }

    public static void pressRight() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_RIGHT, false);
    }

    public static void pressUp() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_UP, false);
    }

    public static void pressDown() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_DPAD_DOWN, false);
    }

    public static void pressEnter() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_ENTER, false);
    }

    public static void pressMenu() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_MENU, false);
    }

    public static void pressHome() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_HOME, false);
    }

    public static void pressBack() throws InvocationTargetException, IllegalAccessException {
        sendKeyEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_BACK, false);
    }

    public static void touchDown(float clientX, float clientY) throws InvocationTargetException, IllegalAccessException {
        downTime = SystemClock.uptimeMillis();
        injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_DOWN, downTime, downTime, clientX, clientY, 1.0f);
    }

    public static void touchUp(float clientX, float clientY) throws InvocationTargetException, IllegalAccessException {
        injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_UP, downTime, SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    public static void touchMove(float clientX, float clientY) throws InvocationTargetException, IllegalAccessException {
        injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_MOVE, downTime, SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    private static void injectMotionEvent(InputManager im, Method injectInputEventMethod, int inputSource, int action, long downTime, long eventTime, float x, float y, float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, event, Integer.valueOf(0));
    }

    private static void injectKeyEvent(InputManager im, Method injectInputEventMethod, KeyEvent event) throws InvocationTargetException, IllegalAccessException {
        injectInputEventMethod.invoke(im, event, Integer.valueOf(0));
    }

    private static void sendKeyEvent(InputManager im, Method injectInputEventMethod, int inputSource, int keyCode, boolean shift) throws InvocationTargetException, IllegalAccessException {
        long now = SystemClock.uptimeMillis();
        int meta = shift ? 1 : 0;
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 0, keyCode, 0, meta, -1, 0, 0, inputSource));
        injectKeyEvent(im, injectInputEventMethod, new KeyEvent(now, now, 1, keyCode, 0, meta, -1, 0, 0, inputSource));
    }
}
