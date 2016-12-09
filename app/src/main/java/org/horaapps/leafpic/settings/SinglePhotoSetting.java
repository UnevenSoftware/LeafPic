package org.horaapps.leafpic.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;

import uz.shift.colorpicker.LineColorPicker;

/**
 * Created by dnld on 12/9/16.
 */

public class SinglePhotoSetting extends ThemedSetting {

    public SinglePhotoSetting(ThemedActivity activity, PreferenceUtil SP) {
        super(activity, SP);
    }

    public void show() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());

        View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_media_viewer_theme, null);
        final SwitchCompat swApplyTheme = (SwitchCompat) dialogLayout.findViewById(R.id.sw_apply_theme);

        ((CardView) dialogLayout.findViewById(R.id.dialog_card_)).setCardBackgroundColor(getActivity().getCardBackgroundColor());
        dialogLayout.findViewById(R.id.dialog_title).setBackgroundColor(getActivity().getPrimaryColor());
        ((TextView) dialogLayout.findViewById(R.id.apply_theme)).setTextColor(getActivity().getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.apply_theme_3thAct_title_Sub)).setTextColor(getActivity().getSubTextColor());
        ((IconicsImageView) dialogLayout.findViewById(R.id.ll_apply_theme_icon)).setColor(getActivity().getIconColor());

        swApplyTheme.setChecked(getActivity().themeOnSingleImgAct());
        swApplyTheme.setClickable(false);
        dialogLayout.findViewById(R.id.ll_apply_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swApplyTheme.setChecked(!swApplyTheme.isChecked());
                getActivity().setSwitchColor(swApplyTheme, getActivity().getAccentColor());
            }
        });
        getActivity().setSwitchColor(swApplyTheme, getActivity().getAccentColor());

        final LineColorPicker transparencyColorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.pickerTransparent);
        transparencyColorPicker.setColors(ColorPalette.getTransparencyShadows(getActivity().getPrimaryColor()));
        transparencyColorPicker.setSelectedColor(ColorPalette.getTransparentColor(getActivity().getPrimaryColor(), getActivity().getTransparency()));

        /**TEXT VIEWS**/
        ((TextView) dialogLayout.findViewById(R.id.seek_bar_alpha_title)).setTextColor(getActivity().getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.seek_bar_alpha_title_Sub)).setTextColor(getActivity().getSubTextColor());

        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setNeutralButton(getActivity().getString(R.string.cancel).toUpperCase(), null);
        dialogBuilder.setPositiveButton(getActivity().getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSP().getEditor();
                boolean applyTheme = swApplyTheme.isChecked();
                editor.putBoolean(getActivity().getString(R.string.preference_apply_theme_pager), applyTheme);
                if (applyTheme) {
                    int c = Color.alpha(transparencyColorPicker.getColor());
                    editor.putInt(getActivity().getString(R.string.preference_transparency), 255 - c);
                }
                editor.commit();
                getActivity().updateTheme();
            }
        });
        dialogBuilder.show();
    }
}
