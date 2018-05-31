package org.horaapps.leafpic.util;

import android.content.Context;
import android.graphics.Bitmap;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.ThumbnailItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Temidayo on 05/30/18.
 */
public final class ThumbnailsManager {
    private static List<ThumbnailItem> filterThumbs = new ArrayList<>(10);
    private static List<ThumbnailItem> processedThumbs = new ArrayList<>(10);

    private ThumbnailsManager() {
    }

    public static void addThumb(ThumbnailItem thumbnailItem) {
        filterThumbs.add(thumbnailItem);
    }

    public static List<ThumbnailItem> processThumbs(Context context) {
        for (ThumbnailItem thumb : filterThumbs) {
            // scaling down the image
            float size = context.getResources().getDimension(R.dimen.thumbnail_size);
            thumb.image = Bitmap.createScaledBitmap(thumb.image, (int) size, (int) size, false);
            thumb.image = thumb.filter.processFilter(thumb.image);
            //cropping circle
            thumb.image = GeneralUtils.generateCircularBitmap(thumb.image);
            processedThumbs.add(thumb);
        }
        return processedThumbs;
    }

    public static void clearThumbs() {
        filterThumbs = new ArrayList<>();
        processedThumbs = new ArrayList<>();
    }
}
