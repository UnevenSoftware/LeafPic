package org.horaapps.leafpic.Data;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.signature.StringSignature;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.StringUtils;

import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dnld on 26/04/16.
 */
public class Media implements Parcelable, Serializable {


    private String path = null;
    private long dateModified = -1;
    private String mime = null;
    private String uri = null;
    private long id;

    private long size = 0;
    private boolean selected = false;
    private MetadataItem metadata;

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
        this.uri = mediaUri.toString();
        this.path = null;
        setMIME(context.getContentResolver().getType(getUri()));
    }

    public Media(@NotNull Cursor cur) {
        this.path = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));
        this.size = cur.getLong(cur.getColumnIndex(MediaStore.Images.Media.SIZE));
        this.dateModified = cur.getLong(cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
        setMIME(cur.getString(cur.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)));
        this.id = cur.getLong(cur.getColumnIndex(MediaStore.Images.Media._ID));
        this.uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), getId()).toString();
    }

    public void setUri(String uriString) {
        this.uri = uriString;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private long getId() {
        return id;
    }

    public String getMIME() {
        return mime;
    }

    private void setMIME() {
        String extension = path.substring(path.lastIndexOf('.')+1);
        setMIME(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase()));
    }

    public StringSignature getSignature() {
        return new StringSignature(getPath() + "-" + getDateModified());
    }

    public StringSignature getSignature(String salt) {
        return new StringSignature(getPath() + "-" + getDateModified() + salt );
    }

    private void setMIME(String mime) {
        this.mime = (mime != null) ? mime : "unknown";
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isGif() { return getMIME().endsWith("gif"); }

    public boolean isImage() { return getMIME().startsWith("image"); }

    public boolean isVideo() { return getMIME().startsWith("video"); }

    public Uri getUri() {
        return hasUri() ? Uri.parse(uri) : Uri.fromFile(new File(path));
    }

    @TestOnly
    public byte[] getThumbnail() {

        ExifInterface exif;
        try {
            exif = new ExifInterface(getPath());
        } catch (IOException e) {
            return null;
        }
        if (exif.hasThumbnail())
            return exif.getThumbnail();
        return null;

        // NOTE: ExifInterface is faster than metadata-extractor to getValue the thumbnail data
        /*try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(getMediaPath()));
            ExifThumbnailDirectory thumbnailDirectory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
            if (thumbnailDirectory.hasThumbnailData())
                return thumbnailDirectory.getThumbnailData();
        } catch (Exception e) { return null; }*/
    }

    @TestOnly
    public String getThumbnail(Context context) {
        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
                context.getContentResolver(), id,
                MediaStore.Images.Thumbnails.MINI_KIND,
                new String[]{ MediaStore.Images.Thumbnails.DATA } );
        if(cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
        return null;
    }


    public String getName() {
        return StringUtils.getPhotoNameByPath(path);
    }

    public MediaDetailsMap<String, String> getAllDetails() {
        MediaDetailsMap<String, String> data = new MediaDetailsMap<String, String>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(getPath()));
            for(Directory directory : metadata.getDirectories()) {

                for(Tag tag : directory.getTags()) {
                    data.put(tag.getTagName(), directory.getObject(tag.getTagType())+"");
                }
            }
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;

    }

    public MediaDetailsMap<String, String> getMainDetails(Context context){
        metadata = new MetadataItem(new File(getPath()));
        MediaDetailsMap<String, String> details = new MediaDetailsMap<String, String>();
        details.put(context.getString(R.string.path), getDisplayName());
        details.put(context.getString(R.string.type), getMIME());
        String tmp;
        if ((tmp = metadata.getResolution()) != null)
            details.put(context.getString(R.string.resolution), tmp);

        details.put(context.getString(R.string.size), getHumanReadableSize());
        details.put(context.getString(R.string.date), SimpleDateFormat.getDateTimeInstance().format(new Date(getDateModified())));
        if (metadata.getOrientation() != -1)
            details.put(context.getString(R.string.orientation), metadata.getOrientation()+"");
        if (metadata.getDateOriginal() != null)
            details.put(context.getString(R.string.date_taken), SimpleDateFormat.getDateTimeInstance().format(metadata.getDateOriginal()));

        if ((tmp = metadata.getCameraInfo()) != null)
            details.put(context.getString(R.string.camera), tmp);
        if ((tmp = metadata.getExifInfo()) != null)
            details.put(context.getString(R.string.exif), tmp);
        GeoLocation location;
        if ((location = metadata.getLocation()) != null)
            details.put(context.getString(R.string.location), location.toDMSString());

        return details;
    }
    public long getSize() {
        return size;
    }

    public GeoLocation getGeoLocation()  {
        return metadata.getLocation();
    }

    public boolean setOrientation(int orientation) {
        // TODO: 06/08/16 implement this method
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
    
    public void removeLocationData() {
        ExifInterface exif = null;
        try { 
            exif = new ExifInterface(getPath()); 
        } catch (IOException ex) { 
            return; 
        }  
        
        if (exit == null) return;
        
        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "0/0");
        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "0");
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "0/0,0/0000,00000000/00000");
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "0");)
        exif.setAttribute((ExifInterface.TAG_GPS_LONGITUDE, "0/0,0/0,000000/00000 ");
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "0");
        try {  
            exif.saveAttributes(); }
        catch (IOException e) {
            // cant save
        }
    }

    private boolean hasPath() {
        return path != null;
    }

    private boolean hasUri() {
        return uri != null;
    }


    private long getDateTaken() {
        /*if (metadataMap.containsKey(TAG_DATE_TAKEN))
            try { return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(metadataMap.getValue(TAG_DATE_TAKEN).toString()).getTime(); }
            catch (ParseException e) { return -1; }
            catch (NullPointerException e) { return -1; }
        return -1;*/
        // TODO: 16/08/16 improved
        Date dateOriginal = metadata.getDateOriginal();
        if (dateOriginal != null) return metadata.getDateOriginal().getTime();
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


    private String getHumanReadableSize() {
        return StringUtils.humanReadableByteCount(size, true);
    }

    public String getPath() {
        return path;
    }

    private String getDisplayName() {
        return  hasPath() ? getPath() : getUri().getEncodedPath();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.dateModified);
        dest.writeString(this.mime);
        dest.writeLong(this.id);
        dest.writeLong(this.size);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    protected Media(Parcel in) {
        this.path = in.readString();
        this.dateModified = in.readLong();
        this.mime = in.readString();
        this.id = in.readLong();
        this.size = in.readLong();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
