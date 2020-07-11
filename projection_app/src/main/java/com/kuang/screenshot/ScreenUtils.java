package com.kuang.screenshot;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.ServiceManager;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.Surface;
import android.view.SurfaceControl;

public class ScreenUtils {

    private static final Point POINT = new Point();
    private static int rotation = 0;

    /**
     * 获取手机的宽和高，需要考虑旋转屏幕的问题
     *
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Point getDisplaySize() throws NoSuchFieldException, IllegalAccessException {
        if (VERSION.SDK_INT >= 18) {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            wm.getInitialDisplaySize(0, POINT);

            if (Build.VERSION.SDK_INT < 26) {
                rotation = wm.getRotation();
            } else {
                rotation = wm.getDefaultDisplayRotation();
            }
        } else if (VERSION.SDK_INT == 17) {
            DisplayInfo di =
                    IDisplayManager.Stub.asInterface(ServiceManager.getService("display")).getDisplayInfo(0);
            POINT.x = di.logicalWidth;
            POINT.y = di.logicalHeight;

            rotation = ((Integer) DisplayInfo.class.getDeclaredField("rotation").get(di)).intValue();
        } else {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            wm.getRealDisplaySize(POINT);

            rotation = wm.getRotation();
        }

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            int temp = POINT.x;
            POINT.x = POINT.y;
            POINT.y = temp;
        }

        return POINT;
    }

    /**
     * 如果没有旋转，直接返回图片
     * 否则返回旋转的图片
     *
     * @return
     * @throws Exception
     */
    public static Bitmap screenshot() throws Exception {
        Point size = ScreenUtils.getDisplaySize();

        if (VERSION.SDK_INT <= 17) {
            String surfaceClassName = "android.view.Surface";
            return (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", int.class, int.class)
                    .invoke(null, new Object[]{size.x, size.y});
        } else if (VERSION.SDK_INT < 28) {
            return SurfaceControl.screenshot(size.x, size.y);
        } else {
            return SurfaceControl.screenshot(new Rect(0, 0, size.x, size.y), size.x, size.y, rotation);
        }
    }
}