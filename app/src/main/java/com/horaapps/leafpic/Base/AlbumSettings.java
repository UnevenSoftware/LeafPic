package com.horaapps.leafpic.Base;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings implements Parcelable {

    public String coverPath;
    public String columnSortingMode;
    public Boolean ascending;

    public AlbumSettings(String cover, String SortingMode, Boolean asce) {
        coverPath = cover;
        columnSortingMode = SortingMode;
        ascending = asce;
    }

    public AlbumSettings() {
        coverPath = null;
        columnSortingMode = null;
        ascending = false;
    }

    public String getSQLSortingMode() {
        columnSortingMode = columnSortingMode != null ? columnSortingMode : MediaStore.Images.ImageColumns.DATE_TAKEN;
        return columnSortingMode + (ascending ? " ASC" : " DESC");
    }

    protected AlbumSettings(Parcel in) {
        coverPath = in.readString();
        columnSortingMode = in.readString();
        byte ascendingVal = in.readByte();
        ascending = ascendingVal == 0x02 ? null : ascendingVal != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(coverPath);
        dest.writeString(columnSortingMode);
        if (ascending == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (ascending ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AlbumSettings> CREATOR = new Parcelable.Creator<AlbumSettings>() {
        @Override
        public AlbumSettings createFromParcel(Parcel in) {
            return new AlbumSettings(in);
        }

        @Override
        public AlbumSettings[] newArray(int size) {
            return new AlbumSettings[size];
        }
    };
}