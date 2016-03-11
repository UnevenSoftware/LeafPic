package com.leafpic.app.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by dnld on 11/03/16.
 */
public class Measure {

    public static int pxToDp(int dp, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
