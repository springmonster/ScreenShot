package android.hardware.display;

import android.os.IBinder;
import android.view.DisplayInfo;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public interface IDisplayManager {
    DisplayInfo getDisplayInfo(int i);

    abstract class Stub {
        public static IDisplayManager asInterface(IBinder invoke) {
            return null;
        }
    }
}
