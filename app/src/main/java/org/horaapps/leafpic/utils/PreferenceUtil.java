package org.horaapps.leafpic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dnld on 31/07/16.
 */

public class PreferenceUtil {

  private static PreferenceUtil instance;
  private SharedPreferences SP;

  private PreferenceUtil(Context mContext) {
	SP = PreferenceManager.getDefaultSharedPreferences(mContext);
  }

  public static PreferenceUtil getInstance(Context context) {
	if (instance == null) {
	  synchronized (PreferenceUtil.class) {
		if (instance == null)
		  instance = new PreferenceUtil(context);
	  }
	}
	return instance;
  }

  public SharedPreferences.Editor getEditor() {
	return SP.edit();
  }

  public void putString(String key, String value) {
	getEditor().putString(key, value).commit();
  }

  public String getString(String key, String defValue) {
	return SP.getString(key, defValue);
  }

  public void putInt(String key, int value) {
	getEditor().putInt(key, value).commit();
  }

  public int getInt(String key, int defValue) {
	return SP.getInt(key, defValue);
  }

  public void putBoolean(String key, boolean value) {
	getEditor().putBoolean(key, value).commit();
  }

  public boolean getBoolean(String key, boolean defValue) {
	return SP.getBoolean(key, defValue);
  }


  public void remove(String key) {
	getEditor().remove(key).commit();
  }

  public void clearPreferences() {
	getEditor().clear().commit();
  }
}
