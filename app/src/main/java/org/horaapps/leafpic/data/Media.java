package org.horaapps.leafpic.data;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import com.bumptech.glide.signature.ObjectKey;
import com.drew.lang.GeoLocation;
import com.drew.lang.annotations.NotNull;
import org.horaapps.leafpic.timeline.data.TimelineItem;
import org.horaapps.leafpic.util.ArrayUtils;
import org.horaapps.leafpic.util.MimeTypeUtils;
import org.horaapps.leafpic.util.StringUtils;
import java.io.File;
import java.io.IOException;

// TODO Calvin: Separate out the logic here
/**
 * Ideally, we should have separate data classes for images, videos & gifs
 *  Base class can be Media, and others should extend
 *  Try to separate out Database logic and projections from this class
 */
public class Media implements TimelineItem, CursorHandler, Parcelable {

    private static final String[] sProjection = new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.ORIENTATION };

    private static final int CURSOR_POS_DATA = ArrayUtils.getIndex(sProjection, MediaStore.Images.Media.DATA);

    private static final int CURSOR_POS_DATE_TAKEN = ArrayUtils.getIndex(sProjection, MediaStore.Images.Media.DATE_TAKEN);

    private static final int CURSOR_POS_MIME_TYPE = ArrayUtils.getIndex(sProjection, MediaStore.Images.Media.MIME_TYPE);

    private static final int CURSOR_POS_SIZE = ArrayUtils.getIndex(sProjection, MediaStore.Images.Media.SIZE);

    private static final int CURSOR_POS_ORIENTATION = ArrayUtils.getIndex(sProjection, MediaStore.Images.Media.ORIENTATION);

    private String path = null;

    private long dateModified = -1;

    private String mimeType = MimeTypeUtils.UNKNOWN_MIME_TYPE;

    private int orientation = 0;

    private String uriString = null;

    private long size = -1;

    private boolean selected = false;

    public Media() {
    }

    public Media(String path, long dateModified) {
        this.path = path;
        this.dateModified = dateModified;
        this.mimeType = MimeTypeUtils.getMimeType(path);
    }

    public Media(File file) {
        this(file.getPath(), file.lastModified());
        this.size = file.length();
        this.mimeType = MimeTypeUtils.getMimeType(path);
    }

    public Media(String path) {
        this(path, -1);
    }

    public Media(Uri mediaUri) {
        this.uriString = mediaUri.toString();
        this.path = null;
        this.mimeType = MimeTypeUtils.getMimeType(uriString);
    }

    public Media(@NotNull Cursor cur) {
        this.path = cur.getString(CURSOR_POS_DATA);
        this.dateModified = cur.getLong(CURSOR_POS_DATE_TAKEN);
        this.mimeType = cur.getString(CURSOR_POS_MIME_TYPE);
        this.size = cur.getLong(CURSOR_POS_SIZE);
        this.orientation = cur.getInt(CURSOR_POS_ORIENTATION);
    }

    @Override
    public Media handle(Cursor cu) {
        return new Media(cu);
    }

    public static String[] getProjection() {
        return sProjection;
    }

    public void setUri(String uriString) {
        this.uriString = uriString;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    // TODO Calvin: Do not store adapter selection in a Media data class.
    // This does not belong here.
    public boolean isSelected() {
        return selected;
    }

    public boolean setSelected(boolean selected) {
        if (this.selected == selected)
            return false;
        this.selected = selected;
        return true;
    }

    public boolean toggleSelected() {
        selected = !selected;
        return selected;
    }

    public boolean isGif() {
        return mimeType.endsWith("gif");
    }

    public boolean isImage() {
        return mimeType.startsWith("image");
    }

    public boolean isVideo() {
        return mimeType.startsWith("video");
    }

    public Uri getUri() {
        return uriString != null ? Uri.parse(uriString) : Uri.fromFile(new File(path));
    }

    public String getDisplayPath() {
        return path != null ? path : getUri().getEncodedPath();
    }

    public String getName() {
        return StringUtils.getPhotoNameByPath(path);
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public Long getDateModified() {
        return dateModified;
    }

    public ObjectKey getSignature() {
        return new ObjectKey(getDateModified() + getPath() + getOrientation());
    }

    public int getOrientation() {
        return orientation;
    }

    //<editor-fold desc="Exif & More">
    // TODO remove from here!
    @Deprecated
    public Bitmap getBitmap() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        return bitmap;
    }

    @Deprecated
    public GeoLocation getGeoLocation() {
        return /*metadata != null ? metadata.getLocation() :*/
        null;
    }

    @Deprecated
    public boolean setOrientation(final int orientation) {
        this.orientation = orientation;
        // TODO: 28/08/16  find a better way
        // TODO update also content provider
        new Thread(new Runnable() {

            public void run() {
                int exifOrientation = -1;
                try {
                    ExifInterface exif = new ExifInterface(path);
                    switch(orientation) {
                        case 90:
                            exifOrientation = ExifInterface.ORIENTATION_ROTATE_90;
                            break;
                        case 180:
                            exifOrientation = ExifInterface.ORIENTATION_ROTATE_180;
                            break;
                        case 270:
                            exifOrientation = ExifInterface.ORIENTATION_ROTATE_270;
                            break;
                        case 0:
                            exifOrientation = ExifInterface.ORIENTATION_NORMAL;
                            break;
                    }
                    if (exifOrientation != -1) {
                        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(exifOrientation));
                        exif.saveAttributes();
                    }
                } catch (IOException ignored) {
                }
            }
        }).start();
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Media)
            return getPath().equals(((Media) obj).getPath());
        return super.equals(obj);
    }

    @Deprecated
    private long getDateTaken() {
        /*// TODO: 16/08/16 improved
        Date dateOriginal = metadata.getDateOriginal();
        if (dateOriginal != null) return metadata.getDateOriginal().getTime();
        return -1;*/
        return 1;
    }

    @Deprecated
    public boolean fixDate() {
        long newDate = getDateTaken();
        if (newDate != -1) {
            File f = new File(path);
            if (f.setLastModified(newDate)) {
                dateModified = newDate;
                return true;
            }
        }
        return false;
    }

    //</editor-fold>
    public File getFile() {
        if (path != null) {
            File file = new File(path);
            if (file.exists())
                return file;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.dateModified);
        dest.writeString(this.mimeType);
        dest.writeInt(this.orientation);
        dest.writeString(this.uriString);
        dest.writeLong(this.size);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    protected Media(Parcel in) {
        this.path = in.readString();
        this.dateModified = in.readLong();
        this.mimeType = in.readString();
        this.orientation = in.readInt();
        this.uriString = in.readString();
        this.size = in.readLong();
        this.selected = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {

        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    @Override
    public int getTimelineType() {
        return TYPE_MEDIA;
    }
}
