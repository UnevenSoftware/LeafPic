package com.horaapps.leafpic.Base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        if(mime == null) mime= "custom";
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
        catch (IOException ex) { return 0; }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                }
            }
        }
        return 0;
    }

    public boolean setOrientation(int orientation){
        int asd;
        ExifInterface exif;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException ex) { return false; }
        switch (orientation) {
            case 90: asd = ExifInterface.ORIENTATION_ROTATE_90; break;
            case 180: asd = ExifInterface.ORIENTATION_ROTATE_180; break;
            case 270: asd = ExifInterface.ORIENTATION_ROTATE_270; break;
            case 0: asd = 1; break;
            default: return false;
        }
        exif.setAttribute(ExifInterface.TAG_ORIENTATION,asd+"");
        try {  exif.saveAttributes(); }
        catch (IOException e) {  return false;}
        return true;
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

    public long getDateEXIF() {
        ExifInterface exif;
        Date date;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return -1; }
        try {
            date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(exif.getAttribute(ExifInterface.TAG_DATETIME));
        }
        catch (ParseException e) { return -1; }
        catch (NullPointerException e) { return -1; }
        return date.getTime();
    }

    public boolean fixDate(){
        long newDate = getDateEXIF();
        if (newDate != -1){
            File f = new File(getPath());
            if (f.setLastModified(newDate)) {
                dateModified = newDate;
                return true;
            }
        }
        return false;
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

    public long getDate() {
        long exifDate = getDateEXIF();
        return exifDate != -1 ? exifDate : dateModified;
    }

    public long getDateModified() {
        return dateModified;
    }

    public boolean isSelected() {
        return selected;
    }

    public Bitmap getBitmap(){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path,bmOptions);
        bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
        return bitmap;
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