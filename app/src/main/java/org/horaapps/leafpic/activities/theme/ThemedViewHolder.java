package org.horaapps.leafpic.activities.theme;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by dnld on 08/04/17.
 */

public abstract class ThemedViewHolder extends RecyclerView.ViewHolder implements Themed {

    private static final  ButterKnife.Setter<View, ThemeHelper> THEME_ACTION = (view, themeHelper, index) -> {
        if (view instanceof Themed) ((Themed) view).refreshTheme(themeHelper);
    };

    public ThemedViewHolder(View view) {
        super(view);
    }

    public static void themeViews(ThemeHelper themeHelper, View ... views) {
        ButterKnife.apply(views, THEME_ACTION, themeHelper);
    }
}