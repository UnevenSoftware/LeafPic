package com.horaapps.leafpic.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by dnld on 11/03/16.
 */
public class Measure {

    public static final String TAG = "Measure";

    public static int pxToDp(int dp, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getScreenWidth(Context c){
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getAlbumsColums(Context c){
        //StringUtils.showToast(c, getRealScreenSize(c).x / Costants.ALBUM_CARD_WIDTH_test+"");
        int n = Math.round(getDensity(c) / Costants.ALBUM_CARD_WIDTH);
        return n < 2 ? 2 : n;
    }

    public static int getPhotosColums(Context c){
        int n = Math.round(getDensity(c)/ Costants.PHOTO_CARD_WIDTH);
        return n < 3 ? 3 : n;
    }

    public static int getDensity(Context c){
        //StringUtils.showToast(c,  (getScreenWidth(c) / c.getResources().getDisplayMetrics().densityDpi)+"");
        return Math.round((getScreenWidth(c) * c.getResources().getDisplayMetrics().density));
    }

    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return r.getDimensionPixelSize(resourceId);

        return 0;
    }

    public static int getNavBarHeight(Context ct){
        return getNavigationBarSize(ct).y;
    }

    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    public static int rotateBy(int current, int degres){
        int rotation = current + degres;
        if (rotation > 359) rotation -=360;
        if (rotation < 0) rotation +=360;
        return rotation;
    }
}
