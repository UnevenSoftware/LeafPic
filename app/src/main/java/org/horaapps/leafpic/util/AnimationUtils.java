package org.horaapps.leafpic.util;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;

import org.horaapps.leafpic.util.preferences.Prefs;

/**
 * Created by dnld on 24/02/18.
 */

public class AnimationUtils {
    public static RecyclerView.ItemAnimator getItemAnimator(RecyclerView.ItemAnimator itemAnimator) {
        if(Prefs.animationsEnabled()) {
            return itemAnimator;
        }
        return null;
    }

    public static ViewPager.PageTransformer getPageTransformer(ViewPager.PageTransformer pageTransformer) {
        if(Prefs.animationsEnabled()) {
            return pageTransformer;
        }
        return null;
    }
}
