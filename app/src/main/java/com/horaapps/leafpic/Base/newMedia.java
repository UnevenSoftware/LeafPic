package com.horaapps.leafpic.Base;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by dnld on 26/04/16.
 */
public class newMedia {

    String path = null;
    long dateModified = -1;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    boolean selected = false;

    public newMedia(String path, long dateModified) {
        this.path=path;
        this.dateModified=dateModified;
    }

    public newMedia(String path) {
        this.path=path;
    }

    public Uri getUri() {
        return Uri.fromFile(new File(path));
    }

    public byte[] getThumnail()  {
        ExifInterface exif;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return null; }
        byte[] imageData = exif.getThumbnail();
        if (imageData != null)
            return imageData;
        return null;
    }

    public String getPath() {
        return path;
    }

    public long getDateModified() {
        return dateModified;
    }

    public boolean isSelected() {
        return selected;
    }
}
