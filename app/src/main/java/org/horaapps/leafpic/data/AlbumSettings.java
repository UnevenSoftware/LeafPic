package org.horaapps.leafpic.data;

import android.content.Context;
import android.support.annotation.Nullable;

import org.horaapps.leafpic.data.base.FilterMode;
import org.horaapps.leafpic.data.base.SortingMode;
import org.horaapps.leafpic.data.base.SortingOrder;

import java.io.Serializable;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings implements Serializable {

    private String path;
    private String coverPath;
    private int sortingMode;
    private int sortingOrder;
    private boolean pinned;

    private FilterMode filterMode = FilterMode.ALL;

    static AlbumSettings getSettings(Context context, Album album) {
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        return h.getSettings(album.getPath());
    }

    static AlbumSettings getDefaults() {
        return new AlbumSettings(null, null, SortingMode.DATE.getValue(), SortingOrder.DESCENDING.getValue(), 0);
    }


    AlbumSettings(String path, String cover, int sortingMode, int sortingOrder, int pinned) {
        this.path = path;
        this.coverPath = cover;
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        this.pinned = pinned == 1;
    }

    FilterMode getFilterMode() {
        return filterMode;
    }

    void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    String getCoverPath() {
        return coverPath;
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(sortingMode);
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(sortingOrder);
    }

    void changeSortingMode(Context context, SortingMode sortingMode) {
        this.sortingMode = sortingMode.getValue();
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumSortingMode(path, sortingMode.getValue());
    }

    void changeSortingOrder(Context context, SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder.getValue();
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumSortingOrder(path, sortingOrder.getValue());
    }

    void changeCoverPath(Context context, @Nullable String coverPath) {
        this.coverPath = coverPath;
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumPhotoPreview(path, coverPath);
    }

    boolean isPinned() {
        return pinned;
    }

    public void togglePin(Context context) {
        this.pinned = !pinned;
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.pinAlbum(path, pinned);
    }
}