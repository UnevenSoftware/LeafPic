package org.horaapps.leafpic.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StaticMapProvider;
import org.horaapps.leafpic.util.ThemeHelper;
import org.horaapps.leafpic.views.SquareImageView;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

import static org.horaapps.leafpic.util.ThemeHelper.AMOLED_THEME;
import static org.horaapps.leafpic.util.ThemeHelper.DARK_THEME;
import static org.horaapps.leafpic.util.ThemeHelper.LIGHT_THEME;

/**
 * Created by Jibo on 02/03/2016.
 */
@SuppressWarnings("ResourceAsColor")
public class SettingsActivity extends ThemedActivity {

    private PreferenceUtil SP;

    private Toolbar toolbar;
    private ScrollView scr;

    private TextView txtGT;
    private TextView txtTT;
    private TextView txtPT;
    private TextView txtVT;
    private TextView txtAT;

    private SwitchCompat swNavBar;
    private SwitchCompat swStatusBar;
    private SwitchCompat swMaxLuminosity;
    private SwitchCompat swPictureOrientation;
    private SwitchCompat swDelayFullImage;
    private SwitchCompat swInternalPlayer;
    private SwitchCompat swAutoUpdate;
    private SwitchCompat swUseMediaStore;
    private SwitchCompat swIncludeVideo;
    private SwitchCompat swSwipeDirection;
    private SwitchCompat swShowFab;
    private SwitchCompat swSubScaling;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        SP = PreferenceUtil.getInstance(getApplicationContext());

        txtTT = (TextView) findViewById(R.id.theme_setting_title);
        txtGT = (TextView) findViewById(R.id.general_setting_title);
        txtPT = (TextView) findViewById(R.id.picture_setting_title);
        txtVT = (TextView) findViewById(R.id.video_setting_title);
        txtAT = (TextView) findViewById(R.id.advanced_setting_title);

        scr = (ScrollView)findViewById(R.id.settingAct_scrollView);

