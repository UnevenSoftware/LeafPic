package org.horaapps.leafpic.activities.theme;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import org.horaapps.leafpic.util.ViewUtil;

/**
 * Created by dnld on 4/16/17.
 */

public class ThemedAlertDialogBuilder extends AlertDialog.Builder {

    private ThemeHelper themeHelper;

    public ThemedAlertDialogBuilder(Context context, ThemeHelper themeHelper) {
        super(context, themeHelper.getDialogStyle());
        this.themeHelper = themeHelper;
    }

    @Override
    public AlertDialog.Builder setView(View view) {
        for (View v : ViewUtil.getAllChildren(view))
            if (v instanceof Themed) ((Themed) v).refreshTheme(themeHelper);
        return super.setView(view);
    }
}
