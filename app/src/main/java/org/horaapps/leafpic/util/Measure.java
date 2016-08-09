package org.horaapps.leafpic.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by dnld on 11/03/16.
 */
public class Measure {

    public static final String TAG = "Measure";

    public static int pxToDp(int px, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(px * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private static int getScreenWidth(Context c){
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private static float getScreenWidthDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return pxToDp(displayMetrics.widthPixels, context);
        //return displayMetrics.widthPixels / displayMetrics.density;
    }

    public static int getAlbumsColumns(Context context) {

        float width = getScreenWidthDp(context);



       /* if(width < 600)
            return 2;
        if (width < 840)
            return 3;*/


        int n = (int)( width / Constants.ALBUM_CARD_WIDTH);
        //StringUtils.showToast(c,width + " - " + n);
        //Log.wtf(TAG,width + " - " + n);
        if(n < 2) n = 2;
        else if (n > 8) n = 8;
        return n;
        //return n < 2 ? 2 : n;
    }

    public static int getPhotosColumns(Context context) {
        float width = getScreenWidthDp(context);
        //int n = Math.round(getDensity(c)/ Constants.PHOTO_CARD_WIDTH);
        int n = (int)( width / Constants.PHOTO_CARD_WIDTH);
        //StringUtils.showToast(c,width + " - " + n);
        //Log.wtf(TAG,width + " - " + n);
        if(n < 3) n = 3;
        else if (n > 8) n = 8;
        return n;
        //return n < 3 ? 3 : n;
    }

    public static int getDensity(Context c){
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

    private static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    public static int rotateBy(int current, int degrees){
        int rotation = current + degrees;
        if (rotation > 359) rotation -=360;
        if (rotation < 0) rotation +=360;
        return rotation;
    }
}
