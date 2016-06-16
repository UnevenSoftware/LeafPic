package com.horaapps.leafpic.Base;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static final String TAG_MAKE = "Make";
    private static final String TAG_MODEL = "Model";
    private static final String TAG_F_NUMBER = "F-Number";
    private static final String TAG_ISO = "ISO Speed Ratings";
    private static final String TAG_EXPOSURE = "Exposure Time";
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
    private Uri uri = null;

    private long size = 0;
    private boolean selected = false;

    public Media() { }

    public Media(String path, long dateModified) {
        this.path = path;
        this.dateModified = dateModified;
        setMIME();
    }

    public Media(File file) {
        this.path = file.getAbsolutePath();
        this.dateModified = file.lastModified();
        this.size = file.length();
        setMIME();
    }

    public Media(String path) {
        this.path = path;
        setMIME();
    }

    public Media(Context context, Uri mediaUri) {
        this.uri = mediaUri;
        this.path = null;
        ContentResolver cR = context.getContentResolver();
        mime = MimeTypeMap.getSingleton().getExtensionFromMimeType(cR.getType(uri));
        if(mime == null) mime = "unknown";
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

    public boolean isGif() { return getMIME().endsWith("gif"); }

    public boolean isImage() { return getMIME().startsWith("image"); }

    public boolean isVideo() { return getMIME().startsWith("video"); }

    public Uri getUri() {
        return isMediainStorage() ? Uri.fromFile(new File(path)) : uri;
    }

    public byte[] getThumbnail() {

        ExifInterface exif;
        try {
            exif = new ExifInterface(getPath());
        } catch (IOException e) {
            return null;
        }
        byte[] imageData = exif.getThumbnail();
        if (imageData != null)
            return imageData;
        return null;

        // NOTE: ExifInterface is faster than metadata-extractor to get the thumbnail data
        /*try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(getPath()));
            ExifThumbnailDirectory thumbnailDirectory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
            if (thumbnailDirectory.hasThumbnailData())
                return thumbnailDirectory.getThumbnailData();
        } catch (Exception e) { return null; }*/
    }


    public GeoLocation getGeoLocation()  {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(getPath()));
            GpsDirectory thumbnailDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            return thumbnailDirectory.getGeoLocation();
        } catch (Exception e) { return null; }
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
                default: return -1;
            }
        }
        return -1;
    }

    public boolean setOrientation(int orientation){
        /*int asd;
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
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, asd+"");
        try {  exif.saveAttributes(); }
        catch (IOException e) {  return false;}
        return true;*/
        return false;
    }

    public boolean isMediainStorage() {
        return path != null;
    }

    public String getExifInfo() {
        return String.format("%s %s %s", getFNumber(), getExposureTime(), getISO());
    }

    private Rational getRational(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Rational)
            return (Rational)o;
        if (o instanceof Integer)
            return new Rational((Integer)o, 1);
        if (o instanceof Long)
            return new Rational((Long)o, 1);

        // NOTE not doing conversions for real number types

        return null;
    }

    private String getFNumber() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_F_NUMBER)) {
            DecimalFormat format = new DecimalFormat("0.0");
            format.setRoundingMode(RoundingMode.HALF_UP);
            Rational f = getRational(metadataMap.get(TAG_F_NUMBER));

            return "f/" + format.format(f.doubleValue());
        }
        return null;
    }

    private String getExposureTime() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_EXPOSURE)){
            DecimalFormat format = new DecimalFormat("0.000");
            format.setRoundingMode(RoundingMode.HALF_UP);
            Rational f = getRational(metadataMap.get(TAG_EXPOSURE));
            return format.format(f.doubleValue())+"s";
        }
        return null;
    }

    private String getISO() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_ISO))
            return "ISO-"+metadataMap.get(TAG_ISO).toString();
        return null;
    }

    public String getCameraInfo() {
        String make;
        if ((make = getMake()) != null)
            return String.format("%s %s", make, getModel());
        return null;
    }

    private String getMake() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_MAKE))
            return metadataMap.get(TAG_MAKE).toString();
        return null;
    }

    private String getModel() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_MODEL))
            return metadataMap.get(TAG_MODEL).toString();
        return null;
    }


    private int getWidth() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_WIDTH))
            return Integer.parseInt(metadataMap.get(TAG_WIDTH).toString());
        return -1;
    }

    private int getHeight() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_HEIGHT))
            return Integer.parseInt(metadataMap.get(TAG_HEIGHT).toString());
        return -1;
    }

    public long getDateTaken() {
        loadMetadata();
        if (metadataMap.containsKey(TAG_DATE_TAKEN))
            try { return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(metadataMap.get(TAG_DATE_TAKEN).toString()).getTime(); }
            catch (ParseException e) { return -1; }
            catch (NullPointerException e) { return -1; }
        return -1;
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