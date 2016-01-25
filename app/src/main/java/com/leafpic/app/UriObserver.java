package com.leafpic.app;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by dnld on 1/25/16.
 */
public class UriObserver {
    private final Cursor mCursor;
    private final ContentObserver mObserver;
    private boolean mRunning = true;

    public UriObserver(Cursor c, final OnChangeListener listener) {
        mCursor = c;
        mObserver = new ObserverWithListener(listener);
        mCursor.registerContentObserver(mObserver);
    }

    public static UriObserver getInstance(ContentResolver contentResolver, Uri uri, OnChangeListener listener) {
        Cursor c = contentResolver.query(uri, new String[]{"*"}, null, null, null);

        if ((c = Dao.moveToFirst(c)) == null) {
            log.e("Cannot start observer for uri: " + uri);
            return null;
        }

        return new UriObserver(c, listener);
    }

    public void stop() {
        mCursor.unregisterContentObserver(mObserver);
        Dao.closeCursor(mCursor);
        mRunning = false;
    }

    public interface OnChangeListener {
        void onChange();
    }

    private class ObserverWithListener extends ContentObserver {
        private final OnChangeListener mListener;

        public ObserverWithListener(OnChangeListener listener) {
            super(new Handler());

            mListener = listener;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mRunning) {
                Log.d("asd_observer", "Change triggered");
                mListener.onChange();
            }
        }
    }
}