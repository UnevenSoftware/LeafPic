package org.horaapps.leafpic.activities.base;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ThemedActivity;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends ThemedActivity {
    CompositeDisposable disposables = new CompositeDisposable();


    public void disposeLater(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(Prefs.forceEnglish()) forceEnglish();
        super.onCreate(savedInstanceState);
    }

    public void forceEnglish() {
        changeLocale(new Locale("it"));
    }

    public void restoreDefaultLocale() {
        changeLocale(Locale.getDefault());
    }

    private void changeLocale(Locale locale) {
        Configuration configuration = getResources().getConfiguration();
        Locale currentLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            currentLocale = configuration.getLocales().get(0);
        else currentLocale = configuration.locale;

        if(!currentLocale.getLanguage().equals(locale.getLanguage())) {
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, null);
        }
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        disposables.dispose();
        super.onDestroy();
    }
}
