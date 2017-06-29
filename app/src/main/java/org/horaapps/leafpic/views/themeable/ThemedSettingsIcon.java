package org.horaapps.leafpic.views.themeable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.activities.theme.ThemeHelper;
import org.horaapps.leafpic.activities.theme.Themed;

/**
 * Created by darken (darken@darken.eu) on 04.03.2017.
 */
public class ThemedSettingsIcon extends IconicsImageView implements Themed {
    public ThemedSettingsIcon(Context context) {
        this(context, null);
    }

    public ThemedSettingsIcon(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemedSettingsIcon(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        setColor(themeHelper.getIconColor());
    }
}
