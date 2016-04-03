package com.leafpic.app.Base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dnld on 03/04/16.
 */
public class AlbumMediaCount implements Parcelable {
    public int photos;
    public int videos;
    public int getTotal(){ return photos+videos; }

    public AlbumMediaCount(){ photos = videos = 0;}

    protected AlbumMediaCount(Parcel in) {
        photos = in.readInt();
        videos = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(photos);
        dest.writeInt(videos);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AlbumMediaCount> CREATOR = new Parcelable.Creator<AlbumMediaCount>() {
        @Override
        public AlbumMediaCount createFromParcel(Parcel in) {
            return new AlbumMediaCount(in);
        }

        @Override
        public AlbumMediaCount[] newArray(int size) {
            return new AlbumMediaCount[size];
        }
    };
}