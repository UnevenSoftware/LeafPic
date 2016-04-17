package com.leafpic.app.Base;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.leafpic.app.utils.StringUtils;

public class Media implements Parcelable {

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

    public long ID;
    public String Path = null;
    public String MIME = null;
    public long DateTaken = -1;
    public String FolderPath = null;
    public long DateModified = -1;
    public int orientation = 0;
    public int width;
    public int height;
    public long size;
    boolean selected = false;

    public Media(String path) {
        Path = path;
    }

    public Media(long id, String path, long dateTaken, long dateModified, String mime, int width, int height, int size, int orientation) {
        ID = id;
        Path = path;
        DateTaken = dateTaken;
        MIME = mime;
        DateModified = dateModified;
        this.width = width;
        this.height=height;
        this.size = size;
        this.orientation = orientation;
    }

    protected Media(Parcel in) {
        ID = in.readLong();
        Path = in.readString();
        MIME = in.readString();
        DateTaken = in.readLong();
        FolderPath = in.readString();
        DateModified = in.readLong();
        width = in.readInt();
        height = in.readInt();
        size = in.readLong();
        selected = in.readByte() != 0x00;
        orientation = in.readInt();
    }

    public Uri getUri() {
        return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), ID);
    }

    public boolean isGif() {
        return (MIME != null && MIME.equals("image/gif"));
    }

    public boolean isVideo() {
        return (MIME != null && MIME.startsWith("video/"));
    }

    public boolean isImage() {
        return (MIME != null && MIME.startsWith("image/"));
    }

    public String getResolution() {
        return width + "x" + height;
    }

    public String getHumanReadableSize() {
        return StringUtils.humanReadableByteCount(size, true);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean val) {
        selected = val;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(ID);
        dest.writeString(Path);
        dest.writeString(MIME);
        dest.writeLong(DateTaken);
        dest.writeString(FolderPath);
        dest.writeLong(DateModified);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeLong(size);
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
        dest.writeInt(orientation);
    }
}