package com.horaapps.leafpic.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;


public class CustomTabService {
    private CustomTabsClient mCustomTabsClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent mCustomTabsIntent;

    private Activity activity;
    private int color;

    public CustomTabService (Activity act, int c) {
        this.activity = act;
        this.color = c;
        init();
    }

    private void init() {
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient= null;
            }
        };
        CustomTabsClient.bindCustomTabsService(activity, activity.getPackageName(), mCustomTabsServiceConnection);
        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .setToolbarColor(color)
                .build();
    }

    public void launchUrl(String Url){
        mCustomTabsIntent.launchUrl(activity, Uri.parse(Url));
    }
}
