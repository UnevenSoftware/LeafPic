package com.horaapps.leafpic;

import android.app.Application;

import com.horaapps.leafpic.Base.newAlbum;
import com.horaapps.leafpic.Base.newHandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private newHandlingAlbums albums;
    private newAlbum currentAlbum;

    public newAlbum getCurrentAlbum() {
        return currentAlbum;
    }

    public void setCurrentAlbum(newAlbum currentAlbum) {
        this.currentAlbum = currentAlbum;
    }


    public newHandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(newHandlingAlbums albums) {
        this.albums = albums;
    }
}