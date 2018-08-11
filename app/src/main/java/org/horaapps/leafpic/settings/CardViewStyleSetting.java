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

import org.horaapps.leafpic.CardViewStyle;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ColorPalette;
import org.horaapps.liz.Theme;
import org.horaapps.liz.ThemedActivity;

import static org.horaapps.leafpic.CardViewStyle.COMPACT;
import static org.horaapps.leafpic.CardViewStyle.FLAT;
import static org.horaapps.leafpic.CardViewStyle.MATERIAL;


/**
 * Created by dnld on 12/9/16.
 */

public class CardViewStyleSetting extends ThemedSetting {

    public CardViewStyleSetting(ThemedActivity activity) {
        super(activity);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());
        final View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_album_card_style, null);

        TextView dialogTitle = dialogLayout.findViewById(R.id.dialog_card_view_style_title);
        ((CardView) dialogLayout.findViewById(R.id.dialog_card_view_style)).setCardBackgroundColor(getActivity().getCardBackgroundColor());
        dialogTitle.setBackgroundColor(getActivity().getPrimaryColor());

        final RadioGroup rGroup = dialogLayout.findViewById(R.id.radio_group_card_view_style);
        final CheckBox chkShowMediaCount = dialogLayout.findViewById(R.id.show_media_count);
        final CheckBox chkShowAlbumPath = dialogLayout.findViewById(R.id.show_album_path);
        RadioButton rCompact = dialogLayout.findViewById(R.id.radio_card_compact);
        RadioButton rFlat = dialogLayout.findViewById(R.id.radio_card_flat);
        RadioButton rMaterial = dialogLayout.findViewById(R.id.radio_card_material);

        chkShowMediaCount.setChecked(Prefs.showMediaCount());
        chkShowAlbumPath.setChecked(Prefs.showAlbumPath());

        getActivity().themeRadioButton(rCompact);
        getActivity().themeRadioButton(rFlat);
        getActivity().themeRadioButton(rMaterial);
        getActivity().themeCheckBox(chkShowMediaCount);
        getActivity().themeCheckBox(chkShowAlbumPath);

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                final View v;
                switch (i) {
                    case R.id.radio_card_compact:
                        v = LayoutInflater.from(getActivity()).inflate(COMPACT.getLayout(), null);
                        v.findViewById(R.id.ll_album_info).setBackgroundColor(ColorPalette.getTransparentColor(getActivity().getBackgroundColor(), 150));
                        break;
                    case R.id.radio_card_flat:
                        v = LayoutInflater.from(getActivity()).inflate(FLAT.getLayout(), null);
                        v.findViewById(R.id.ll_album_info).setBackgroundColor(ColorPalette.getTransparentColor(getActivity().getBackgroundColor(), 150));
                        break;
                    case R.id.radio_card_material: default:
                        v = LayoutInflater.from(getActivity()).inflate(MATERIAL.getLayout(), null);
                        v.findViewById(R.id.ll_album_info).setBackgroundColor(getActivity().getCardBackgroundColor());
                        break;
                }

                ImageView img = v.findViewById(R.id.album_preview);
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
                ((TextView) v.findViewById(R.id.album_path)).setTextColor(getActivity().getSubTextColor());

                v.findViewById(R.id.ll_media_count).setVisibility( chkShowMediaCount.isChecked() ? View.VISIBLE : View.GONE);
                chkShowMediaCount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        v.findViewById(R.id.ll_media_count).setVisibility(b ? View.VISIBLE : View.GONE);
                    }
                });

                v.findViewById(R.id.album_path).setVisibility( chkShowAlbumPath.isChecked() ? View.VISIBLE : View.GONE);
                chkShowAlbumPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        v.findViewById(R.id.album_path).setVisibility(b ? View.VISIBLE : View.GONE);
                    }
                });

                ((TextView) v.findViewById(R.id.album_name)).setText(StringUtils.html("<i><font color='" + textColor + "'>PraiseDuarte</font></i>"));
                ((TextView) v.findViewById(R.id.album_path)).setText("~/home/PraiseDuarte");

                ((CardView) v).setUseCompatPadding(true);
                ((CardView) v).setRadius(2);

                ((LinearLayout) dialogLayout.findViewById(R.id.ll_preview_album_card)).removeAllViews();
                ((LinearLayout) dialogLayout.findViewById(R.id.ll_preview_album_card)).addView(v);
            }
        });

        switch (Prefs.getCardStyle()) {
            case COMPACT: rCompact.setChecked(true); break;
            case FLAT: rFlat.setChecked(true); break;
            case MATERIAL: default: rMaterial.setChecked(true); break;
        }

        builder.setNegativeButton(getActivity().getString(R.string.cancel).toUpperCase(), null);
        builder.setPositiveButton(getActivity().getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CardViewStyle cardViewStyle;
                switch (rGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_card_material:
                    default:
                        cardViewStyle = MATERIAL;
                        break;
                    case R.id.radio_card_flat:
                        cardViewStyle = FLAT;
                        break;
                    case R.id.radio_card_compact:
                        cardViewStyle = COMPACT;
                        break;
                }
                Prefs.setCardStyle(cardViewStyle);
                Prefs.setShowMediaCount(chkShowMediaCount.isChecked());
                Prefs.setShowAlbumPath(chkShowAlbumPath.isChecked());
                Toast.makeText(getActivity(), getActivity().getString(R.string.restart_app), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(dialogLayout);
        builder.show();
    }
}
