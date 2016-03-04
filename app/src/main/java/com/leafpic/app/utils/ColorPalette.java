package com.leafpic.app.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.leafpic.app.R;

/**
 * Created by dnld on 24/02/16.
 */
public class ColorPalette {

    public static int[] getAccentColors(Context context){
        return new int[]{
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.md_purple_500),
                ContextCompat.getColor(context, R.color.md_deep_purple_500),
                ContextCompat.getColor(context, R.color.md_blue_500),
                ContextCompat.getColor(context, R.color.md_light_blue_500),
                ContextCompat.getColor(context, R.color.md_cyan_500),
                ContextCompat.getColor(context, R.color.md_teal_500),
                ContextCompat.getColor(context, R.color.md_green_500),
                ContextCompat.getColor(context, R.color.md_yellow_500),
                ContextCompat.getColor(context, R.color.md_orange_500),
                ContextCompat.getColor(context, R.color.md_deep_orange_500),
                ContextCompat.getColor(context, R.color.md_blue_grey_500),
        };
    }

    public static int getDarkBackgroundColor(Context context){
        return ContextCompat.getColor(context, R.color.act_bg_dark);
    }

    public static int getLightBackgroundColor(Context context){
        return ContextCompat.getColor(context, R.color.act_bg_light);
    }

    public static int getDarkTextColor(Context context){
        return ContextCompat.getColor(context, R.color.cp_TextDark);
    }

    public static int getLightTextColor(Context context){
        return ContextCompat.getColor(context, R.color.cp_TextLight);
    }

    public static int[] getBaseColors(Context context) {
        return new int[]{
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.md_pink_500),
                ContextCompat.getColor(context, R.color.md_purple_500),
                ContextCompat.getColor(context, R.color.md_deep_purple_500),
                ContextCompat.getColor(context, R.color.md_indigo_500),
                ContextCompat.getColor(context, R.color.md_blue_500),
                ContextCompat.getColor(context, R.color.md_light_blue_500),
                ContextCompat.getColor(context, R.color.md_cyan_500),
                ContextCompat.getColor(context, R.color.md_teal_500),
                ContextCompat.getColor(context, R.color.md_green_500),
                ContextCompat.getColor(context, R.color.md_light_green_500),
                ContextCompat.getColor(context, R.color.md_lime_500),
                ContextCompat.getColor(context, R.color.md_yellow_500),
                ContextCompat.getColor(context, R.color.md_amber_500),
                ContextCompat.getColor(context, R.color.md_orange_500),
                ContextCompat.getColor(context, R.color.md_deep_orange_500),
                ContextCompat.getColor(context, R.color.md_brown_500),
                ContextCompat.getColor(context, R.color.md_blue_grey_500),
                ContextCompat.getColor(context, R.color.md_grey_500)
        };
    }

    public static int[] getColors(Context context, int c) {
        if (c == ContextCompat.getColor(context, R.color.md_red_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_red_200),
                    ContextCompat.getColor(context, R.color.md_red_300),
                    ContextCompat.getColor(context, R.color.md_red_400),
                    ContextCompat.getColor(context, R.color.md_red_500),
                    ContextCompat.getColor(context, R.color.md_red_600),
                    ContextCompat.getColor(context, R.color.md_red_700),
                    ContextCompat.getColor(context, R.color.md_red_800),
                    ContextCompat.getColor(context, R.color.md_red_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_pink_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_pink_200),
                    ContextCompat.getColor(context, R.color.md_pink_300),
                    ContextCompat.getColor(context, R.color.md_pink_400),
                    ContextCompat.getColor(context, R.color.md_pink_500),
                    ContextCompat.getColor(context, R.color.md_pink_600),
                    ContextCompat.getColor(context, R.color.md_pink_700),
                    ContextCompat.getColor(context, R.color.md_pink_800),
                    ContextCompat.getColor(context, R.color.md_pink_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_purple_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_purple_200),
                    ContextCompat.getColor(context, R.color.md_purple_300),
                    ContextCompat.getColor(context, R.color.md_purple_400),
                    ContextCompat.getColor(context, R.color.md_purple_500),
                    ContextCompat.getColor(context, R.color.md_purple_600),
                    ContextCompat.getColor(context, R.color.md_purple_700),
                    ContextCompat.getColor(context, R.color.md_purple_800),
                    ContextCompat.getColor(context, R.color.md_purple_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_deep_purple_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_deep_purple_200),
                    ContextCompat.getColor(context, R.color.md_deep_purple_300),
                    ContextCompat.getColor(context, R.color.md_deep_purple_400),
                    ContextCompat.getColor(context, R.color.md_deep_purple_500),
                    ContextCompat.getColor(context, R.color.md_deep_purple_600),
                    ContextCompat.getColor(context, R.color.md_deep_purple_700),
                    ContextCompat.getColor(context, R.color.md_deep_purple_800),
                    ContextCompat.getColor(context, R.color.md_deep_purple_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_indigo_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_indigo_200),
                    ContextCompat.getColor(context, R.color.md_indigo_300),
                    ContextCompat.getColor(context, R.color.md_indigo_400),
                    ContextCompat.getColor(context, R.color.md_indigo_500),
                    ContextCompat.getColor(context, R.color.md_indigo_600),
                    ContextCompat.getColor(context, R.color.md_indigo_700),
                    ContextCompat.getColor(context, R.color.md_indigo_800),
                    ContextCompat.getColor(context, R.color.md_indigo_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_blue_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_blue_200),
                    ContextCompat.getColor(context, R.color.md_blue_300),
                    ContextCompat.getColor(context, R.color.md_blue_400),
                    ContextCompat.getColor(context, R.color.md_blue_500),
                    ContextCompat.getColor(context, R.color.md_blue_600),
                    ContextCompat.getColor(context, R.color.md_blue_700),
                    ContextCompat.getColor(context, R.color.md_blue_800),
                    ContextCompat.getColor(context, R.color.md_blue_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_light_blue_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_light_blue_200),
                    ContextCompat.getColor(context, R.color.md_light_blue_300),
                    ContextCompat.getColor(context, R.color.md_light_blue_400),
                    ContextCompat.getColor(context, R.color.md_light_blue_500),
                    ContextCompat.getColor(context, R.color.md_light_blue_600),
                    ContextCompat.getColor(context, R.color.md_light_blue_700),
                    ContextCompat.getColor(context, R.color.md_light_blue_800),
                    ContextCompat.getColor(context, R.color.md_light_blue_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_cyan_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_cyan_200),
                    ContextCompat.getColor(context, R.color.md_cyan_300),
                    ContextCompat.getColor(context, R.color.md_cyan_400),
                    ContextCompat.getColor(context, R.color.md_cyan_500),
                    ContextCompat.getColor(context, R.color.md_cyan_600),
                    ContextCompat.getColor(context, R.color.md_cyan_700),
                    ContextCompat.getColor(context, R.color.md_cyan_800),
                    ContextCompat.getColor(context, R.color.md_cyan_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_teal_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_teal_200),
                    ContextCompat.getColor(context, R.color.md_teal_300),
                    ContextCompat.getColor(context, R.color.md_teal_400),
                    ContextCompat.getColor(context, R.color.md_teal_500),
                    ContextCompat.getColor(context, R.color.md_teal_600),
                    ContextCompat.getColor(context, R.color.md_teal_700),
                    ContextCompat.getColor(context, R.color.md_teal_800),
                    ContextCompat.getColor(context, R.color.md_teal_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_green_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_green_200),
                    ContextCompat.getColor(context, R.color.md_green_300),
                    ContextCompat.getColor(context, R.color.md_green_400),
                    ContextCompat.getColor(context, R.color.md_green_500),
                    ContextCompat.getColor(context, R.color.md_green_600),
                    ContextCompat.getColor(context, R.color.md_green_700),
                    ContextCompat.getColor(context, R.color.md_green_800),
                    ContextCompat.getColor(context, R.color.md_green_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_light_green_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_light_green_200),
                    ContextCompat.getColor(context, R.color.md_light_green_300),
                    ContextCompat.getColor(context, R.color.md_light_green_400),
                    ContextCompat.getColor(context, R.color.md_light_green_500),
                    ContextCompat.getColor(context, R.color.md_light_green_600),
                    ContextCompat.getColor(context, R.color.md_light_green_700),
                    ContextCompat.getColor(context, R.color.md_light_green_800),
                    ContextCompat.getColor(context, R.color.md_light_green_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_lime_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_lime_200),
                    ContextCompat.getColor(context, R.color.md_lime_300),
                    ContextCompat.getColor(context, R.color.md_lime_400),
                    ContextCompat.getColor(context, R.color.md_lime_500),
                    ContextCompat.getColor(context, R.color.md_lime_600),
                    ContextCompat.getColor(context, R.color.md_lime_700),
                    ContextCompat.getColor(context, R.color.md_lime_800),
                    ContextCompat.getColor(context, R.color.md_lime_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_yellow_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_yellow_400),
                    ContextCompat.getColor(context, R.color.md_yellow_500),
                    ContextCompat.getColor(context, R.color.md_yellow_600),
                    ContextCompat.getColor(context, R.color.md_yellow_700),
                    ContextCompat.getColor(context, R.color.md_yellow_800),
                    ContextCompat.getColor(context, R.color.md_yellow_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_amber_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_amber_200),
                    ContextCompat.getColor(context, R.color.md_amber_300),
                    ContextCompat.getColor(context, R.color.md_amber_400),
                    ContextCompat.getColor(context, R.color.md_amber_500),
                    ContextCompat.getColor(context, R.color.md_amber_600),
                    ContextCompat.getColor(context, R.color.md_amber_700),
                    ContextCompat.getColor(context, R.color.md_amber_800),
                    ContextCompat.getColor(context, R.color.md_amber_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_orange_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_orange_200),
                    ContextCompat.getColor(context, R.color.md_orange_300),
                    ContextCompat.getColor(context, R.color.md_orange_400),
                    ContextCompat.getColor(context, R.color.md_orange_500),
                    ContextCompat.getColor(context, R.color.md_orange_600),
                    ContextCompat.getColor(context, R.color.md_orange_700),
                    ContextCompat.getColor(context, R.color.md_orange_800),
                    ContextCompat.getColor(context, R.color.md_orange_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_deep_orange_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_deep_orange_200),
                    ContextCompat.getColor(context, R.color.md_deep_orange_300),
                    ContextCompat.getColor(context, R.color.md_deep_orange_400),
                    ContextCompat.getColor(context, R.color.md_deep_orange_500),
                    ContextCompat.getColor(context, R.color.md_deep_orange_600),
                    ContextCompat.getColor(context, R.color.md_deep_orange_700),
                    ContextCompat.getColor(context, R.color.md_deep_orange_800),
                    ContextCompat.getColor(context, R.color.md_deep_orange_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_brown_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_brown_200),
                    ContextCompat.getColor(context, R.color.md_brown_300),
                    ContextCompat.getColor(context, R.color.md_brown_400),
                    ContextCompat.getColor(context, R.color.md_brown_500),
                    ContextCompat.getColor(context, R.color.md_brown_600),
                    ContextCompat.getColor(context, R.color.md_brown_700),
                    ContextCompat.getColor(context, R.color.md_brown_800),
                    ContextCompat.getColor(context, R.color.md_brown_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_grey_500)) {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_grey_400),
                    ContextCompat.getColor(context, R.color.md_grey_500),
                    ContextCompat.getColor(context, R.color.md_grey_600),
                    ContextCompat.getColor(context, R.color.md_grey_700),
                    ContextCompat.getColor(context, R.color.md_grey_800),
                    ContextCompat.getColor(context, R.color.md_grey_900),
                    Color.parseColor("#000000")
            };
        } else {
            return new int[]{
                    ContextCompat.getColor(context, R.color.md_blue_grey_300),
                    ContextCompat.getColor(context, R.color.md_blue_grey_400),
                    ContextCompat.getColor(context, R.color.md_blue_grey_500),
                    ContextCompat.getColor(context, R.color.md_blue_grey_600),
                    ContextCompat.getColor(context, R.color.md_blue_grey_700),
                    ContextCompat.getColor(context, R.color.md_blue_grey_800),
                    ContextCompat.getColor(context, R.color.md_blue_grey_900)
            };
        }
    }
}
