package com.leafpic.app;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jibo on 21/02/2016.
 */
public class ThemeActivity extends AppCompatActivity {
    private boolean mLastDarkTheme;
    private int mLastPrimaryColor;
    private int mLastAccentColor;
    private boolean mLastColoredNav;

    @ColorInt
    public static int shiftColor(@ColorInt int color, @FloatRange(from = 0.0f, to = 2.0f) float by) {
        if (by == 1f) {
            return color;
        }
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= by; // value component
        return Color.HSVToColor(hsv);
    }
    @ColorInt
    public static int shiftColorDown(@ColorInt int color) {
        return shiftColor(color, 0.9f);
    }

    protected int darkTheme() {
        return R.style.AppTheme_Dark;
    }

    protected int lightTheme() {
        return R.style.AppTheme;
    }

    public boolean isDarkTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PrefUtils.DARK_THEME, false);
    }

    public int primaryColor() {
        String key = PrefUtils.PRIMARY_COLOR_PREFIX;
        if (mLastDarkTheme) {
            key += "_dark";
        } else {
            key += "_light";
        }
        final int defaultColor = ContextCompat.getColor(this, mLastDarkTheme ?
                R.color.dark_theme_gray : R.color.material_indigo_500);
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(key, defaultColor);
    }

    protected void primaryColor(int newColor) {
        String key = PrefUtils.PRIMARY_COLOR_PREFIX;
        if (mLastDarkTheme) {
            key += "_dark";
        } else {
            key += "_light";
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(key, newColor).apply();
    }

    public int primaryColorDark() {
        return shiftColorDown(primaryColor());
    }

    public int accentColor() {
        String key = PrefUtils.ACCENT_COLOR_PREFIX;
        if (mLastDarkTheme) {
            key += "_dark";
        } else {
            key += "_light";
        }
        final int defaultColor = ContextCompat.getColor(this, R.color.material_pink_500);
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(key, defaultColor);
    }

    protected void accentColor(int newColor) {
        String key = PrefUtils.ACCENT_COLOR_PREFIX;
        if (mLastDarkTheme) {
            key += "_dark";
        } else {
            key += "_light";
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(key, newColor).apply();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLastDarkTheme = isDarkTheme();
        mLastPrimaryColor = primaryColor();
        mLastAccentColor = accentColor();
        mLastColoredNav = PrefUtils.isColoredNavBar(this);
        ColorStateList sl = ColorStateList.valueOf(mLastAccentColor);
        /*
        ThemeSingleton.get().positiveColor = sl;
        ThemeSingleton.get().neutralColor = sl;
        ThemeSingleton.get().negativeColor = sl;
        ThemeSingleton.get().widgetColor = mLastAccentColor;
        */
        setTheme(mLastDarkTheme ? darkTheme() : lightTheme());
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Sets color of entry in the system recents page
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                    primaryColor());
            setTaskDescription(td);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasColoredBars()) {
            final int dark = primaryColorDark();
            if (allowStatusBarColoring()) {
                getWindow().setStatusBarColor(dark);
            } else {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
            }
            if (mLastColoredNav) {
                getWindow().setNavigationBarColor(dark);
            }
        }
    }

    protected boolean allowStatusBarColoring() {
        return false;
    }

    protected boolean hasColoredBars() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean darkTheme = isDarkTheme();
        int primaryColor = primaryColor();
        int accentColor = accentColor();
        boolean coloredNav = PrefUtils.isColoredNavBar(this);
        if (darkTheme != mLastDarkTheme || primaryColor != mLastPrimaryColor ||
                accentColor != mLastAccentColor || coloredNav != mLastColoredNav) {
            recreate();
        }
    }
}
