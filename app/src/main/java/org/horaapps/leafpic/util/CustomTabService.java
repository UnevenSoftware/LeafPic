package org.horaapps.leafpic.util;

import android.content.ComponentName;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.widget.Toast;

import horaapps.org.liz.ThemedActivity;


public class CustomTabService {

    private CustomTabsClient mCustomTabsClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent mCustomTabsIntent;

    private ThemedActivity activity;

    public CustomTabService (ThemedActivity act) {
        this.activity = act;
        init();
    }

    private void init() {
        CustomTabsServiceConnection mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };
        CustomTabsClient.bindCustomTabsService(activity, activity.getPackageName(), mCustomTabsServiceConnection);
        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .setToolbarColor(activity.getPrimaryColor())
                .build();
    }

    public void launchUrl(String Url){
        try {
            mCustomTabsIntent.launchUrl(activity, Uri.parse(Url));
        } catch (Exception e) {
            Toast.makeText(activity.getApplication(), "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openUrl(ThemedActivity activity, String url) {
        CustomTabService service = new CustomTabService(activity);
        service.launchUrl(url);
    }
}
