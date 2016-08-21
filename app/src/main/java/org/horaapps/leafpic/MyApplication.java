package org.horaapps.leafpic;

import android.app.Application;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class MyApplication extends Application {

    private HandlingAlbums albums = null;

    public Album getCurrentAlbum() {
        return albums.dispAlbums.size() > 0 ? albums.getCurrentAlbum() : new Album();
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

    public void updateAlbums() {
        albums.loadAlbums(getApplicationContext());
    }
}