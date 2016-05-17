package com.horaapps.leafpic;

import android.app.Application;

import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Base.Album;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private HandlingAlbums albums;

    public Album getCurrentAlbum() {
        return albums.getCurrentAlbum();
    }

    public HandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(HandlingAlbums albums) {
        this.albums = albums;
    }
}