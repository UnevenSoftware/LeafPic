package com.horaapps.leafpic.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.horaapps.leafpic.R;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by dnld on 02/08/16.
 */

public class ThemeHelper {

  public static final int DARK_THEME = 2;
  public static final int LIGHT_THEME = 1;
  public static final int AMOLED_THEME = 3;

  private PreferenceUtil SP;
  private Context context;

  private int baseTheme;
  private int primaryColor;
  private int accentColor;

  public ThemeHelper(Context context) {
	this.SP = PreferenceUtil.getInstance(context);
	this.context = context;
	updateTheme();
  }

  public void updateTheme(){
	this.primaryColor = SP.getInt(context.getString(R.string.preference_primary_color),
			getColor(R.color.md_indigo_500));
	this.accentColor = SP.getInt(context.getString(R.string.preference_accent_color),
			getColor(R.color.md_light_blue_500));
	baseTheme = SP.getInt(context.getString(R.string.preference_base_theme), LIGHT_THEME);
  }

  public int getPrimaryColor() {
	return primaryColor;
  }

  public int getAccentColor() {
	return accentColor;
  }

  public int getBaseTheme(){ return baseTheme; }

  public static int getPrimaryColor(Context context) {
	PreferenceUtil SP = PreferenceUtil.getInstance(context);
	return SP.getInt(context.getString(R.string.preference_primary_color),
			ContextCompat.getColor(context, R.color.md_indigo_500));
  }

  public static int getAccentColor(Context context) {
	PreferenceUtil SP = PreferenceUtil.getInstance(context);
	return SP.getInt(context.getString(R.string.preference_accent_color),
			getColor(context,R.color.md_light_blue_500));
  }

  public static int getBaseTheme(Context context) {
	PreferenceUtil SP = PreferenceUtil.getInstance(context);
	return SP.getInt(context.getString(R.string.preference_base_theme), LIGHT_THEME);
  }

  public int getColor(@ColorRes int color) {
	return ContextCompat.getColor(context, color);
  }

  public static int getColor(Context context, @ColorRes int color) {
	return ContextCompat.getColor(context, color);
  }

  public int getBackgroundColor(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_dark_background);break;
	  case AMOLED_THEME:color = getColor(R.color.md_black_1000);break;
	  case LIGHT_THEME:
	  default:color = getColor(R.color.md_light_background);
	}
	return color;
  }

  public int getInvertedBackgroundColor(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_light_background);break;
	  case AMOLED_THEME:color = getColor(R.color.md_light_background);break;
	  case LIGHT_THEME:
	  default:color = getColor(R.color.md_black_1000);
	}
	return color;
  }

  public int getTextColor(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_grey_200);break;
	  case AMOLED_THEME:color = getColor(R.color.md_grey_200);break;
	  case LIGHT_THEME:
	  default:color = getColor(R.color.md_grey_800);
	}
	return color;
  }

  public int getSubTextColor(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_grey_400);break;
	  case AMOLED_THEME:color = getColor(R.color.md_grey_400);break;
	  case LIGHT_THEME:
	  default:color = getColor(R.color.md_grey_600);
	}
	return color;
  }

  public int getCardBackgroundColor(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_dark_cards);break;
	  case AMOLED_THEME:color = getColor(R.color.md_black_1000);break;
	  case LIGHT_THEME:default:color = getColor(R.color.md_light_cards);
	}
	return color;
  }

  public int getIconColor(){
	int color;
	switch (baseTheme){
	  case DARK_THEME: case AMOLED_THEME: color = getColor(R.color.md_white_1000);break;
	  case LIGHT_THEME: default: color = getColor(R.color.md_light_primary_icon);
	}
	return color;
  }

  public IconicsDrawable getToolbarIcon(IIcon icon) {
	return new IconicsDrawable(context).icon(icon).color(Color.WHITE).sizeDp(18);
  }

  public IconicsDrawable getIcon(IIcon icon) {
	return new IconicsDrawable(context).icon(icon).color(getIconColor());
  }


  public int getDrawerBackground(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_dark_cards);break;
	  case AMOLED_THEME:color = getColor(R.color.md_black_1000);break;
	  case LIGHT_THEME:
	  default: color = getColor(R.color.md_light_cards);
	}
	return color;
  }

  public Drawable getPlaceHolder(){
	switch (baseTheme){
	  case DARK_THEME : return ContextCompat.getDrawable(context, R.drawable.ic_empty);
	  case AMOLED_THEME : return ContextCompat.getDrawable(context, R.drawable.ic_empty_amoled);
	  case LIGHT_THEME: return ContextCompat.getDrawable(context, R.drawable.ic_empty_white);
	}
	return null;
  }

  public static Drawable getPlaceHolder(Context context){
	switch (getBaseTheme(context)){
	  case DARK_THEME : return ContextCompat.getDrawable(context, R.drawable.ic_empty);
	  case AMOLED_THEME : return ContextCompat.getDrawable(context, R.drawable.ic_empty_amoled);
	  case LIGHT_THEME: return ContextCompat.getDrawable(context, R.drawable.ic_empty_white);
	}
	return null;
  }

  public int getDialogStyle(){
	int style;
	switch (getBaseTheme()){
	  case DARK_THEME: style = R.style.AlertDialog_Dark;break;
	  case AMOLED_THEME: style = R.style.AlertDialog_Dark_Amoled;break;
	  case LIGHT_THEME: default: style = R.style.AlertDialog_Light;break;
	}
	return style;
  }

  public int getPopupToolbarStyle(){
	int style;
	switch (getBaseTheme()){
	  case DARK_THEME: style = R.style.DarkActionBarMenu;break;
	  case AMOLED_THEME: style = R.style.AmoledDarkActionBarMenu;break;
	  case LIGHT_THEME: default: style = R.style.LightActionBarMenu;
	}
	return style;
  }

  public ArrayAdapter<String> getSpinnerAdapter(ArrayList<String> items) {
	switch (getBaseTheme()){
	  case AMOLED_THEME:
	  case DARK_THEME: return new ArrayAdapter<String>(context, R.layout.spinner_item_light, items);
	  case LIGHT_THEME: default: return new ArrayAdapter<String>(context, R.layout.spinner_item_dark, items);
	}
  }

  public int getDefaultThemeToolbarColor3th(){
	int color;
	switch (baseTheme){
	  case DARK_THEME:color = getColor(R.color.md_black_1000); break;
	  case AMOLED_THEME:color = getColor(R.color.md_blue_grey_800);break;
	  case LIGHT_THEME: default: color = getColor(R.color.md_blue_grey_800);
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

  public void updateRadioButtonColor(RadioButton radioButton) {
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  radioButton.setButtonTintList(getRadioButtonColor());
	  radioButton.setTextColor(getTextColor());
	}
  }
  public void setRadioTextButtonColor(RadioButton radioButton, int color) {
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  radioButton.setButtonTintList(getRadioButtonColor());
	  radioButton.setTextColor(color);
	}
  }

  public void updateSwitchColor(SwitchCompat sw, int color){
	sw.getThumbDrawable().setColorFilter(sw.isChecked() ? color : getSubTextColor(), PorterDuff.Mode.MULTIPLY);
	sw.getTrackDrawable().setColorFilter(sw.isChecked() ? ColorPalette.getTransparentColor(color,100): getBackgroundColor(), PorterDuff.Mode.MULTIPLY);
  }

  public void setScrollViewColor(ScrollView scr){
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
	} catch(Exception e) {
	  e.printStackTrace();
	}
  }

  public void setColorScrollBarDrawable(Drawable drawable) {
	drawable.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));
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
}
