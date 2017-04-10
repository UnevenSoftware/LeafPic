package org.horaapps.leafpic;

import android.app.Application;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static App getInstance() {
        return mInstance;
    }

    @Deprecated
    public Album getAlbum() {
        return Album.getEmptyAlbum();
    }

    @Deprecated
    public HandlingAlbums getAlbums() {
        return HandlingAlbums.getInstance(getApplicationContext());
    }
}