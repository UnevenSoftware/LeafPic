package com.leafpic.app.Views;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.leafpic.app.PhotoPagerActivity;
import com.leafpic.app.R;
import com.leafpic.app.utils.ColorPalette;

/**
 * Created by dnld on 23/02/16.
 */
public class ThemedActivity extends AppCompatActivity {

    SharedPreferences SP;

    private int primaryColor;
    private int accentColor;
    private boolean darkTheme;
    private boolean coloredNavBar;
    private boolean openCollapsing;
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

    public boolean isCollapsingToolbar() {
        return openCollapsing;
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
        if(darkTheme) return ColorPalette.getDarkBackgroundColor(getApplicationContext());
        else return ColorPalette.getLightBackgroundColor(getApplicationContext());
    }

    public int getInvertedBackgroundColor(){
        if(darkTheme) return ColorPalette.getLightBackgroundColor(getApplicationContext());
        else return ColorPalette.getDarkBackgroundColor(getApplicationContext());
    }

    public int getTextColor(){
        if(darkTheme) return ColorPalette.getDarkTextColor(getApplicationContext());
        else return ColorPalette.getLightTextColor(getApplicationContext());
    }

    public void setNavBarColor() {
        if (this.getClass().getSimpleName().equals(PhotoPagerActivity.class.getSimpleName())) {
            if(isApplyThemeOnImgAct())
                if (isNavigationBarColored())
                    getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
                else
                    getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), getTransparency()));
            else
                getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent_dark_gray));
        } else
            if (isNavigationBarColored()) getWindow().setNavigationBarColor(getPrimaryColor());
            else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
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

    protected void setStatusBarColor() {
        /* if (makeTranslucent) {
            //getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //getWindow().setStatusBarColor(getPrimaryColor());
        }
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);*/
        // TODO : IT WORKS BUT, MUST BE OPTIMIZED
        if (this.getClass().getSimpleName().equals(PhotoPagerActivity.class.getSimpleName())) {
            if(isApplyThemeOnImgAct())
                if (isTraslucentStatusBar() && isTransparencyZero())
                    getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                else
                    getWindow().setStatusBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
            else
                getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent_dark_gray));
        } else {
            if (isTraslucentStatusBar()) {
                int c = ColorPalette.getOscuredColor(getPrimaryColor());
                getWindow().setStatusBarColor(c);
            } else
                getWindow().setStatusBarColor(getPrimaryColor());
        }
    }


    /*public int getTransparentColor(int color, int alpha){
        int res = ColorUtils.setAlphaComponent(color, alpha);
        return  res;
    }*/

    public void updateTheme(){
        this.primaryColor = SP.getInt("primary_color", ContextCompat.getColor(getApplicationContext(),R.color.md_teal_500));//TEAL CARD BG DEFAULT;
        this.accentColor = SP.getInt("accent_color", ContextCompat.getColor(getApplicationContext(), R.color.md_orange_500));//TEAL COLOR DEFAULT
        darkTheme = SP.getBoolean("set_dark_theme", true);//DARK THEME DEFAULT
        coloredNavBar = SP. getBoolean("nav_bar", false);
        openCollapsing = SP.getBoolean("set_collaps_toolbar", true);
        oscuredStatusBar = SP.getBoolean("set_traslucent_statusbar",true);
        applyThemeImgAct = SP.getBoolean("apply_theme_img_act", false);
    }

    public void setRecentApp(String text){
        BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
        setTaskDescription(new ActivityManager.TaskDescription(text, drawable.getBitmap(), getPrimaryColor()));
    }
}
