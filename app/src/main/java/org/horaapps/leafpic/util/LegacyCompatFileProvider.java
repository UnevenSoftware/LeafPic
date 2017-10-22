package org.horaapps.leafpic.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.commonsware.cwac.provider.LegacyCompatCursorWrapper;

import org.horaapps.leafpic.BuildConfig;

import java.io.File;

/**
 * Created by dnld on 16/10/17.
 */

public class LegacyCompatFileProvider extends FileProvider {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return(new LegacyCompatCursorWrapper(super.query(uri, projection, selection, selectionArgs, sortOrder)));
    }

    public static Uri getUri(Context context, File file) {
        return getUriForFile(context, context.getPackageName() + ".provider", file);
    }
}
