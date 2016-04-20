package com.horaapps.leafpic.Views;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;

import com.horaapps.leafpic.R;
import com.horaapps.leafpic.utils.ColorPalette;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

/**
 * Created by dnld on 23/02/16.
 */
public class ThemedActivity extends AppCompatActivity {

    SharedPreferences SP;

    private int primaryColor;
    private int accentColor;
    private boolean darkTheme;
    private boolean coloredNavBar;
    private boolean oscuredStatusBar;
    private boolean applyThemeImgAct; //TASPARENCY


    public boolean isNavigationBarColored() {
        return coloredNavBar;
    }

    public boolean isTraslucentStatusBar() {
        return oscuredStatusBar;
    }

    public boolean isApplyThemeOnImgAct() {
        return applyThemeImgAct;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public boolean isTransparencyZero() {
        return 255 - SP.getInt("set_alpha", 0) == 255;
    }

    public int getTransparency() {
        return 255 - SP.getInt("set_alpha", 0);
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    //METHOD
    public int getBackgroundColor(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_dark_background
                : R.color.md_light_background);
    }

    public int getInvertedBackgroundColor(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_light_background
                : R.color.md_dark_background);
    }

    public int getTextColor(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_grey_200
                : R.color.md_grey_800);
    }

    public int getSubTextColor(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_grey_400
                : R.color.md_grey_600);
    }

    public int getCardBackgroundColor(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_dark_cards
                : R.color.md_light_cards);
    }

    public int getIconColor(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_dark_primary_icon
                : R.color.md_light_primary_icon);
    }

    public int getDrawerBackground(){
        return ContextCompat.getColor(getApplicationContext(), isDarkTheme()
                ? R.color.md_dark_cards
                : R.color.md_light_cards);
    }


    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isNavigationBarColored()) getWindow().setNavigationBarColor(getPrimaryColor());
            else
                getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 255));
        }
    }

    public void updateSwitchColor(SwitchCompat sw, int color){
        if(sw.isChecked())
            sw.getThumbDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        else
            sw.getThumbDrawable().setColorFilter(getTextColor(), PorterDuff.Mode.MULTIPLY);
        sw.getTrackDrawable().setColorFilter(getBackgroundColor(), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateTheme();
    }

    public IconicsDrawable getToolbarIcon(IIcon icon){
        return new IconicsDrawable(this).icon(icon).color(Color.WHITE).sizeDp(18);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        updateTheme();
    }

    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTraslucentStatusBar())
                getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
            else
                getWindow().setStatusBarColor(getPrimaryColor());
        }
    }

    public void updateTheme(){
        this.primaryColor = SP.getInt("primary_color", ContextCompat.getColor(getApplicationContext(),R.color.md_indigo_500));//DEFAULT;
        this.accentColor = SP.getInt("accent_color", ContextCompat.getColor(getApplicationContext(), R.color.md_light_blue_500));//COLOR DEFAULT
        darkTheme = SP.getBoolean("set_dark_theme", false);//DARK THEME DEFAULT
        coloredNavBar = SP. getBoolean("nav_bar", false);
        oscuredStatusBar = SP.getBoolean("set_traslucent_statusbar",true);
        applyThemeImgAct = SP.getBoolean("apply_theme_img_act", false);
    }


    public void setRecentApp(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
            setTaskDescription(new ActivityManager.TaskDescription(text, drawable.getBitmap(), getPrimaryColor()));
        }
    }
}
