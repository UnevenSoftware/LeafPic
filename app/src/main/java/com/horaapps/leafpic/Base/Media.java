package com.horaapps.leafpic.Base;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;

import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by dnld on 26/04/16.
 */
public class Media implements Parcelable {

    String path = null;
    long dateModified = -1;
    String mime = null;

    long size = 0;
    boolean selected = false;

    public Media() { }

    public Media(String path, long dateModified) {
        this.path=path;
        this.dateModified=dateModified;
        setMIME();
    }

    public Media(String path, long dateModified, long size) {
        this.path = path;
        this.dateModified = dateModified;
        this.size = size;
        setMIME();
    }

    public Media(String path) {
        this.path = path;
        setMIME();
    }

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

    public long getSize() {
        return size;
    }

    public int getOrientation() {
        ExifInterface exif;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return 0; }
        return Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
    }

    public int getWidth() { //TODO improve
        ExifInterface exif;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return 0; }
        return Integer.parseInt(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
    }

    public int getHeight() { //TODO improve
        ExifInterface exif;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return 0; }
        return Integer.parseInt(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
    }

    public String getResolution() {
        return String.format(Locale.getDefault(), "%dx%d", getWidth(), getHeight());
    }

    public String getHumanReadableSize() {
        return StringUtils.humanReadableByteCount(size, true);
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

    protected Media(Parcel in) {
        path = in.readString();
        dateModified = in.readLong();
        mime = in.readString();
        size = in.readLong();
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
        dest.writeLong(size);
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}