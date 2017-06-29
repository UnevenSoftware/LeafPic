package org.horaapps.leafpic;

import android.app.Application;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.HandlingAlbums;

/**
 * Created by dnld on 28/04/16.
 */
public class App extends /*horaapps.org.liz.App*/ Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(this).build();

        Log.wtf("asd", Hawk.get("card_view_style") + "");
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