package org.horaapps.leafpic.settings;

import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.horaapps.leafpic.R;

import horaapps.org.liz.ColorPalette;
import horaapps.org.liz.ThemedActivity;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

import static horaapps.org.liz.Theme.AMOLED;
import static horaapps.org.liz.Theme.DARK;
import static horaapps.org.liz.Theme.LIGHT;


/**
 * Created by dnld on 12/9/16.
 */

public class ColorsSetting extends ThemedSetting {

    public ColorsSetting(ThemedActivity activity) {
        super(activity);
    }

    public void chooseBaseTheme() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());

        final View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_base_theme, null);
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

    public interface ColorChooser {
        void onColorSelected(int color);

        void onDialogDismiss();

        void onColorChanged(int color);
    }

    public void chooseColor(@StringRes int title, final ColorChooser chooser, int defaultColor) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());

        View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_color_picker, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary);
        final LineColorPicker colorPicker2 = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary_2);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.dialog_title);
        dialogTitle.setText(title);
        ((CardView) dialogLayout.findViewById(R.id.dialog_card)).setCardBackgroundColor(getActivity().getCardBackgroundColor());

        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                dialogTitle.setBackgroundColor(c);
                chooser.onColorChanged(c);
            }
        });

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                colorPicker2.setColors(ColorPalette.getColors(getActivity(), colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });

        int[] baseColors = ColorPalette.getBaseColors(getActivity());
        colorPicker.setColors(baseColors);

        for (int i : baseColors) {
            for (int i2 : ColorPalette.getColors(getActivity(), i))
                if (i2 == defaultColor) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPalette.getColors(getActivity(), i));
                    colorPicker2.setSelectedColor(i2);
                    break;
                }
        }

        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNegativeButton(getActivity().getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                chooser.onDialogDismiss();
            }
        });

        dialogBuilder.setPositiveButton(getActivity().getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                alertDialog.setOnDismissListener(null);
                chooser.onColorSelected(colorPicker2.getColor());

            }
        });

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                chooser.onDialogDismiss();
            }
        });
        dialogBuilder.show();
    }

}
