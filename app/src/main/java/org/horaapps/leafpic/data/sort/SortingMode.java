package org.horaapps.leafpic.data.sort;

import android.provider.MediaStore;

/**
 * Created by dnld on 18/08/16.
 */
public enum SortingMode {

    NAME(0, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME), DATE(1, MediaStore.MediaColumns.DATE_MODIFIED, "max(" + MediaStore.Images.Media.DATE_MODIFIED + ")"), SIZE(2, MediaStore.MediaColumns.SIZE, "count(*)"), TYPE(3, MediaStore.MediaColumns.MIME_TYPE), NUMERIC(4, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

    int value;

    String mediaColumn;

    String albumsColumn;

    SortingMode(int value, String mediaColumn) {
        this.value = value;
        this.mediaColumn = mediaColumn;
        this.albumsColumn = mediaColumn;
    }

    SortingMode(int value, String mediaColumn, String albumsColumn) {
        this.value = value;
        this.mediaColumn = mediaColumn;
        this.albumsColumn = albumsColumn;
    }

    public String getMediaColumn() {
        return mediaColumn;
    }

    public String getAlbumsColumn() {
        return albumsColumn;
    }

    public int getValue() {
        return value;
    }

    public static SortingMode fromValue(int value) {
        switch(value) {
            case 0:
                return NAME;
            case 1:
            default:
                return DATE;
            case 2:
                return SIZE;
            case 3:
                return TYPE;
            case 4:
                return NUMERIC;
        }
    }
}
