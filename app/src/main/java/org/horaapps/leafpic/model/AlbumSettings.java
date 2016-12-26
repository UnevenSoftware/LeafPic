package org.horaapps.leafpic.model;

import android.content.Context;

import org.horaapps.leafpic.model.base.FilterMode;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;

import java.io.Serializable;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings implements Serializable {

    String coverPath;
    int sortingMode;
    int sortingOrder;
    boolean pinned;
    FilterMode filterMode = FilterMode.ALL;

    static AlbumSettings getSettings(Context context, Album album) {
        return HandlingAlbums.getInstance(context).getSettings(album.getPath());
    }

    public static AlbumSettings getDefaults() {
        return new AlbumSettings(null, SortingMode.DATE.getValue(), SortingOrder.DESCENDING.getValue(), 0);
    }

    AlbumSettings(String cover, int sortingMode, int sortingOrder, int pinned) {
        this.coverPath = cover;
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        this.pinned = pinned == 1;
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(sortingMode);
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(sortingOrder);
    }
}