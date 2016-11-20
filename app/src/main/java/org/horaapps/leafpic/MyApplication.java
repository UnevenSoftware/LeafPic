package org.horaapps.leafpic;

import android.app.Application;

import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    public Album getAlbum() {
        return getAlbums().getCount() > 0 ? getAlbums().getCurrentAlbum() : Album.getEmptyAlbum();
    }

    public HandlingAlbums getAlbums() {
        return HandlingAlbums.getInstance(getApplicationContext());
    }
}