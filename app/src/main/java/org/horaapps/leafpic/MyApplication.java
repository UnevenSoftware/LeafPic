package org.horaapps.leafpic;

import android.app.Application;

import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    public Album getAlbum() {
        return getAlbums().getCount() > 0 ? getAlbums().getCurrentAlbum() : Album.getEmptyAlbum();
    }

    public HandlingAlbums getAlbums() {
        return HandlingAlbums.getInstance(getApplicationContext());
    }
}