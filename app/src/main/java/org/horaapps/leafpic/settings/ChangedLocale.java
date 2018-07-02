package org.horaapps.leafpic.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

public class ChangedLocale extends AppCompatActivity {
    private Locale mCurrentLocale;

    @Override
    protected void onStart() {
        super.onStart();

        mCurrentLocale = getResources().getConfiguration().locale;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Locale locale = getLocale(this);

        if (!locale.equals(mCurrentLocale)) {

            mCurrentLocale = locale;
            recreate();
        }
    }


    public static Locale getLocale(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String lang = sharedPreferences.getString("language", "en");
        switch (lang) {
            case "English":
                lang = "en";
                break;
        }
        return new Locale(lang);
    }

}
