package org.horaapps.leafpic.util;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import org.horaapps.liz.ThemeHelper;

/**
 * A Chrome Custom Tabs wrapper to preload and show URLs in Chrome Custom Tabs.
 * Also provides a static method to launch a tab directly without warm up.
 */
public class ChromeCustomTabs {

    private CustomTabsServiceConnection serviceConnection;

    private CustomTabsIntent mCustomTabsIntent;

    @ColorInt
    private int toolbarColor;

    private Context context;

    public ChromeCustomTabs(@NonNull Context context) {
        this.context = context;
        toolbarColor = ThemeHelper.getInstance(context).getPrimaryColor();
        initService();
    }

    private void initService() {
        serviceConnection = new CustomTabsServiceConnection() {

            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                customTabsClient.warmup(0L);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // NO-OP
            }
        };
        // Bind the Chrome Custom Tabs service
        CustomTabsClient.bindCustomTabsService(context, ApplicationUtils.getPackageName(), serviceConnection);
        mCustomTabsIntent = new CustomTabsIntent.Builder().setShowTitle(true).setToolbarColor(toolbarColor).build();
    }

    public void launchUrl(String Url) {
        mCustomTabsIntent.launchUrl(context, Uri.parse(Url));
    }

    /**
     * Allow the Chrome Custom Tabs service to disconnect and GC.
     */
    public void destroy() {
        context.unbindService(serviceConnection);
    }

    /**
     * Launches a Chrome Custom Tab without warmup / service.
     *
     * @param context The context - used for launching an Activity.
     * @param url     The URL to load.
     */
    public static void launchUrl(@NonNull Context context, @NonNull String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
