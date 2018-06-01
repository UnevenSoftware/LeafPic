package org.horaapps.leafpic.data;

import android.graphics.Bitmap;

import com.zomato.photofilters.imageprocessors.Filter;

/**
 * @author Temidayo on 05/30/18.
 */
public class ThumbnailItem {
    public Bitmap image;
    public Filter filter;

    public ThumbnailItem() {
        image = null;
        filter = new Filter();
    }
}
