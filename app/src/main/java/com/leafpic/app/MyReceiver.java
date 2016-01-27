package com.leafpic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.leafpic.app.utils.string;

/**
 * Created by dnld on 1/27/16.
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        string.showToast(context, "service startde");
        Intent myIntent = new Intent(context, PhotoRecordingService.class);
        context.startService(myIntent);

    }
}