package com.horaapps.leafpic.utils;

import android.os.Build;

/**
 * Created by dnld on 04/06/16.
 */

public class SystemUtil {

    /**
     * Get information if Android version is Kitkat (4.4).
     *
     * @return true if Kitkat.
     */
    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
    }

    /**
     * Get information if Android version is Lollipop (5.0) or higher.
     *
     * @return true if Lollipop or higher.
     */
    public static boolean isAndroid5() {
        return isAtLeastVersion(Build.VERSION_CODES.LOLLIPOP);
    }

    /**
     * Check if Android version is at least the given version.
     *
     * @param version The version
     * @return true if Android version is at least the given version
     */
    private static boolean isAtLeastVersion(final int version) {
        return Build.VERSION.SDK_INT >= version;
    }
}
