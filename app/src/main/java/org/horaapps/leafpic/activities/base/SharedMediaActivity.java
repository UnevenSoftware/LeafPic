package org.horaapps.leafpic.activities.base;

import android.os.Bundle;

import org.horaapps.leafpic.MyApplication;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;

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
	return ((MyApplication) getApplicationContext()).getAlbum();
  }
}
