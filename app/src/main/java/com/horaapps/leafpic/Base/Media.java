package com.horaapps.leafpic.Base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by dnld on 26/04/16.
 */
public class Media implements Parcelable {

    private static final String TAG_WIDTH = "Image Width";
    private static final String TAG_HEIGHT = "Image Height";
    private static final String TAG_DATE_TAKEN = "Date/Time Original";
    public static final String TAG_MAKE = "Make";
    public static final String TAG_MODEL = "Model";
    public static final String TAG_F_NUMBER = "F-Number";
    public static final String TAG_ISO = "ISO Speed Ratings";
    private static final String TAG_ORIENTATION = "Orientation";

    //region ORIENTATION VALUES
    private static final int ORIENTATION_NORMAL = 1;
    private static final int ORIENTATION_ROTATE_180 = 3;
    private static final int ORIENTATION_ROTATE_90 = 6;  // rotate 90 cw to right it
    private static final int ORIENTATION_ROTATE_270 = 8;  // rotate 270 to right it
    //endregion

    private final Map<String, Object> metadataMap = new HashMap<String, Object>();

    String path = null;
    private long dateModified = -1;
    private String mime = null;

    private long size = 0;
    private boolean selected = false;

    public Media() { }

    public Media(String path, long dateModified) {
        this.path=path;
        this.dateModified=dateModified;
        setMIME();
        //loadMetadata();
    }

    public Media(String path, long dateModified, long size) {
        this.path = path;
        this.dateModified = dateModified;
        this.size = size;
        setMIME();
        //loadMetadata();
    }

    public Media(String path) {
        this.path = path;
        setMIME();
        //loadMetadata();
    }

    public String getMIME() {
        return mime;
    }

    private void setMIME() {
        String extension = path.substring(path.lastIndexOf('.')+1);
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        if(mime == null) mime= "unknown";
    }

    void setSelected(boolean selected) {
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
        loadMetadata();
        if (metadataMap.containsKey(TAG_ORIENTATION)) {
            switch (Integer.parseInt(metadataMap.get(TAG_ORIENTATION).toString())) {
                case ORIENTATION_NORMAL: return 0;
                case ORIENTATION_ROTATE_90: return 90;
                case ORIENTATION_ROTATE_180: return 180;
                case ORIENTATION_ROTATE_270: return 270;
            }
        }
        return -1;
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

    private void loadMetadata() {
        if (metadataMap.isEmpty()) {
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(new File(getPath()));
                for (Directory directory : metadata.getDirectories())
                    for (Tag tag : directory.getTags()) {
                        metadataMap.put(tag.getTagName(), directory.getObject(tag.getTagType()));
                        //Log.wtf("asd", tag.getTagName());
                    }
            } catch (Exception e){ e.printStackTrace(); }
        }
    }

    public int getWidth() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_WIDTH))
            return Integer.parseInt(metadataMap.get(TAG_WIDTH).toString());
        return -1;
    }

    public int getHeight() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_HEIGHT))
            return Integer.parseInt(metadataMap.get(TAG_HEIGHT).toString());
        return -1;
    }

    public long getDateTaken() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_DATE_TAKEN))
            //
            try { return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(metadataMap.get(TAG_DATE_TAKEN).toString()).getTime(); }
            catch (ParseException e) { return -1; }
            catch (NullPointerException e) { return -1; }

        return -1;

        /*ExifInterface exif;
        Date date;
        try { exif = new ExifInterface(getPath()); }
        catch (IOException e) {  return -1; }
        try {
            date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(exif.getAttribute(ExifInterface.TAG_DATETIME));
        }
        catch (ParseException e) { return -1; }
        catch (NullPointerException e) { return -1; }
        return date.getTime();*/
    }

    public boolean fixDate(){
        long newDate = getDateTaken();
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
        long exifDate = getDateTaken();
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