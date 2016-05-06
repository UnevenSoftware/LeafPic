package com.horaapps.leafpic;

import android.app.Application;

import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Base.Album;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private HandlingAlbums albums;
    private Album currentAlbum;

    public Album getCurrentAlbum() {
        return currentAlbum;
    }

    public void setCurrentAlbum(Album currentAlbum) {
        this.currentAlbum = currentAlbum;
    }


    public HandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(HandlingAlbums albums) {
        this.albums = albums;
    }
}