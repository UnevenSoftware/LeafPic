package com.leafpic.app;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by dnld on 1/16/16.
 */
class MediaStoreObserver extends ContentObserver {
    public MediaStoreObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.d("INSTANT", "GETTING CHANGES");
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        // do s.th.
        // depending on the handler you might be on the UI
        // thread, so be cautious!
        Log.d("INSTANT", "GETTING CHANGES");
        Log.d("INSTANT", uri.getPath());
    }

}

