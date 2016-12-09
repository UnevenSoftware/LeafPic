package org.horaapps.leafpic.settings;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.SettingsActivity;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

import static org.horaapps.leafpic.util.Theme.AMOLED;
import static org.horaapps.leafpic.util.Theme.DARK;
import static org.horaapps.leafpic.util.Theme.LIGHT;

/**
 * Created by dnld on 12/9/16.
 */

public class ColorsSetting extends ThemedSetting {

    public ColorsSetting(ThemedActivity activity, PreferenceUtil SP) {
        super(activity, SP);
    }

    public ColorsSetting(PreferenceUtil SP) {
        super(SP);
    }

    public void choseBaseTheme() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());

        final View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_basic_theme, null);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.basic_theme_title);
        final CardView dialogCardView = (CardView) dialogLayout.findViewById(R.id.basic_theme_card);

        /** SET OBJ THEME **/
        dialogTitle.setBackgroundColor(getActivity().getPrimaryColor());
        dialogCardView.setCardBackgroundColor(getActivity().getCardBackgroundColor());
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setView(dialogLayout);
        final AlertDialog dialog = dialogBuilder.show();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ll_white_basic_theme:
                        getActivity().setBaseTheme(LIGHT);
                        break;
                    case R.id.ll_dark_basic_theme:
                        getActivity().setBaseTheme(DARK);
                        break;
                    case R.id.ll_dark_amoled_basic_theme:
                        getActivity().setBaseTheme(AMOLED);
                        break;
                }
                getActivity().updateUiElements();
                dialog.dismiss();
            }
        };
        dialogLayout.findViewById(R.id.ll_white_basic_theme).setOnClickListener(listener);
        dialogLayout.findViewById(R.id.ll_dark_basic_theme).setOnClickListener(listener);
        dialogLayout.findViewById(R.id.ll_dark_amoled_basic_theme).setOnClickListener(listener);
    }

    public void chosePrimaryColor(final  SettingsActivity activity) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());

        final View dialogLayout = LayoutInflater.from(activity).inflate(R.layout.color_piker_primary, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary);
        final LineColorPicker colorPicker2 = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary_2);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.cp_primary_title);
        CardView dialogCardView = (CardView) dialogLayout.findViewById(R.id.cp_primary_card);
        dialogCardView.setCardBackgroundColor(activity.getCardBackgroundColor());

        colorPicker.setColors(ColorPalette.getBaseColors(activity));
        for (int i : colorPicker.getColors())
            for (int i2 : ColorPalette.getColors(activity, i))
                if (i2 == activity.getPrimaryColor()) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPalette.getColors(activity, i));
                    colorPicker2.setSelectedColor(i2);
                    break;
            }

        dialogTitle.setBackgroundColor(activity.getPrimaryColor());

        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                activity.updateViewsWithPrimaryColor(c);
                dialogTitle.setBackgroundColor(c);
            }
        });

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                colorPicker2.setColors(ColorPalette.getColors(activity, colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });

        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNeutralButton(activity.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                activity.updateViewsWithPrimaryColor(activity.getPrimaryColor());
                dialog.cancel();
            }
        });

        dialogBuilder.setPositiveButton(activity.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getSP().putInt(activity.getString(R.string.preference_primary_color), colorPicker2.getColor());
                activity.updateTheme();
                activity.updateViewsWithPrimaryColor(activity.getPrimaryColor());
            }
        });

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.updateViewsWithPrimaryColor(activity.getPrimaryColor());
            }
        });
        dialogBuilder.show();
    }

    public void choseAccentColor(final SettingsActivity activity) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());

        final View dialogLayout = LayoutInflater.from(activity).inflate(R.layout.color_piker_accent, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_accent);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.cp_accent_title);
        CardView cv = (CardView) dialogLayout.findViewById(R.id.cp_accent_card);
        cv.setCardBackgroundColor(activity.getCardBackgroundColor());

        colorPicker.setColors(ColorPalette.getAccentColors(activity.getApplicationContext()));
        colorPicker.setSelectedColor(activity.getAccentColor());
        dialogTitle.setBackgroundColor(activity.getAccentColor());

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                dialogTitle.setBackgroundColor(c);
                // TODO: 12/9/16 callback
                activity.updateViewsWithAccentColor(colorPicker.getColor());

            }
        });
        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNeutralButton(activity.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                activity.updateViewsWithAccentColor(activity.getAccentColor());
            }
        });
        dialogBuilder.setPositiveButton(activity.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getSP().putInt(activity.getString(R.string.preference_accent_color), colorPicker.getColor());
                activity.updateTheme();
                activity.updateViewsWithAccentColor(activity.getAccentColor());
            }
        });
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.updateViewsWithAccentColor(activity.getAccentColor());
            }
        });
        dialogBuilder.show();
    }
}
