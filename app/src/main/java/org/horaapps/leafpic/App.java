package org.horaapps.leafpic;

import android.support.multidex.MultiDexApplication;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;
import com.orhanobut.hawk.Hawk;
import com.squareup.leakcanary.LeakCanary;
import org.horaapps.leafpic.util.ApplicationUtils;
import org.horaapps.leafpic.util.preferences.Prefs;

/**
 * Created by dnld on 28/04/16.
 */
public class App extends MultiDexApplication {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ApplicationUtils.init(this);
        /**
         * This process is dedicated to LeakCanary for heap analysis.
         *  You should not init your app in this process.
         */
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        registerFontIcons();
        initialiseStorage();
    }

    public static App getInstance() {
        return mInstance;
    }

    private void registerFontIcons() {
        Iconics.registerFont(new GoogleMaterial());
        Iconics.registerFont(new CommunityMaterial());
        Iconics.registerFont(new FontAwesome());
    }

    private void initialiseStorage() {
        Prefs.init(this);
        Hawk.init(this).build();
    }
}
