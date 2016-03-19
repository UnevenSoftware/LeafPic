package com.leafpic.app.Base;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings {

    public String coverPath;
    public String columnSortingMode;
    public Boolean ascending;
    String columnCount;

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
        if (ascending)
            return columnSortingMode + " ASC";
        else
            return columnSortingMode + " DESC";
    }
}
