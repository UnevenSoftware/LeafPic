package com.horaapps.leafpic;

import android.app.Application;

import com.horaapps.leafpic.Data.Album;
import com.horaapps.leafpic.Data.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private HandlingAlbums albums = null;

    public Album getCurrentAlbum() {
        return albums.getCurrentAlbum();
    }

    @Override
    public void onCreate() {
        albums = new HandlingAlbums(getApplicationContext());
        super.onCreate();
    }

    public HandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(HandlingAlbums albums) {
        this.albums = albums;
    }

    void updateAlbums() {
        albums.loadAlbums(getApplicationContext());
    }
}