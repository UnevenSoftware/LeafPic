package com.leafpic.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.bumptech.glide.load.engine.Resource;

/**
 * Created by dnld on 11/03/16.
 */
public class Measure {

    public static int pxToDp(int dp, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getScreenWidth(Context c){
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getAlbumsColums(Context c){
        int width = getScreenWidth(c);
        return Math.round(width/Costants.ALBUM_CARD_WIDTH);
    }

    public static int getPhotosColums(Context c){
        int width = getScreenWidth(c);
        return Math.round(width/Costants.PHOTO_CARD_WIDTH);
    }

    public static int getStatusBarHeight(Resources r) {
        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return r.getDimensionPixelSize(resourceId);

        return 0;
    }
}
