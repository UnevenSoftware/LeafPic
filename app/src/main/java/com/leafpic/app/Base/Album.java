package com.leafpic.app.Base;

import android.os.Parcel;
import android.os.Parcelable;
import com.leafpic.app.R;
import com.leafpic.app.utils.string;

import java.util.ArrayList;

/**
 * Created by dnld on 12/11/15.
 */
public class Album implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    public String ID;
    public String DisplayName;
    public String Path = "";

    ArrayList<Photo> photos;
    private int imagesCount = 0;
    private boolean hidden = false;
    private boolean selected = false;

    public Album(String id, String name, int count) {
        ID = id;
        DisplayName = name;
        imagesCount = count;
    }


    public Album(String path, String displayName, boolean hidden, int count) {
        photos = new ArrayList<Photo>();
        DisplayName = displayName;
        Path = path;
        setHidden(hidden);
        imagesCount = count;
    }

    protected Album(Parcel in) {
        ID = in.readString();
        DisplayName = in.readString();
        imagesCount = in.readInt();
        Path = in.readString();
        if (in.readByte() == 0x01) {
            photos = new ArrayList<Photo>();
            in.readList(photos, Photo.class.getClassLoader());
        } else {
            photos = null;
        }
        hidden = in.readByte() != 0x00;
        selected = in.readByte() != 0x00;
    }

    public void setPath() {
        try {
            Path = string.getBucketPathbyImagePath(photos.get(0).Path);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean value) {
        hidden = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelcted(boolean value) {
        selected = value;
    }

    public String getPathCoverAlbum() {
        if (photos.size() > 0) return "file://" + photos.get(0).Path;
        else return "drawable://" + R.drawable.ic_empty;
    }

    public int getImagesCount() {
        return imagesCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(DisplayName);
        dest.writeInt(imagesCount);
        dest.writeString(Path);
        if (photos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(photos);
        }
        dest.writeByte((byte) (hidden ? 0x01 : 0x00));
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }
}