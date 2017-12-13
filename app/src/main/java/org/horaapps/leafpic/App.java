package org.horaapps.leafpic;

import android.app.Application;

import com.orhanobut.hawk.Hawk;
import com.squareup.leakcanary.LeakCanary;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.HandlingAlbums;
import org.horaapps.leafpic.util.preferences.Prefs;

/**
 * Created by dnld on 28/04/16.
 */
public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        initialiseStorage();
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

    private void initialiseStorage() {
        Prefs.init(this);
        Hawk.init(this).build();
    }
}