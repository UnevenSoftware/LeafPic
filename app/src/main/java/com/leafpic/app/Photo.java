package com.leafpic.app;

import android.os.Parcel;
import android.os.Parcelable;
import com.leafpic.app.utils.string;

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
    String ID;
    String FolderPath;
    String DateTaken;

    public Photo(String path) {
        Path = path;
        FolderPath = string.getBucketPathbyImagePath(path);
    }

    public Photo(String path, String dateTaken) {
        Path = path;
        FolderPath = string.getBucketPathbyImagePath(path);
        DateTaken = dateTaken;
    }

    /*public Photo(String ID, String folderName, String path) {
        this.ID = ID;
        FolderPath = folderName;
        Path = path;
        //addBitmapToMemoryCache();
    }*/

    protected Photo(Parcel in) {
        Path = in.readString();
        ID = in.readString();
        FolderPath = in.readString();
        DateTaken = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Path);
        dest.writeString(ID);
        dest.writeString(FolderPath);
        dest.writeString(DateTaken);
    }
}