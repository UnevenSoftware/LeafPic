package com.leafpic.app.Views;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.leafpic.app.Base.ColorPalette;
import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

/**
 * Created by dnld on 23/02/16.
 */
public class ThemedActivity extends AppCompatActivity {

    SharedPreferences SP;

    private int primaryColor;
    private int accentColor;
    private boolean darkTheme;
    private boolean coloredNavigationBar;

    public int getAccentColor() {
        return accentColor;
    }

    public boolean isNavigationBarColored() {
        return coloredNavigationBar;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public int getBackgroundColor(){
        if(darkTheme) return ColorPalette.getDarkBackgroundColor(getApplicationContext());
        else return ColorPalette.getLightBackgroundColor(getApplicationContext());
    }
    @Override
    public void onResume(){
        super.onResume();
        updateTheme();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        updateTheme();
    }

    public void updateTheme(){
        this.primaryColor = SP.getInt("primary_color", Color.rgb(0, 150, 136));//TEAL CARD BG DEFAULT;
        this.accentColor = SP.getInt("accent_color", Color.rgb(0, 77, 64));//TEAL COLOR DEFAULT
        darkTheme = SP.getBoolean("set_dark_theme", false);
        coloredNavigationBar =SP. getBoolean("nav_bar", false);
    }
    public void setRecentApp(String text){
        BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
        setTaskDescription(new ActivityManager.TaskDescription(text, drawable.getBitmap(), getPrimaryColor()));
    }
}
