package android.view;

import android.graphics.Point;
import android.os.IBinder;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public interface IWindowManager {
    void getInitialDisplaySize(int i, Point displaySize);

    int getRotation();

    int getDefaultDisplayRotation();

    void getRealDisplaySize(Point displaySize);

    abstract class Stub {

        public static IWindowManager asInterface(IBinder invoke) {
            return null;
        }
    }
}
