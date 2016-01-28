package com.leafpic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by dnld on 1/26/16.
 */
public class PhotosReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.wtf("receiver_photo", "asdasdasd");
        Toast.makeText(context, "New Photo is Saved as : -", Toast.LENGTH_LONG).show();
        /*Cursor cursor = context.getContentResolver().query(intent.getData(),      null,null, null, null);
        cursor.moveToFirst();
        String image_path = cursor.getString(cursor.getColumnIndex("_data"));
        Toast.makeText(context, "New Photo is Saved as : -" + image_path, Toast.LENGTH_LONG).show();
        */
    }
}
