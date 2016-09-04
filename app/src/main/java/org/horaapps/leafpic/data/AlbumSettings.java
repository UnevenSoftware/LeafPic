package org.horaapps.leafpic.data;

import android.content.Context;

import org.horaapps.leafpic.data.base.FilterMode;
import org.horaapps.leafpic.data.base.SortingMode;
import org.horaapps.leafpic.data.base.SortingOrder;

import java.io.Serializable;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings implements Serializable {

    private String path;
    private long id;
    private String coverPath;
    private int sortingMode;
    private int sortingOrder;

    private FilterMode filterMode = FilterMode.ALL;

    static AlbumSettings getSettings(Context context, Album album) {
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        return h.getSettings(album.getPath(), album.getId());
    }

    AlbumSettings(String path, long id, String cover, int sortingMode, int sortingOrder) {
        this.id = id;
        this.path = path;
        this.coverPath = cover;
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
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
        h.setAlbumSortingMode(path, id, sortingMode.getValue());
    }

    void changeSortingOrder(Context context, SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder.getValue();
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumSortingOrder(path, id, sortingOrder.getValue());
    }

    void changeCoverPath(Context context, String coverPath) {
        this.coverPath = coverPath;
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        if (coverPath != null)
            h.setAlbumPhotoPreview(path, id, coverPath);
        else h.clearAlbumPreview(path, id);
    }
}