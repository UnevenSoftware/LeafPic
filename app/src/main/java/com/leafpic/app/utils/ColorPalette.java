package com.leafpic.app.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.leafpic.app.R;

/**
 * Created by dnld on 24/02/16.
 */
public class ColorPalette {

    public static int[] getDominantColors(Context context){
        return new int[]{
                ContextCompat.getColor(context, R.color.accent_red),
                ContextCompat.getColor(context, R.color.accent_pink),
                ContextCompat.getColor(context, R.color.accent_deep_purple),
                ContextCompat.getColor(context, R.color.accent_indago),
                ContextCompat.getColor(context, R.color.accent_blue),
                ContextCompat.getColor(context, R.color.accent_cyan),
                ContextCompat.getColor(context, R.color.accent_teal),
                ContextCompat.getColor(context, R.color.accent_green),
                ContextCompat.getColor(context, R.color.accent_yellow),
                ContextCompat.getColor(context, R.color.accent_amber),
                ContextCompat.getColor(context, R.color.accent_orange),
                ContextCompat.getColor(context, R.color.accent_brown)
        };
    }

    public static int getDarkBackgroundColor(Context context){
        return ContextCompat.getColor(context, R.color.act_bg_dark);
    }

    public static int getLightBackgroundColor(Context context){
        return ContextCompat.getColor(context, R.color.act_bg_light);
    }
}
