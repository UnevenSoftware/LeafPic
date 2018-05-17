package org.horaapps.leafpic.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;

/**
 * Utility class for accessing Android device-specific information
 */
public class DeviceUtils {

    /**
     * Returns the state of device being in Landscape orientation.
     */
    public static boolean isLandscape(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Returns the state of device being in Portrait orientation.
     */
    public static boolean isPortrait(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
