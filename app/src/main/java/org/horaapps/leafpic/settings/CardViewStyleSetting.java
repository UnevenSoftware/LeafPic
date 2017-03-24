package org.horaapps.leafpic.settings;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.Theme;

import static org.horaapps.leafpic.util.CardViewStyle.COMPACT;
import static org.horaapps.leafpic.util.CardViewStyle.FLAT;
import static org.horaapps.leafpic.util.CardViewStyle.MATERIAL;

/**
 * Created by dnld on 12/9/16.
 */

public class CardViewStyleSetting extends ThemedSetting {

    public CardViewStyleSetting(ThemedActivity activity, PreferenceUtil SP) {
        super(activity, SP);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());
        final View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_select_cardview_style, null);

        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.dialog_card_view_style_title);
        ((CardView) dialogLayout.findViewById(R.id.dialog_card_view_style)).setCardBackgroundColor(getActivity().getCardBackgroundColor());
        dialogTitle.setBackgroundColor(getActivity().getPrimaryColor());

        final RadioGroup rGroup = (RadioGroup) dialogLayout.findViewById(R.id.radio_group_card_view_style);
        final CheckBox chkShowNPhots = (CheckBox) dialogLayout.findViewById(R.id.show_n_photos);
        RadioButton rCompact = (RadioButton) dialogLayout.findViewById(R.id.radio_card_compact);
        RadioButton rFlat = (RadioButton) dialogLayout.findViewById(R.id.radio_card_flat);
        RadioButton rMaterial = (RadioButton) dialogLayout.findViewById(R.id.radio_card_material);

        chkShowNPhots.setChecked(getSP().getBoolean("show_n_photos", true));

        getActivity().themeRadioButton(rCompact);
        getActivity().themeRadioButton(rFlat);
        getActivity().themeRadioButton(rMaterial);
        getActivity().themeCheckBox(chkShowNPhots);

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                final View v;
                switch (i) {
                    case R.id.radio_card_compact:
                        v = LayoutInflater.from(getActivity()).inflate(COMPACT.getLayout(), null);
                        v.findViewById(R.id.linear_card_text).setBackgroundColor(ColorPalette.getTransparentColor(getActivity().getBackgroundColor(), 150));
                        break;
                    case R.id.radio_card_flat:
                        v = LayoutInflater.from(getActivity()).inflate(FLAT.getLayout(), null);
                        v.findViewById(R.id.linear_card_text).setBackgroundColor(ColorPalette.getTransparentColor(getActivity().getBackgroundColor(), 150));
                        break;
                    case R.id.radio_card_material: default:
                        v = LayoutInflater.from(getActivity()).inflate(MATERIAL.getLayout(), null);
                        v.findViewById(R.id.linear_card_text).setBackgroundColor(getActivity().getCardBackgroundColor());
                        break;
                }

                ImageView img = (ImageView) v.findViewById(org.horaapps.leafpic.R.id.album_preview);
                img.setBackgroundColor(getActivity().getPrimaryColor());

                Glide.with(getActivity())
                        .load(R.drawable.donald_header)
                        .into(img);

                String hexPrimaryColor = ColorPalette.getHexColor(getActivity().getPrimaryColor());
                String hexAccentColor = ColorPalette.getHexColor(getActivity().getAccentColor());

                if (hexAccentColor.equals(hexPrimaryColor))
                    hexAccentColor = ColorPalette.getHexColor(ColorPalette.getDarkerColor(getActivity().getAccentColor()));

                String textColor = getActivity().getBaseTheme().equals(Theme.LIGHT) ? "#2B2B2B" : "#FAFAFA";
                String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>420</font></b>";

                ((TextView) v.findViewById(R.id.album_media_count)).setText(StringUtils.html(albumPhotoCountHtml));
                ((TextView) v.findViewById(R.id.album_media_label)).setTextColor(getActivity().getTextColor());
                v.findViewById(R.id.ll_n_media).setVisibility( chkShowNPhots.isChecked() ? View.VISIBLE : View.GONE);

                chkShowNPhots.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        v.findViewById(R.id.ll_n_media).setVisibility(b ? View.VISIBLE : View.GONE);
                    }
                });

                ((TextView) v.findViewById(R.id.album_name)).setText(StringUtils.html("<i><font color='" + textColor + "'>PraiseDuarte</font></i>"));

                ((CardView) v).setUseCompatPadding(true);
                ((CardView) v).setRadius(2);

                ((LinearLayout) dialogLayout.findViewById(R.id.ll_preview_album_card)).removeAllViews();
                ((LinearLayout) dialogLayout.findViewById(R.id.ll_preview_album_card)).addView(v);
            }
        });

        switch (CardViewStyle.fromValue(getSP().getInt("card_view_style", 0))) {
            case COMPACT: rCompact.setChecked(true); break;
            case FLAT: rFlat.setChecked(true); break;
            case MATERIAL: default: rMaterial.setChecked(true); break;
        }

        builder.setNegativeButton(getActivity().getString(R.string.cancel).toUpperCase(), null);
        builder.setPositiveButton(getActivity().getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (rGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_card_material:
                    default: getSP().putInt("card_view_style", CardViewStyle.MATERIAL.getValue()); break;
                    case R.id.radio_card_flat: getSP().putInt("card_view_style", CardViewStyle.FLAT.getValue()); break;
                    case R.id.radio_card_compact: getSP().putInt("card_view_style", CardViewStyle.COMPACT.getValue()); break;
                }
                getSP().putBoolean("show_n_photos", chkShowNPhots.isChecked());
                Toast.makeText(getActivity(), getActivity().getString(R.string.card_style_alert), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(dialogLayout);
        builder.show();
    }
}
