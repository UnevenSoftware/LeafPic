package com.horaapps.leafpic.Views;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.horaapps.leafpic.R;
import com.horaapps.leafpic.utils.ColorPalette;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by dnld on 23/02/16.
 */
public class ThemedActivity extends AppCompatActivity {

    public static final int DARK_THEME = 2;
    public static final int LIGHT_THEME = 1;
    public static final int AMOLED_THEME = 3;

    private SharedPreferences SP;

    private int primaryColor;
    private int accentColor;
    private int basicTheme;
    private boolean coloredNavBar;
    private boolean oscuredStatusBar;
    private boolean applyThemeImgAct;


    public boolean isNavigationBarColored() {
        return coloredNavBar;
    }

    public boolean isTranslucentStatusBar() {
        return oscuredStatusBar;
    }

    protected boolean isApplyThemeOnImgAct() {
        return applyThemeImgAct;
    }

    protected boolean isTransparencyZero() {
        return 255 - SP.getInt(getString(R.string.preference_transparency), 0) == 255;
    }

    public int getTransparency() {
        return 255 - SP.getInt(getString(R.string.preference_transparency), 0);
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getBaseTheme(){ return  basicTheme; }

    protected int getBackgroundColor(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_dark_background);break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000);break;
            case LIGHT_THEME:
            default:color = ContextCompat.getColor(getApplicationContext(), R.color.md_light_background);
        }
        return color;
    }

    protected Drawable getPlaceHolder(){
        switch (basicTheme){
            case DARK_THEME : return ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_empty);
            case AMOLED_THEME : return ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_empty_amoled);
            case LIGHT_THEME: return ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_empty_white);
        }
        return null;
    }

    protected int getInvertedBackgroundColor(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_light_background);break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color
                    .md_light_background);break;
            case LIGHT_THEME:
            default:color = ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000);
        }
        return color;
    }

    public int getTextColor(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_200);break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_200);break;
            case LIGHT_THEME:
            default:color = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_800);
        }
        return color;
    }

    public int getSubTextColor(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_400);break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_400);
                break;
            case LIGHT_THEME:
            default:color = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_600);
        }
        return color;
    }

    public int getCardBackgroundColor(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_dark_cards);break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000);break;
            case LIGHT_THEME:default:color = ContextCompat.getColor(getApplicationContext(), R.color.md_light_cards);
        }
        return color;
    }

    public int getIconColor(){
        int color;
        switch (basicTheme){
            case DARK_THEME:case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_white_1000);break;
            case LIGHT_THEME:default:color = ContextCompat.getColor(getApplicationContext(), R.color.md_light_primary_icon);
        }
        return color;
    }

    protected int getDrawerBackground(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_dark_cards);break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000);break;
            case LIGHT_THEME:
            default: color = ContextCompat.getColor(getApplicationContext(), R.color.md_light_cards);
        }
        return color;
    }

    public int getDialogStyle(){
        int style;
        switch (getBaseTheme()){
            case DARK_THEME: style = R.style.AlertDialog_Dark;break;
            case AMOLED_THEME: style = R.style.AlertDialog_Dark_Amoled;break;
            case LIGHT_THEME: default: style = R.style.AlertDialog_Light;
        }
        return style;
    }

    protected int getPopupToolbarStyle(){
        int style;
        switch (getBaseTheme()){
            case DARK_THEME: style = R.style.DarkActionBarMenu;break;
            case AMOLED_THEME: style = R.style.AmoledDarkActionBarMenu;break;
            case LIGHT_THEME: default: style = R.style.LightActionBarMenu;
        }
        return style;
    }


    protected ArrayAdapter<String> getSpinnerAdapter(ArrayList<String> items) {
        switch (getBaseTheme()){
            case AMOLED_THEME:
            case DARK_THEME: return new ArrayAdapter<String>(this, R.layout.spinner_item_light, items);
            case LIGHT_THEME: default: return new ArrayAdapter<String>(this, R.layout.spinner_item_dark, items);
        }
    }


    protected int getDefaultThemeToolbarColor3th(){
        int color;
        switch (basicTheme){
            case DARK_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000); break;
            case AMOLED_THEME:color = ContextCompat.getColor(getApplicationContext(), R.color.md_blue_grey_800);break;
            case LIGHT_THEME: default: color = ContextCompat.getColor(getApplicationContext(), R.color.md_blue_grey_800);
        }
        return color;
    }

    private ColorStateList getRadioButtonColor(){
        return new ColorStateList(
                new int[][]{
                        new int[]{ -android.R.attr.state_enabled }, //disabled
                        new int[]{ android.R.attr.state_enabled } //enabled
                }, new int[] { getTextColor(), getAccentColor() });
    }

    protected void updateRadioButtonColor(RadioButton radioButton) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            radioButton.setButtonTintList(getRadioButtonColor());
            radioButton.setTextColor(getTextColor());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isNavigationBarColored()) getWindow().setNavigationBarColor(getPrimaryColor());
            else
                getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 255));
        }
    }

    public void updateSwitchColor(SwitchCompat sw, int color){
        sw.getThumbDrawable().setColorFilter(sw.isChecked() ? color : getSubTextColor(), PorterDuff.Mode.MULTIPLY);
        sw.getTrackDrawable().setColorFilter(sw.isChecked() ? ColorPalette.getTransparentColor(color,100): getBackgroundColor(), PorterDuff.Mode.MULTIPLY);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTranslucentStatusBar())
                getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
            else
                getWindow().setStatusBarColor(getPrimaryColor());
        }
    }

    protected void setScrollViewColor(ScrollView scr){
        try
        {
            Field mScrollCacheField = View.class.getDeclaredField("mScrollCache");
            mScrollCacheField.setAccessible(true);
            Object mScrollCache = mScrollCacheField.get(scr); // scr is your Scroll View

            Field scrollBarField = mScrollCache.getClass().getDeclaredField("scrollBar");
            scrollBarField.setAccessible(true);
            Object scrollBar = scrollBarField.get(mScrollCache);

            Method method = scrollBar.getClass().getDeclaredMethod("setVerticalThumbDrawable", Drawable.class);
            method.setAccessible(true);

            ColorDrawable ColorDraw = new ColorDrawable(getPrimaryColor());
            method.invoke(scrollBar, ColorDraw);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void setCursorDrawableColor(EditText editText, int color) {
        try {
            Field fCursorDrawableRes =
                    TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);

            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (final Throwable ignored) {  }
    }

    public void updateTheme(){
        this.primaryColor = SP.getInt(getString(R.string.preference_primary_color),
                ContextCompat.getColor(getApplicationContext(), R.color.md_indigo_500));
        this.accentColor = SP.getInt(getString(R.string.preference_accent_color),
                ContextCompat.getColor(getApplicationContext(), R.color.md_light_blue_500));
        basicTheme = SP.getInt(getString(R.string.preference_base_theme), LIGHT_THEME);
        coloredNavBar = SP. getBoolean(getString(R.string.preference_colored_nav_bar), false);
        oscuredStatusBar = SP.getBoolean(getString(R.string.preference_translucent_status_bar),true);
        applyThemeImgAct = SP.getBoolean(getString(R.string.preference_apply_theme_pager), true);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setRecentApp(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
            setTaskDescription(new ActivityManager.TaskDescription(text, drawable.getBitmap(), getPrimaryColor()));
        }
    }
}
