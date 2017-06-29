package horaapps.org.liz;

import android.app.Application;

import com.orhanobut.hawk.Hawk;

/**
 * Created by dnld on 6/29/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(getApplicationContext()).build();
    }
}
