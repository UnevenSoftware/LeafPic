package com.horaapps.leafpic;

import android.app.Application;
import android.content.Context;

import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private HandlingAlbums albums;
    static Context context;

    public static Context getContext() { return context; }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    Album getCurrentAlbum() {
        return albums.getCurrentAlbum();
    }

    void removeCurrentAlbum() { albums.removeCurrentAlbum(); }

    public HandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(HandlingAlbums albums) {
        this.albums = albums;
    }

    void updateAlbums() {
        albums.loadPreviewAlbums(getApplicationContext());
    }
}