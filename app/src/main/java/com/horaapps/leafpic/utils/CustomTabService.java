package com.horaapps.leafpic.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;


public class CustomTabService {
    final String CUSTOM_TAB_PACKAGE_NAME = "com.horaapps.leafpic";
    CustomTabsClient mCustomTabsClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent mCustomTabsIntent;
    SharedPreferences SP;

    Activity activity;
    int color;
    public CustomTabService (Activity act, int c){
        this.activity = act;
        this.color = c;
    }

    public void Inizialize() {
        //CUSTOM TAB
        SP = PreferenceManager.getDefaultSharedPreferences(activity);

        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient= customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient= null;
            }
        };
        CustomTabsClient.bindCustomTabsService(activity, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);
        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .setToolbarColor(color)
                .build();
    }

    public void LaunchUrl(String Url){
        mCustomTabsIntent.launchUrl(activity, Uri.parse(Url));
    }
}
