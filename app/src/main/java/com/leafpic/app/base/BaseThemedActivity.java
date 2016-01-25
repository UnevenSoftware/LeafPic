package com.leafpic.app.base;

import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import com.afollestad.appthemeengine.ATEActivity;

/**
 * Created by dnld on 1/25/16.
 */
public class BaseThemedActivity extends ATEActivity {

    @Nullable
    @Override
    protected final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }
}