package com.horaapps.leafpic.Base;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.koushikdutta.ion.bitmap.Exif;

import java.io.File;
import java.io.IOException;

/**
 * Created by dnld on 26/04/16.
 */
public class newMedia implements Parcelable {

    String path = null;
    long dateModified = -1;
    String mime = null;

    public String getMIME() {
        return mime;
    }

    public void setMIME() {
        String extension = path.substring(path.lastIndexOf('.')+1);
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    boolean selected = false;

    public newMedia(String path, long dateModified) {
        this.path=path;
        this.dateModified=dateModified;
        setMIME();
    }

    public newMedia(String path) {
        this.path = path;
        setMIME();
    }
    public boolean isGif() { return getPath().endsWith("gif"); }

    public boolean isImage() { return getMIME().startsWith("image"); }

    public boolean isVideo() { return getMIME().startsWith("video"); }

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

    public int getOrientation() {ExifInterface exif;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return 0; }
        return Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
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

    protected newMedia(Parcel in) {
        path = in.readString();
        dateModified = in.readLong();
        mime = in.readString();
        selected = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeLong(dateModified);
        dest.writeString(mime);
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<newMedia> CREATOR = new Parcelable.Creator<newMedia>() {
        @Override
        public newMedia createFromParcel(Parcel in) {
            return new newMedia(in);
        }

        @Override
        public newMedia[] newArray(int size) {
            return new newMedia[size];
        }
    };
}