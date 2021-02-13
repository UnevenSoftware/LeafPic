package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.orhanobut.hawk.Hawk;
import org.horaapps.leafpic.R;


/**
 * This class is used to create a recyclerview that has a scrollbar that matches the color of the chosen accent color.
 * The need for this class arises because the native method of setting a recyclerview scrollbar color is set to a drawable,
 * that doesn't have a dynamic way to change colors.
 *
 * Edited by Bharadwaj Giridhar on April 9,2020
 **/

public class CustomRecyclerView extends RecyclerView {

    private int scrollBarColor = Hawk.get(getResources().getString(R.string.preference_accent_color));

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScrollBarColor(@ColorInt int scrollBarColor) {
        this.scrollBarColor = scrollBarColor;
    }

    /**
     * Called by Android {@link android.view.View#onDrawScrollBars(Canvas)}
     **/
    protected void onDrawHorizontalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP);
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /**
     * Called by Android {@link android.view.View#onDrawScrollBars(Canvas)}
     **/
    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP);
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }
}
