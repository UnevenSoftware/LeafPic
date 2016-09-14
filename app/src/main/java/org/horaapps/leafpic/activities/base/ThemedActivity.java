package org.horaapps.leafpic.activities.base;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.iconics.typeface.IIcon;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.ThemeHelper;

import java.util.ArrayList;

/**
 * Created by dnld on 23/02/16.
 */
public class ThemedActivity extends AppCompatActivity {


  private ThemeHelper themeHelper;
  private PreferenceUtil SP;


  private boolean coloredNavBar;
  private boolean obscuredStatusBar;
  private boolean applyThemeImgAct;


  @Override
  public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	SP = PreferenceUtil.getInstance(getApplicationContext());
	themeHelper = new ThemeHelper(getApplicationContext());
  }

  @Override
  public void onResume(){
	super.onResume();
	updateTheme();
  }

  public void updateTheme(){
	themeHelper.updateTheme();
	coloredNavBar = SP.getBoolean(getString(R.string.preference_colored_nav_bar), false);
	obscuredStatusBar = SP.getBoolean(getString(R.string.preference_translucent_status_bar),true);
	applyThemeImgAct = SP.getBoolean(getString(R.string.preference_apply_theme_pager), true);
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    // NOTE: icons stuff
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public void setNavBarColor() {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  if (isNavigationBarColored()) getWindow().setNavigationBarColor(getPrimaryColor());
	  else
		getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 255));
	}
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  protected void setStatusBarColor() {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  if (isTranslucentStatusBar())
		getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
	  else
		getWindow().setStatusBarColor(getPrimaryColor());
	}
  }

  protected void setScrollViewColor(ScrollView scr){
	themeHelper.setScrollViewColor(scr);
  }

  public void setCursorDrawableColor(EditText editText, int color) {
	// TODO: 02/08/16 remove this
	ThemeHelper.setCursorDrawableColor(editText, color);
  }



  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public void setRecentApp(String text){
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
	  setTaskDescription(new ActivityManager.TaskDescription(text, drawable.getBitmap(), getPrimaryColor()));
	}
  }


  public boolean isNavigationBarColored() {
	return coloredNavBar;
  }

  public boolean isTranslucentStatusBar() {
	return obscuredStatusBar;
  }

  protected boolean isApplyThemeOnImgAct() {
	return applyThemeImgAct;
  }

  protected boolean isTransparencyZero() {
	return 255 - SP.getInt(getString(R.string.preference_transparency), 0) == 255;
  }

  public int getTransparency() {
	return 255 - SP.getInt(getString(org.horaapps.leafpic.R.string.preference_transparency), 0);
  }

  public void setBaseTheme(int baseTheme, boolean permanent) {
	themeHelper.setBaseTheme(baseTheme, permanent);
  }

  public void themeSeekBar(SeekBar bar) {
  	themeHelper.themeSeekBar(bar);
  }
  public int getPrimaryColor() {
	return themeHelper.getPrimaryColor();
  }

  public int getAccentColor() {
	return themeHelper.getAccentColor();
  }

  public int getBaseTheme(){ return  themeHelper.getBaseTheme(); }

  protected int getBackgroundColor(){
	return themeHelper.getBackgroundColor();
  }

  protected Drawable getPlaceHolder(){
	return themeHelper.getPlaceHolder();
  }

  protected int getInvertedBackgroundColor(){
	return themeHelper.getInvertedBackgroundColor();
  }

  public int getTextColor(){
	return themeHelper.getTextColor();
  }

  public int getSubTextColor(){
	return themeHelper.getSubTextColor();
  }

  public int getCardBackgroundColor(){
	return themeHelper.getCardBackgroundColor();
  }

  public int getIconColor(){
	return themeHelper.getIconColor();
  }

  protected int getDrawerBackground(){
	return themeHelper.getDrawerBackground();
  }

  public int getDialogStyle(){
	return themeHelper.getDialogStyle();
  }

  protected int getPopupToolbarStyle(){
	return themeHelper.getPopupToolbarStyle();
  }

  protected ArrayAdapter<String> getSpinnerAdapter(ArrayList<String> items) {
	return themeHelper.getSpinnerAdapter(items);
  }

  protected int getDefaultThemeToolbarColor3th(){
	return themeHelper.getDefaultThemeToolbarColor3th();
  }

  protected void updateRadioButtonColor(RadioButton radioButton) {
	themeHelper.updateRadioButtonColor(radioButton);
  }
  protected void setRadioTextButtonColor(RadioButton radioButton, int color) {
	themeHelper.setRadioTextButtonColor(radioButton, color);
  }

  public void updateSwitchColor(SwitchCompat sw, int color){
	themeHelper.updateSwitchColor(sw, color);
  }

  public IconicsDrawable getToolbarIcon(IIcon icon){
	return themeHelper.getToolbarIcon(icon);
  }
}