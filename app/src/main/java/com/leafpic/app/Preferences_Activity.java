package com.leafpic.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Preferences_Activity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        //getActionBar().setTitle("Setting");
        //setTitle("Setting");
        initUiTweaks();
    }

    public void initUiTweaks() {

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.toolbar));
            getWindow().setStatusBarColor(getColor(R.color.toolbar));
        }*/



        /**** ToolBar
         toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
         setSupportActionBar(toolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setShowHideAnimationEnabled(true);
         toolbar.setBackgroundColor(getColor(R.color.trasparent_toolbar));
         */
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.preferences);
        }
    }
}
