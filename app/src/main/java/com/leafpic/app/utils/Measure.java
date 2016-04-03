package com.leafpic.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.bumptech.glide.load.engine.Resource;

/**
 * Created by dnld on 11/03/16.
 */
public class Measure {

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
        int n = Math.round(getDensity(c) / Costants.ALBUM_CARD_WIDTH);
        return n < 2 ? 2 : n;
    }

    public static int getPhotosColums(Context c){
        int n = Math.round(getDensity(c)/ Costants.PHOTO_CARD_WIDTH);;
        return n < 3 ? 3 : n;
    }

    public static int getDensity(Context c){
        return Math.round((getScreenWidth(c) / c.getResources().getDisplayMetrics().density));
    }

    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return r.getDimensionPixelSize(resourceId);

        return 0;
    }
}
