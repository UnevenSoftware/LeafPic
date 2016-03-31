package com.leafpic.app.Base;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by dnld on 12/11/15.
 */
public class Album implements Parcelable {


    public String ID;
    public String DisplayName;
    public String Path = "";

    ArrayList<Media> medias;
    private int[] count = new int[2];
    private boolean hidden = false;
    private boolean selected = false;
    private String coverPath = null;

    public Album(String id, String name, int[] count) {
        ID = id;
        DisplayName = name;
        this.count = count;
    }

    public String getContentDescdription(Context c){
        if (count[0]>0 && count[1]==0) return  count[0]== 1 ? c.getString(R.string.singular_photo) : c.getString(R.string.plural_photos);
        else if (count[0]==0 && count[1]>0) return c.getString(R.string.video);
        else return c.getString(R.string.media);
    }

    public Album(String ID) {
        this.ID = ID;
    }

    public Album(String path, String displayName, boolean hidden, int count) {
        medias = new ArrayList<Media>();
        DisplayName = displayName;
        Path = path;
        setHidden(hidden);
        this.count[0] = count;
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
        if (hasCustomCover()) return coverPath;
        if (medias.size() > 0) return "file://" + medias.get(0).Path;
        else return "drawable://" + R.drawable.ic_empty;
    }

    public void setPath() {
        try {
            Path = StringUtils.getBucketPathbyImagePath(medias.get(0).Path);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCustomCover() {
        return coverPath != null;
    }

    public Media getCoverAlbum() {
        if (coverPath != null) {
            return new Media(coverPath);
        }
        if (medias.size() > 0) return medias.get(0);
        return new Media("drawable://" + R.drawable.ic_empty);
    }

    public void setCoverPath(String path) {
        coverPath = path;
    }

    public int getImagesCount() {
        return count[0]+count[1];
    }


    protected Album(Parcel in) {
        ID = in.readString();
        DisplayName = in.readString();
        Path = in.readString();
        if (in.readByte() == 0x01) {
            medias = new ArrayList<Media>();
            in.readList(medias, Media.class.getClassLoader());
        } else {
            medias = null;
        }
        hidden = in.readByte() != 0x00;
        selected = in.readByte() != 0x00;
        coverPath = in.readString();
        count[0] = in.readInt();
        count[1] = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(DisplayName);
        dest.writeString(Path);
        if (medias == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(medias);
        }
        dest.writeByte((byte) (hidden ? 0x01 : 0x00));
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
        dest.writeString(coverPath);
        dest.writeInt(count[0]);
        dest.writeInt(count[1]);
    }

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
}