package com.horaapps.leafpic.Views;

import android.os.Bundle;

import com.horaapps.leafpic.Data.Album;
import com.horaapps.leafpic.Data.HandlingAlbums;
import com.horaapps.leafpic.MyApplication;

/**
 * Created by dnld on 03/08/16.
 */

public class SharedMediaActivity extends ThemedActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
  }

  public HandlingAlbums getAlbums() {
	return ((MyApplication) getApplicationContext()).getAlbums();
  }

  public Album getAlbum() {
	return ((MyApplication) getApplicationContext()).getCurrentAlbum();
  }
}
