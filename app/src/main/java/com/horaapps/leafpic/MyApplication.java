package com.horaapps.leafpic;

import android.app.Application;

import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Base.newAlbum;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private HandlingAlbums albums;
    private newAlbum currentAlbum;

    public newAlbum getCurrentAlbum() {
        return currentAlbum;
    }

    public void setCurrentAlbum(newAlbum currentAlbum) {
        this.currentAlbum = currentAlbum;
    }


    public HandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(HandlingAlbums albums) {
        this.albums = albums;
    }
}