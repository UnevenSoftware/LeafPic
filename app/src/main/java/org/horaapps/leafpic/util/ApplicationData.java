package org.horaapps.leafpic.util;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Data class for holding Application-related data.
 */
public class ApplicationData {

    /**
     * Holds the Application's package name specified in Manifest
     */
    public static String PACKAGE_NAME;

    public static void init(@NonNull Context context) {
        PACKAGE_NAME = context.getPackageName();
    }
}
