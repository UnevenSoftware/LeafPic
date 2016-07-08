package com.horaapps.leafpic.Base;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings implements Serializable{

    public final static int SORT_BY_NAME = 3;
    public final static int SORT_BY_DATE = 0;
    public final static int SORT_BY_SIZE = 1;
    public final static int SORT_BY_TYPE = 2;

    String coverPath;
    public int columnSortingMode;
    public boolean ascending;

    AlbumSettings(String cover, int SortingMode, boolean asce) {
        coverPath = cover;
        columnSortingMode = SortingMode;
        ascending = asce;
    }

    AlbumSettings() {
        coverPath = null;
        columnSortingMode = 0;
        ascending = false;
    }
}