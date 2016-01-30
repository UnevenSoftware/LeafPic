package com.leafpic.app;

import android.os.Parcel;
import android.os.Parcelable;

public class Photo implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public String Path;
    public String MIME;
    public String DateTaken;
    public String FolderPath;
    boolean selected = false;

    public Photo(String path) {
        Path = path;
    }

    public Photo(String path, String dateTaken) {
        Path = path;
        DateTaken = dateTaken;
    }

    public Photo(String path, String dateTaken, String mime) {
        Path = path;
        DateTaken = dateTaken;
        MIME = mime;
    }

    public Photo(String path, String dateTaken, String mime, String folderPath) {
        Path = path;
        DateTaken = dateTaken;
        MIME = mime;
        FolderPath = folderPath;
    }

    protected Photo(Parcel in) {
        Path = in.readString();
        DateTaken = in.readString();
        MIME = in.readString();
        FolderPath = in.readString();
        selected = in.readByte() != 0x00;
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
        dest.writeString(Path);
        dest.writeString(DateTaken);
        dest.writeString(MIME);
        dest.writeString(FolderPath);
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }
}