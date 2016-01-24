package com.leafpic.app;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class Preferences_Activity extends PreferenceActivity {

    SharedPreferences SP;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        initUiTweaks();

        //FOR ADDING TOOLBAR
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.setting_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    /**********

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.preferences);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        //getActionBar().setTitle("Setting");
        //setTitle("Setting");
        //initUiTweaks();
    }
     *********/
    public void initUiTweaks() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.status_bar));

            SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean NavBar = SP.getBoolean("nav_bar", false);
            //boolean NightTheme = SP.getBoolean("set_theme", false);
            if (NavBar==true)
                getWindow().setNavigationBarColor(getResources().getColor(R.color.toolbar));
        }
        /*
         toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
         setSupportActionBar(toolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setShowHideAnimationEnabled(true);
         toolbar.setBackgroundColor(getColor(R.color.trasparent_toolbar));
        */
     }


}
