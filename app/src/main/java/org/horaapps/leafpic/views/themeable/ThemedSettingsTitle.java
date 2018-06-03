package org.horaapps.leafpic.views.themeable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;

/**
 * Created by darken (darken@darken.eu) on 04.03.2017.
 */
public class ThemedSettingsTitle extends android.support.v7.widget.AppCompatTextView implements Themed {

    public ThemedSettingsTitle(Context context) {
        this(context, null);
    }

    public ThemedSettingsTitle(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemedSettingsTitle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        setTextColor(themeHelper.getTextColor());
    }
}