        /*** BASIC THEME ***/
        findViewById(R.id.ll_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseThemeDialog();
            }
        });

        /*** CARD VIEW STYLE ***/
        findViewById(R.id.ll_card_view_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardViewStyleDialog();
            }
        });

        /*** SECURITY ***/
        findViewById(R.id.ll_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Security.isPasswordSet(getApplicationContext()))
                    askPasswordDialog();
                else
                    startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
            }
        });

        /*** PRIMARY COLOR PIKER ***/
        findViewById(R.id.ll_primaryColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                primaryColorPiker();
            }
        });

        /*** ACCENT COLOR PIKER ***/
        findViewById(R.id.ll_accentColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accentColorPiker();
            }
        });

        /*** EXCLUDED ALBUMS INTENT ***/
        findViewById(R.id.ll_excluded_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ExcludedAlbumsActivity.class));
            }
        });

        /*** CUSTOMIZE PICTURE VIEWER DIALOG ***/
        findViewById(R.id.ll_custom_thirdAct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizePictureViewer();
            }
        });

        /*** MAP PROVIDER DIALOG ***/
        findViewById(R.id.ll_map_provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapProviderDialog();
            }
        });

        /*** MULTI COLUMN DIALOG ***/
        findViewById(R.id.ll_n_columns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiColumnsDialog();
            }
        });

        /*** SW Show Fab ***/
        swShowFab = (SwitchCompat) findViewById(R.id.sw_show_fab);
        swShowFab.setChecked(SP.getBoolean(getString(R.string.preference_show_fab), false));
        swShowFab.setClickable(false);
        findViewById(R.id.ll_fab_options).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swShowFab.setChecked(!swShowFab.isChecked());
                SP.putBoolean(getString(R.string.preference_show_fab), swShowFab.isChecked());
                updateSwitchColor(swShowFab, getAccentColor());
            }
        });

        /*** SW Show Fab ***/
        swSubScaling = (SwitchCompat) findViewById(R.id.sw_sub_scaling);
        swSubScaling.setChecked(SP.getBoolean(getString(R.string.preference_sub_scaling), false));
        swSubScaling.setClickable(false);
        findViewById(R.id.ll_sub_scaling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swSubScaling.setChecked(!swSubScaling.isChecked());
                SP.putBoolean(getString(R.string.preference_sub_scaling), swSubScaling.isChecked());
                updateSwitchColor(swSubScaling, getAccentColor());
            }
        });

        /*** SW Internal Player ***/
        swInternalPlayer = (SwitchCompat) findViewById(R.id.set_internal_player);
        swInternalPlayer.setChecked(SP.getBoolean(getString(R.string.preference_internal_player), false));
        swInternalPlayer.setClickable(false);
        findViewById(R.id.ll_switch_internal_player).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swInternalPlayer.setChecked(!swInternalPlayer.isChecked());
                SP.putBoolean(getString(R.string.preference_internal_player), swInternalPlayer.isChecked());
                updateSwitchColor(swInternalPlayer, getAccentColor());
            }
        });

        /*** SW INCLUDE VIDEO ***/
        swIncludeVideo = (SwitchCompat) findViewById(R.id.set_include_video);
        swIncludeVideo.setChecked(SP.getBoolean(getString(R.string.preference_include_video), true));
        swIncludeVideo.setClickable(false);
        findViewById(R.id.ll_switch_include_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swIncludeVideo.setChecked(!swIncludeVideo.isChecked());
                SP.putBoolean(getString(R.string.preference_include_video), swIncludeVideo.isChecked());
                updateSwitchColor(swIncludeVideo, getAccentColor());
            }
        });

        /*** SW SWIPE DIRECTION ***/
        swSwipeDirection = (SwitchCompat) findViewById(R.id.Set_media_viewer_swipe_direction);
        swSwipeDirection.setChecked(SP.getBoolean(getString(R.string.preference_swipe_direction_inverted), false));
        swSwipeDirection.setClickable(false);
        findViewById(R.id.ll_media_viewer_swipe_direction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swSwipeDirection.setChecked(!swSwipeDirection.isChecked());
                SP.putBoolean(getString(R.string.preference_swipe_direction_inverted), swSwipeDirection.isChecked());
                updateSwitchColor(swSwipeDirection, getAccentColor());
            }
        });

        /*** SW AUTO UPDATE MEDIA ***/
        swAutoUpdate = (SwitchCompat) findViewById(R.id.SetAutoUpdateMedia);
        swAutoUpdate.setChecked(SP.getBoolean(getString(R.string.preference_auto_update_media), false));
        swAutoUpdate.setClickable(false);
        findViewById(R.id.ll_auto_update_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swAutoUpdate.setChecked(!swAutoUpdate.isChecked());
                SP.putBoolean(getString(R.string.preference_auto_update_media), swAutoUpdate.isChecked());
                updateSwitchColor(swAutoUpdate, getAccentColor());
            }
        });

        /*** SW MEDIA STORE ***/
        swUseMediaStore = (SwitchCompat) findViewById(R.id.sw_use_media_mediastore);
        swUseMediaStore.setChecked(SP.getBoolean(getString(R.string.preference_use_alternative_provider), false));
        swUseMediaStore.setClickable(false);
        findViewById(R.id.ll_use_media_mediastore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swUseMediaStore.setChecked(!swUseMediaStore.isChecked());
                SP.putBoolean(getString(R.string.preference_use_alternative_provider), swUseMediaStore.isChecked());
                updateSwitchColor(swUseMediaStore, getAccentColor());
            }
        });

        /*** SW DELAY FULL-SIZE IMAGE ***/
        swDelayFullImage = (SwitchCompat) findViewById(R.id.set_full_resolution);
        swDelayFullImage.setChecked(SP.getBoolean(getString(R.string.preference_delay_full_image), true));
        swDelayFullImage.setClickable(false);
        findViewById(R.id.ll_switch_full_resolution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swDelayFullImage.setChecked(!swDelayFullImage.isChecked());
                SP.putBoolean(getString(R.string.preference_delay_full_image), swDelayFullImage.isChecked());
                updateSwitchColor(swDelayFullImage, getAccentColor());
            }
        });

        /*** SW PICTURE ORIENTATION ***/
        swPictureOrientation = (SwitchCompat) findViewById(R.id.set_picture_orientation);
        swPictureOrientation.setChecked(SP.getBoolean(getString(R.string.preference_auto_rotate), false));
        swPictureOrientation.setClickable(false);
        findViewById(R.id.ll_switch_picture_orientation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swPictureOrientation.setChecked(!swPictureOrientation.isChecked());
                SP.putBoolean(getString(R.string.preference_auto_rotate), swPictureOrientation.isChecked());
                updateSwitchColor(swPictureOrientation, getAccentColor());
            }
        });

        /*** SW MAX LUMINOSITY ***/
        swMaxLuminosity = (SwitchCompat) findViewById(R.id.set_max_luminosity);
        swMaxLuminosity.setChecked(SP.getBoolean(getString(R.string.preference_max_brightness), false));
        swMaxLuminosity.setClickable(false);
        findViewById(R.id.ll_switch_max_luminosity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swMaxLuminosity.setChecked(!swMaxLuminosity.isChecked());
                SP.putBoolean(getString(R.string.preference_max_brightness), swMaxLuminosity.isChecked());
                updateSwitchColor(swMaxLuminosity, getAccentColor());
            }
        });

        /*** SW TRANSLUCENT STATUS BAR ***/
        swStatusBar = (SwitchCompat) findViewById(R.id.SetTraslucentStatusBar);
        swStatusBar.setChecked(SP.getBoolean(getString(R.string.preference_translucent_status_bar), true));
        swStatusBar.setClickable(false);
        findViewById(R.id.ll_switch_TraslucentStatusBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swStatusBar.setChecked(!swStatusBar.isChecked());
                SP.putBoolean(getString(R.string.preference_translucent_status_bar), swStatusBar.isChecked());
                updateTheme();
                setStatusBarColor();
                updateSwitchColor(swStatusBar, getAccentColor());
            }
        });

        /*** SW COLORED NAV BAR ***/
        swNavBar = (SwitchCompat) findViewById(R.id.SetColoredNavBar);
        swNavBar.setChecked(SP.getBoolean(getString(R.string.preference_colored_nav_bar), false));
        swNavBar.setClickable(false);
        findViewById(R.id.ll_switch_ColoredNavBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swNavBar.setChecked(!swNavBar.isChecked());
                SP.putBoolean(getString(R.string.preference_colored_nav_bar), swNavBar.isChecked());
                updateTheme();
                updateSwitchColor(swNavBar, getAccentColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setNavigationBarColor(isNavigationBarColored() ? getPrimaryColor() : ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));

            }
        });
    }

    private void multiColumnsDialog() {
        AlertDialog.Builder multiColumnDialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_multi_column, null);

        ((TextView) dialogLayout.findViewById(R.id.folders_title)).setTextColor(getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.media_title)).setTextColor(getTextColor());
        ((CardView) dialogLayout.findViewById(R.id.multi_column_card)).setCardBackgroundColor(getCardBackgroundColor());

        dialogLayout.findViewById(R.id.multi_column_title).setBackgroundColor(getPrimaryColor());
        final TextView nColFolders = (TextView) dialogLayout.findViewById(R.id.n_columns_folders);
        final TextView nColMedia = (TextView) dialogLayout.findViewById(R.id.n_columns_media);
        nColFolders.setTextColor(getSubTextColor());
        nColMedia.setTextColor(getSubTextColor());
        SeekBar barFolders = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_folders);
        SeekBar barMedia = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_media);

        themeSeekBar(barFolders); themeSeekBar(barMedia);

        nColFolders.setText(String.valueOf(SP.getInt("n_columns_folders", 2)));
        nColMedia.setText(String.valueOf(SP.getInt("n_columns_media", 3)));
        barFolders.setProgress(SP.getInt("n_columns_folders", 2) -1);
        barMedia.setProgress(SP.getInt("n_columns_media", 3) -1);
        barFolders.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColFolders.setText(String.valueOf(i+1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        barMedia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColMedia.setText(String.valueOf(i+1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        multiColumnDialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int nFolders = Integer.parseInt(nColFolders.getText().toString());
                int nMedia = Integer.parseInt(nColMedia.getText().toString());
                SP.putInt("n_columns_folders", nFolders);
                SP.putInt("n_columns_media", nMedia);
            }
        });
        multiColumnDialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), null);
        multiColumnDialogBuilder.setView(dialogLayout);
        multiColumnDialogBuilder.show();
    }

    private void askPasswordDialog() {

        Security.askPassword(SettingsActivity.this, new Security.PasswordInterface() {
            @Override
            public void onSuccess() {
                startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
            }

            @Override
            public void onError() {
                Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void cardViewStyleDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_card_view_style, null);
        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.dialog_card_view_style_title);
        ((CardView) dialogLayout.findViewById(R.id.dialog_card_view_style)).setCardBackgroundColor(getCardBackgroundColor());
        dialogTitle.setBackgroundColor(getPrimaryColor());

        final RadioGroup cardStyle = (RadioGroup) dialogLayout.findViewById(R.id.radio_group_card_view_style);
        final RadioButton radioCardMaterial = (RadioButton) dialogLayout.findViewById(R.id.radio_card_material);
        RadioButton radioCardFlat = (RadioButton) dialogLayout.findViewById(R.id.radio_card_flat);
        RadioButton radioCardCompact = (RadioButton) dialogLayout.findViewById(R.id.radio_card_compact);

        setRadioTextButtonColor(radioCardMaterial, getTextColor());
        setRadioTextButtonColor(radioCardFlat, getTextColor());
        setRadioTextButtonColor(radioCardCompact, getTextColor());

        //PREVIEW ALBUM CARD STYLE
        final SquareImageView imgPreviewAlbumCardStyle = (SquareImageView) dialogLayout.findViewById(R.id.preview_album_card_style_image);
        imgPreviewAlbumCardStyle.setBackgroundColor(getPrimaryColor());

        LinearLayout llPreview = (LinearLayout) dialogLayout.findViewById(R.id.ll_preview_album_card);
        llPreview.setBackgroundColor(getInvertedBackgroundColor());

        final LinearLayout llPreviewFlatCompactText = (LinearLayout) dialogLayout.findViewById(R.id.preview_album_card_style_linear_card_text2);
        final TextView txtPreviewFlatCompactAlbum = (TextView) dialogLayout.findViewById(R.id.preview_album_card_style_name2);
        final TextView txtPreviewFlatCompactMedia = (TextView) dialogLayout.findViewById(R.id.preview_album_card_style_photos_count2);

        final LinearLayout llPreviewMaterialText = (LinearLayout) dialogLayout .findViewById(R.id.preview_album_card_style_linear_card_text);
        final TextView txtPreviewMaterialAlbum = (TextView) dialogLayout.findViewById(R.id.preview_album_card_style_name);
        final TextView txtPreviewMaterialMedia = (TextView) dialogLayout.findViewById(R.id.preview_album_card_style_photos_count);

        llPreviewMaterialText.setBackgroundColor(getCardBackgroundColor());
        llPreviewFlatCompactText.setBackgroundColor(ColorPalette.getTransparentColor(getCardBackgroundColor(), 150));

        int color=getTextColor();
        String albumNameHtml = "<i><font color='" + color+ "'>" + getString(R.string.album) + "</font></i>";
        txtPreviewFlatCompactAlbum.setTextColor(color);
        txtPreviewMaterialAlbum.setTextColor(color);
        txtPreviewFlatCompactAlbum.setText(Html.fromHtml(albumNameHtml));
        txtPreviewMaterialAlbum.setText(Html.fromHtml(albumNameHtml));

        color=getSubTextColor();
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & getAccentColor()));
        String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>" + "n" + "</font></b>" + "<font " +
                "color='" + color + "'> " + getString(R.string.media) + "</font>";
        txtPreviewFlatCompactMedia.setTextColor(color);
        txtPreviewMaterialMedia.setTextColor(color);
        txtPreviewFlatCompactMedia.setText(Html.fromHtml(albumPhotoCountHtml));
        txtPreviewMaterialMedia.setText(Html.fromHtml(albumPhotoCountHtml));

        final TextView txtDescription = (TextView) dialogLayout.findViewById(R.id.preview_album_card_style_description);
        txtDescription.setTextColor(getBackgroundColor());

        //SP.putInt("card_view_style",CardViewStyle.CARD_MATERIAL.getValue());
        switch (CardViewStyle.fromValue(SP.getInt("card_view_style", CardViewStyle.CARD_MATERIAL.getValue()))) {
            case CARD_MATERIAL:
            default:
                radioCardMaterial.setChecked(true);
                llPreviewFlatCompactText.setVisibility(View.GONE);
                llPreviewMaterialText.setVisibility(View.VISIBLE);
                txtDescription.setText(getString(R.string.card_type_material_description));
                break;
            case CARD_FLAT:
                radioCardFlat.setChecked(true);
                llPreviewFlatCompactText.setVisibility(View.VISIBLE);
                txtPreviewFlatCompactMedia.setVisibility(View.VISIBLE);
                llPreviewMaterialText.setVisibility(View.GONE);
                txtDescription.setText(getString(R.string.card_type_flat_description));
                break;
            case CARD_COMPACT:
                radioCardCompact.setChecked(true);
                llPreviewFlatCompactText.setVisibility(View.VISIBLE);
                txtPreviewFlatCompactMedia.setVisibility(View.GONE);
                llPreviewMaterialText.setVisibility(View.GONE);
                txtDescription.setText(getString(R.string.card_type_compact_description));
                break;
        }

        View.OnClickListener radioClickLIstener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.radio_card_material:
                        llPreviewFlatCompactText.setVisibility(View.GONE);
                        llPreviewMaterialText.setVisibility(View.VISIBLE);
                        txtDescription.setText(getString(R.string.card_type_material_description));
                        break;
                    case R.id.radio_card_flat:
                        llPreviewFlatCompactText.setVisibility(View.VISIBLE);
                        txtPreviewFlatCompactMedia.setVisibility(View.VISIBLE);
                        llPreviewMaterialText.setVisibility(View.GONE);
                        txtDescription.setText(getString(R.string.card_type_flat_description));
                        break;
                    case R.id.radio_card_compact:
                        llPreviewFlatCompactText.setVisibility(View.VISIBLE);
                        txtPreviewFlatCompactMedia.setVisibility(View.GONE);
                        llPreviewMaterialText.setVisibility(View.GONE);
                        txtDescription.setText(getString(R.string.card_type_compact_description));
                        break;
                }
            }
        };
        radioCardMaterial.setOnClickListener(radioClickLIstener);
        radioCardFlat.setOnClickListener(radioClickLIstener);
        radioCardCompact.setOnClickListener(radioClickLIstener);

        dialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), null);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (cardStyle.getCheckedRadioButtonId()) {
                    case R.id.radio_card_material:
                    default: SP.putInt("card_view_style", CardViewStyle.CARD_MATERIAL.getValue()); break;
                    case R.id.radio_card_flat: SP.putInt("card_view_style", CardViewStyle.CARD_FLAT.getValue()); break;
                    case R.id.radio_card_compact: SP.putInt("card_view_style", CardViewStyle.CARD_COMPACT.getValue()); break;
                }
            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.show();
    }

    private void mapProviderDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_map_provider, null);
        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.title);
        ((CardView) dialogLayout.findViewById(R.id.dialog_chose_provider_title)).setCardBackgroundColor(getCardBackgroundColor());
        dialogTitle.setBackgroundColor(getPrimaryColor());

        final RadioGroup mapProvider = (RadioGroup) dialogLayout.findViewById(R.id.radio_group_maps_provider);
        RadioButton radioGoogleMaps = (RadioButton) dialogLayout.findViewById(R.id.radio_google_maps);
        RadioButton radioMapBoxStreets = (RadioButton) dialogLayout.findViewById(R.id.radio_mapb_streets);
        RadioButton radioMapBoxDark = (RadioButton) dialogLayout.findViewById(R.id.radio_mapb_dark);
        RadioButton radioMapBoxLight = (RadioButton) dialogLayout.findViewById(R.id.radio_mapb_light);
        RadioButton radioTyler = (RadioButton) dialogLayout.findViewById(R.id.radio_osm_tyler);
        setRadioTextButtonColor(radioGoogleMaps, getSubTextColor());
        setRadioTextButtonColor(radioMapBoxStreets, getSubTextColor());
        setRadioTextButtonColor(radioMapBoxDark, getSubTextColor());
        setRadioTextButtonColor(radioMapBoxLight, getSubTextColor());
        setRadioTextButtonColor(radioTyler, getSubTextColor());

        ((TextView) dialogLayout.findViewById(R.id.header_proprietary_maps)).setTextColor(getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.header_free_maps)).setTextColor(getTextColor());
        switch (StaticMapProvider.fromValue(SP.getInt(getString(R.string.preference_map_provider),
                StaticMapProvider.GOOGLE_MAPS.getValue()))) {
            case GOOGLE_MAPS:
            default: radioGoogleMaps.setChecked(true); break;
            case MAP_BOX: radioMapBoxStreets.setChecked(true); break;
            case MAP_BOX_DARK: radioMapBoxDark.setChecked(true); break;
            case MAP_BOX_LIGHT: radioMapBoxLight.setChecked(true); break;
            case TYLER: radioTyler.setChecked(true); break;
        }

        dialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), null);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (mapProvider.getCheckedRadioButtonId()) {
                    case R.id.radio_google_maps:
                    default: SP.putInt(getString(R.string.preference_map_provider), StaticMapProvider.GOOGLE_MAPS.getValue()); break;
                    case R.id.radio_mapb_streets: SP.putInt(getString(R.string.preference_map_provider), StaticMapProvider.MAP_BOX.getValue()); break;
                    case R.id.radio_osm_tyler: SP.putInt(getString(R.string.preference_map_provider), StaticMapProvider.TYLER.getValue()); break;
                    case R.id.radio_mapb_dark: SP.putInt(getString(R.string.preference_map_provider), StaticMapProvider.MAP_BOX_DARK.getValue()); break;
                    case R.id.radio_mapb_light: SP.putInt(getString(R.string.preference_map_provider), StaticMapProvider.MAP_BOX_LIGHT.getValue()); break;
                }
            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.show();
    }

    private void baseThemeDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());

        final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_basic_theme, null);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.basic_theme_title);
        final CardView dialogCardView = (CardView) dialogLayout.findViewById(R.id.basic_theme_card);

        final IconicsImageView whiteSelect = (IconicsImageView) dialogLayout.findViewById(R.id.white_basic_theme_select);
        final IconicsImageView darkSelect = (IconicsImageView) dialogLayout.findViewById(R.id.dark_basic_theme_select);
        final IconicsImageView darkAmoledSelect = (IconicsImageView) dialogLayout.findViewById(R.id.dark_amoled_basic_theme_select);

        switch (getBaseTheme()){
            case LIGHT_THEME:
                whiteSelect.setVisibility(View.VISIBLE);
                darkSelect.setVisibility(View.GONE);
                darkAmoledSelect.setVisibility(View.GONE);
                break;
            case DARK_THEME:
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.VISIBLE);
                darkAmoledSelect.setVisibility(View.GONE);
                break;
            case AMOLED_THEME:
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.GONE);
                darkAmoledSelect.setVisibility(View.VISIBLE);
                break;
        }

        /** SET OBJ THEME **/
        dialogTitle.setBackgroundColor(getPrimaryColor());
        dialogCardView.setCardBackgroundColor(getCardBackgroundColor());

        dialogLayout.findViewById(R.id.ll_white_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.VISIBLE);
                darkSelect.setVisibility(View.GONE);
                darkAmoledSelect.setVisibility(View.GONE);
                setBaseTheme(LIGHT_THEME, false);
                //dialogCardView.setCardBackgroundColor(getCardBackgroundColor());
                //setTheme();

            }
        });
        dialogLayout.findViewById(R.id.ll_dark_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.VISIBLE);
                darkAmoledSelect.setVisibility(View.GONE);
                setBaseTheme(DARK_THEME, false);
                //dialogCardView.setCardBackgroundColor(getCardBackgroundColor());
                //setTheme();
            }
        });
        dialogLayout.findViewById(R.id.ll_dark_amoled_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.GONE);
                darkAmoledSelect.setVisibility(View.VISIBLE);
                setBaseTheme(AMOLED_THEME, false);
                //dialogCardView.setCardBackgroundColor(getCardBackgroundColor());
                //setTheme();

            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SP.putInt(getString(org.horaapps.leafpic.R.string.preference_base_theme), getBaseTheme());
                setTheme();
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setBaseTheme(ThemeHelper.getBaseTheme(getApplicationContext()), false);
                setTheme();
            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.show();
    }

    private void primaryColorPiker(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());

        final View dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary);
        final LineColorPicker colorPicker2 = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary_2);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.cp_primary_title);
        CardView dialogCardView = (CardView) dialogLayout.findViewById(R.id.cp_primary_card);
        dialogCardView.setCardBackgroundColor(getCardBackgroundColor());

        colorPicker.setColors(ColorPalette.getBaseColors(getApplicationContext()));
        for (int i : colorPicker.getColors())
            for (int i2 : ColorPalette.getColors(getBaseContext(), i))
                if (i2 == getPrimaryColor()) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPalette.getColors(getBaseContext(), i));
                    colorPicker2.setSelectedColor(i2);
                    break;}

        dialogTitle.setBackgroundColor(getPrimaryColor());

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(c);
                }

                toolbar.setBackgroundColor(c);
                dialogTitle.setBackgroundColor(c);
                colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });
        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getObscuredColor(c));
                    } else getWindow().setStatusBarColor(c);
                    if (isNavigationBarColored())
                        getWindow().setNavigationBarColor(c);
                    else
                        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                }
                toolbar.setBackgroundColor(c);
                dialogTitle.setBackgroundColor(c);
            }
        });
        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNeutralButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                }
                toolbar.setBackgroundColor(getPrimaryColor());
                dialog.cancel();
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SP.putInt(getString(R.string.preference_primary_color), colorPicker2.getColor());
                updateTheme();
                setNavBarColor();
                setScrollViewColor(scr);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
                    } else {
                        getWindow().setStatusBarColor(getPrimaryColor());
                    }
                }
            }
        });

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                    if (isNavigationBarColored())
                        getWindow().setNavigationBarColor(getPrimaryColor());
                    else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                }
                toolbar.setBackgroundColor(getPrimaryColor());

            }
        });
        dialogBuilder.show();
    }

    private void accentColorPiker(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());

        final View dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_accent);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.cp_accent_title);
        CardView cv = (CardView) dialogLayout.findViewById(R.id.cp_accent_card);
        cv.setCardBackgroundColor(getCardBackgroundColor());

        colorPicker.setColors(ColorPalette.getAccentColors(getApplicationContext()));
        colorPicker.setSelectedColor(getAccentColor());
        dialogTitle.setBackgroundColor(getAccentColor());

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                dialogTitle.setBackgroundColor(c);
                updateViewsWithAccentColor(colorPicker.getColor());

            }
        });
        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNeutralButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                updateViewsWithAccentColor(getAccentColor());
            }
        });
        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SP.putInt(getString(R.string.preference_accent_color), colorPicker.getColor());
                updateTheme();
                updateViewsWithAccentColor(getAccentColor());
            }
        });
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateViewsWithAccentColor(getAccentColor());
            }
        });
        dialogBuilder.show();
    }

    private void customizePictureViewer() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_media_viewer_theme, null);
        final SwitchCompat swApplyTheme_Viewer = (SwitchCompat) dialogLayout.findViewById(R.id.apply_theme_3th_act_enabled);
        final LinearLayout llApplyTheme_Viewer = (LinearLayout) dialogLayout.findViewById(R.id.ll_apply_theme_3thAct);

        ((CardView) dialogLayout.findViewById(R.id.third_act_theme_card)).setCardBackgroundColor(getCardBackgroundColor());
        dialogLayout.findViewById(R.id.third_act_theme_title).setBackgroundColor(getPrimaryColor());//or GetPrimary
        ((TextView) dialogLayout.findViewById(R.id.apply_theme_3thAct_title)).setTextColor(getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.apply_theme_3thAct_title_Sub)).setTextColor(getSubTextColor());
        ((IconicsImageView) dialogLayout.findViewById(R.id.ll_apply_theme_3thAct_icon)).setColor(getIconColor());

        swApplyTheme_Viewer.setChecked(isApplyThemeOnImgAct());
        swApplyTheme_Viewer.setClickable(false);
        llApplyTheme_Viewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swApplyTheme_Viewer.setChecked(!swApplyTheme_Viewer.isChecked());
                updateSwitchColor(swApplyTheme_Viewer, getAccentColor());
            }
        });
        updateSwitchColor(swApplyTheme_Viewer, getAccentColor());

        final LineColorPicker transparencyColorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.pickerTransparent);
        transparencyColorPicker.setColors(ColorPalette.getTransparencyShadows(getPrimaryColor()));
        transparencyColorPicker.setSelectedColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));

        /**TEXT VIEWS**/
        ((TextView) dialogLayout.findViewById(R.id.seek_bar_alpha_title)).setTextColor(getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.seek_bar_alpha_title_Sub)).setTextColor(getSubTextColor());

        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setNeutralButton(getString(R.string.cancel).toUpperCase(), null);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.getEditor();
                editor.putBoolean(getString(R.string.preference_apply_theme_pager), swApplyTheme_Viewer.isChecked());
                int c = Color.alpha(transparencyColorPicker.getColor());
                editor.putInt(getString(R.string.preference_transparency), 255 - c);
                editor.commit();
                updateTheme();
            }
        });

        dialogBuilder.show();
    }

    private void updateViewsWithAccentColor(int color){
        txtGT.setTextColor(color);
        txtTT.setTextColor(color);
        txtPT.setTextColor(color);
        txtVT.setTextColor(color);
        txtAT.setTextColor(color);

        updateSwitchColor(swDelayFullImage, color);
        updateSwitchColor(swNavBar, color);
        updateSwitchColor(swStatusBar, color);
        updateSwitchColor(swMaxLuminosity, color);
        updateSwitchColor(swPictureOrientation, color);
        updateSwitchColor(swInternalPlayer, color);
        updateSwitchColor(swAutoUpdate, color);
        updateSwitchColor(swIncludeVideo, color);
        updateSwitchColor(swSwipeDirection, color);
        updateSwitchColor(swUseMediaStore, color);
        updateSwitchColor(swShowFab, color);
        updateSwitchColor(swSubScaling, color);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
    }

    private void setTheme(){

        /** BackGround **/
        findViewById(org.horaapps.leafpic.R.id.setting_background).setBackgroundColor(getBackgroundColor());

        /** Cards **/
        int color = getCardBackgroundColor();
        ((CardView) findViewById(org.horaapps.leafpic.R.id.general_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.theme_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.preview_picture_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.video_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(R.id.advanced_setting_card)).setCardBackgroundColor(color);

        toolbar.setBackgroundColor(getPrimaryColor());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(
                new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_arrow_left)
                        .color(Color.WHITE)
                        .sizeDp(19));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(org.horaapps.leafpic.R.string.settings));
        setScrollViewColor(scr);
        updateViewsWithAccentColor(getAccentColor());


        /** Icons **/
        color = getIconColor();
        ((IconicsImageView) findViewById(R.id.ll_switch_picture_orientation_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.ll_switch_max_luminosity_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.ll_switch_full_resolution_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.traslucent_statusbar_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.custom_3thact_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.primary_color_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.accent_color_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.basic_theme_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.n_columns_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.nav_bar_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.excluded_album_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.internal_player_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.internal_include_video)).setColor(color);
        ((IconicsImageView) findViewById(R.id.auto_update_media_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.use_media_mediastore_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.security_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.fab_options_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.sub_scaling_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.map_provider_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.media_viewer_swipe_direction_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.card_view_style_icon)).setColor(color);

        /** TextViews **/
        color = getTextColor();
        ((TextView) findViewById(R.id.max_luminosity_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.full_resolution_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.picture_orientation_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.custom_3thAct_title)).setTextColor(color);
        ((TextView) findViewById(R.id.Traslucent_StatusBar_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.PrimaryColor_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.accentColor_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.basic_theme_item)).setTextColor(color);
        ((TextView) findViewById(R.id.n_columns_Item_Title)).setTextColor(color);
        ((TextView) findViewById(R.id.NavBar_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.sub_scaling_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Excluded_Album_Item_Title)).setTextColor(color);
        ((TextView) findViewById(R.id.internal_player_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.include_video_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.auto_update_media_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.security_item_title)).setTextColor(color);
        ((TextView) findViewById(R.id.use_media_mediastore_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.fab_options_item_title)).setTextColor(color);
        ((TextView) findViewById(R.id.map_provider_item_title)).setTextColor(color);
        ((TextView) findViewById(R.id.media_viewer_swipe_direction_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.card_view_style_title)).setTextColor(color);

        /** Sub Text Views**/
        color = getSubTextColor();
        ((TextView) findViewById(R.id.max_luminosity_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.full_resolution_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.custom_3thAct_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.picture_orientation_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.Traslucent_StatusBar_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.use_media_mediastore_Item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.PrimaryColor_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.accentColor_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.basic_theme_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.n_columns_Item_Title_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.NavBar_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.Excluded_Album_Item_Title_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.internal_player_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.sub_scaling_Item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.include_video_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.auto_update_media_Item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.security_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.fab_options_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.map_provider_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.media_viewer_swipe_direction_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.card_view_style_Sub)).setTextColor(color);

        //LIMITED OPTIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.ll_switch_TraslucentStatusBar).setVisibility(View.VISIBLE);
            if(hasNavBar()) findViewById(R.id.ll_switch_ColoredNavBar).setVisibility(View.VISIBLE);
        }
    }

    private boolean hasNavBar() {
        Resources resources = getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return resources.getBoolean(id);
        } else {    // Check for keys
            boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !hasMenuKey && !hasBackKey;
        }
    }
}
