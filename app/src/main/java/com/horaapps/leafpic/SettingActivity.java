package com.horaapps.leafpic;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.SecurityHelper;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

/**
 * Created by Jibo on 02/03/2016.
 */
public class SettingActivity extends ThemedActivity {

    public static final int GOOGLE_MAPS_PROVIDER = 0;
    public static final int OSM_PROVIDER = 1;

    private SharedPreferences SP;
    private SecurityHelper securityObj;

    private Toolbar toolbar;
    private ScrollView scr;

    private TextView txtGT;
    private TextView txtTT;
    private TextView txtPT;
    private TextView txtVT;

    private SwitchCompat swNavBar;
    private SwitchCompat swStatusBar;
    private SwitchCompat swMaxLuminosita;
    private SwitchCompat swPictureOrientation;
    private SwitchCompat swDelayFullImage;
    private SwitchCompat swInternalBrowser;
    private SwitchCompat swAutoUpdate;
    private SwitchCompat swIncludeVideo;
    private SwitchCompat swSwipeDirection;

    private int baseThemeValue;

    //TESTING STUFF
    private FloatingActionButton fabMoreThemeOptions;
    private FloatingActionButton fabMoreGeneralOptions;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        //region TESTING
        fabMoreThemeOptions = (FloatingActionButton) findViewById(R.id.fab_more_theme_options);
        fabMoreGeneralOptions = (FloatingActionButton) findViewById(R.id.fab_more_general_options);

        fabMoreGeneralOptions.setBackgroundTintList(ColorStateList.valueOf(getSubTextColor()));
        fabMoreGeneralOptions.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_keyboard_arrow_up)
                .sizeDp(16).color(getCardBackgroundColor()));

        fabMoreGeneralOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout llMoreOptions = (LinearLayout) findViewById(R.id.ll_more_options_general);
                boolean visible = llMoreOptions.getVisibility() == View.VISIBLE;
                llMoreOptions.setVisibility(visible ? View.GONE : View.VISIBLE);
                fabMoreGeneralOptions.setImageDrawable(new IconicsDrawable(SettingActivity.this)
                        .icon(visible ? GoogleMaterial.Icon.gmd_keyboard_arrow_up : GoogleMaterial.Icon.gmd_keyboard_arrow_down)
                        .sizeDp(16).color(getCardBackgroundColor()));
            }
        });

        fabMoreThemeOptions.setBackgroundTintList(ColorStateList.valueOf(getSubTextColor()));
        fabMoreThemeOptions.setImageDrawable(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_keyboard_arrow_up)
                .sizeDp(16).color(getCardBackgroundColor()));
        fabMoreThemeOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout llMoreOptions = (LinearLayout) findViewById(R.id.ll_more_options_theme);
                boolean visible = llMoreOptions.getVisibility() == View.VISIBLE;
                llMoreOptions.setVisibility(visible ? View.GONE : View.VISIBLE);
                fabMoreThemeOptions.setImageDrawable(new IconicsDrawable(SettingActivity.this)
                                .icon(visible ? GoogleMaterial.Icon.gmd_keyboard_arrow_up : GoogleMaterial.Icon.gmd_keyboard_arrow_down)
                                .sizeDp(16).color(getCardBackgroundColor()));
            }
        });
        //endregion

        securityObj = new SecurityHelper(SettingActivity.this);

        txtTT = (TextView) findViewById(R.id.theme_setting_title);
        txtGT = (TextView) findViewById(R.id.general_setting_title);
        txtPT = (TextView) findViewById(R.id.picture_setting_title);
        txtVT = (TextView) findViewById(R.id.video_setting_title);

        scr = (ScrollView)findViewById(R.id.settingAct_scrollView);


        /*** EXCLUDED ALBUMS ***/
        findViewById(R.id.ll_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseThemeDialog();
            }
        });

        /*** SECURITY ***/
        findViewById(R.id.ll_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!securityObj.isActiveSecurity())
                    startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
                else
                    askPasswordDialog();

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
                startActivity(new Intent(SettingActivity.this, ExcludedAlbumsActivity.class));
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

        /*** SW Internal Player ***/
        swInternalBrowser = (SwitchCompat) findViewById(R.id.set_internal_player);
        swInternalBrowser.setChecked(SP.getBoolean(getString(R.string.preference_internal_player), false));
        swInternalBrowser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_internal_player), isChecked);
                editor.apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ((MyApplication) getApplicationContext()).updateAlbums();
                    }
                }).start();

                updateSwitchColor(swInternalBrowser, getAccentColor());
            }
        });
        updateSwitchColor(swInternalBrowser, getAccentColor());

        /*** SW INCLUDE VIDEO ***/
        swIncludeVideo = (SwitchCompat) findViewById(R.id.set_include_video);
        swIncludeVideo.setChecked(SP.getBoolean(getString(R.string.preference_include_video), true));
        swIncludeVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_include_video), isChecked);
                editor.apply();
                updateSwitchColor(swIncludeVideo, getAccentColor());
            }
        });
        updateSwitchColor(swIncludeVideo, getAccentColor());

        /*** SW SWIPE DIRECTION ***/
        swSwipeDirection = (SwitchCompat) findViewById(R.id.Set_media_viewer_swipe_direction);
        swSwipeDirection.setChecked(SP.getBoolean(getString(R.string.preference_swipe_direction_inverted), false));
        swSwipeDirection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_swipe_direction_inverted), isChecked);
                editor.apply();
                updateSwitchColor(swSwipeDirection, getAccentColor());
            }
        });
        updateSwitchColor(swSwipeDirection, getAccentColor());

        /*** SW AUTO UPDATE MEDIA ***/
        swAutoUpdate = (SwitchCompat) findViewById(R.id.SetAutoUpdateMedia);
        swAutoUpdate.setChecked(SP.getBoolean(getString(R.string.preference_auto_update_media), false));
        swAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_auto_update_media), isChecked);
                editor.apply();
                updateSwitchColor(swAutoUpdate, getAccentColor());
            }
        });
        updateSwitchColor(swAutoUpdate, getAccentColor());

        /*** SW DELAY FULL-SIZE IMAGE ***/
        swDelayFullImage = (SwitchCompat) findViewById(R.id.set_full_resolution);
        swDelayFullImage.setChecked(SP.getBoolean(getString(R.string.preference_delay_full_image), true));
        swDelayFullImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_delay_full_image), isChecked);
                editor.apply();
                updateSwitchColor(swDelayFullImage, getAccentColor());
            }
        });
        updateSwitchColor(swDelayFullImage, getAccentColor());

        /*** SW PICTURE ORIENTATION ***/
        swPictureOrientation = (SwitchCompat) findViewById(R.id.set_picture_orientation);
        swPictureOrientation.setChecked(SP.getBoolean(getString(R.string.preference_auto_rotate), false));
        swPictureOrientation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_auto_rotate), isChecked);
                editor.apply();
                updateSwitchColor(swPictureOrientation, getAccentColor());
            }
        });
        updateSwitchColor(swPictureOrientation, getAccentColor());

        /*** SW MAX LUMINOSITA ***/
        swMaxLuminosita = (SwitchCompat) findViewById(R.id.set_max_luminosita);
        swMaxLuminosita.setChecked(SP.getBoolean(getString(R.string.preference_max_brightness), false));
        swMaxLuminosita.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_max_brightness), isChecked);
                editor.apply();
                updateSwitchColor(swMaxLuminosita, getAccentColor());
            }
        });
        updateSwitchColor(swMaxLuminosita, getAccentColor());

        /*** SW TRANSLUCENT STATUS BAR ***/
        swStatusBar = (SwitchCompat) findViewById(R.id.SetTraslucentStatusBar);
        swStatusBar.setChecked(isTranslucentStatusBar());
        swStatusBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_translucent_status_bar), isChecked);
                editor.apply();
                updateTheme();
                setStatusBarColor();
                updateSwitchColor(swStatusBar, getAccentColor());
            }
        });
        updateSwitchColor(swStatusBar, getAccentColor());

        /*** SW COLORED NAV BAR ***/
        swNavBar = (SwitchCompat) findViewById(R.id.SetColoredNavBar);
        swNavBar.setChecked(isNavigationBarColored());
        swNavBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_colored_nav_bar), isChecked);
                editor.apply();
                updateTheme();
                updateSwitchColor(swNavBar, getAccentColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setNavigationBarColor(
                            isNavigationBarColored() ? getPrimaryColor() : ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));

            }
        });
        updateSwitchColor(swNavBar, getAccentColor());
    }

    private void askPasswordDialog() {
        AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        final EditText editTextPassword  = securityObj.getInsertPasswordDialog(SettingActivity.this,passwordDialogBuilder);
        passwordDialogBuilder.setNegativeButton(getString(R.string.cancel), null);

        passwordDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //This should br empty it will be overwrite later
                //to avoid dismiss of the dialog on wrong password
            }
        });

        final AlertDialog passwordDialog = passwordDialogBuilder.create();
        passwordDialog.show();

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (securityObj.checkPassword(editTextPassword.getText().toString())) {
                    passwordDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                    editTextPassword.getText().clear();
                    editTextPassword.requestFocus();
                }
            }
        });
    }

    private void mapProviderDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        View dialogLayout = getLayoutInflater().inflate(R.layout.map_provider_dialog, null);
        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.title);
        ((CardView) dialogLayout.findViewById(R.id.rename_card)).setCardBackgroundColor(getCardBackgroundColor());
        dialogTitle.setBackgroundColor(getPrimaryColor());
        dialogTitle.setTextColor(getTextColor());

        final RadioGroup mapProvider = (RadioGroup) dialogLayout.findViewById(R.id.radio_group_maps_provider);
        RadioButton radioGoogleMaps = (RadioButton) dialogLayout.findViewById(R.id.radio_google_maps);
        RadioButton radioOsm = (RadioButton) dialogLayout.findViewById(R.id.radio_osm);
        updateRadioButtonColor(radioGoogleMaps);
        updateRadioButtonColor(radioOsm);
        switch (SP.getInt(getString(R.string.preference_map_provider),GOOGLE_MAPS_PROVIDER)) {
            case GOOGLE_MAPS_PROVIDER:
                default:
                    radioGoogleMaps.setChecked(true);
                    break;
            case OSM_PROVIDER:
                radioOsm.setChecked(true);
                break;
        }

        dialogBuilder.setNegativeButton(R.string.cancel, null);
        dialogBuilder.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences.Editor editor = SP.edit();
                editor.putInt(getString(R.string.preference_map_provider), baseThemeValue);
                editor.apply();

                switch (mapProvider.getCheckedRadioButtonId()) {
                    case R.id.radio_google_maps:
                        default:
                        editor.putInt(getString(R.string.preference_map_provider), GOOGLE_MAPS_PROVIDER);
                        break;
                    case R.id.radio_osm:
                        editor.putInt(getString(R.string.preference_map_provider), OSM_PROVIDER);
                        break;
                }
                editor.apply();

            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.show();
    }

    private void baseThemeDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());

        final View dialogLayout = getLayoutInflater().inflate(R.layout.basic_theme_dialog, null);
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
                baseThemeValue = LIGHT_THEME;
            }
        });
        dialogLayout.findViewById(R.id.ll_dark_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.VISIBLE);
                darkAmoledSelect.setVisibility(View.GONE);
                baseThemeValue = DARK_THEME;
            }
        });
        dialogLayout.findViewById(R.id.ll_dark_amoled_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.GONE);
                darkAmoledSelect.setVisibility(View.VISIBLE);
                baseThemeValue = AMOLED_THEME;
            }
        });
        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt(getString(R.string.preference_base_theme), baseThemeValue);
                editor.apply();
                startActivity(getIntent());
                finish();
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                baseThemeValue = getBaseTheme();
            }
        });
        dialogBuilder.show();
    }

    private void primaryColorPiker(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());

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
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
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
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(c));
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

        dialogBuilder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                }
                toolbar.setBackgroundColor(getPrimaryColor());
                dialog.cancel();
            }
        });
        dialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt(getString(R.string.preference_primary_color), colorPicker2.getColor());
                editor.apply();
                updateTheme();
                setNavBarColor();
                setScrollViewColor(scr);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
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
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
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
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());

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
                updateAccentViewsColor(colorPicker.getColor());

            }
        });
        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                updateAccentViewsColor(getAccentColor());
            }
        });
        dialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt(getString(R.string.preference_accent_color), colorPicker.getColor());
                editor.apply();
                updateTheme();
                updateAccentViewsColor(getAccentColor());
            }
        });
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateAccentViewsColor(getAccentColor());
            }
        });
        dialogBuilder.show();
    }

    private void customizePictureViewer(){

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());

        View dialogLayout = getLayoutInflater().inflate(R.layout.third_act_theme_dialog, null);
        final SwitchCompat swApplyTheme_Viewer = (SwitchCompat) dialogLayout.findViewById(R.id.apply_theme_3th_act_enabled);

        ((CardView) dialogLayout.findViewById(R.id.third_act_theme_card)).setCardBackgroundColor(getCardBackgroundColor());
        dialogLayout.findViewById(R.id.third_act_theme_title).setBackgroundColor(getPrimaryColor());//or Getprimary
        ((TextView) dialogLayout.findViewById(R.id.apply_theme_3thAct_title)).setTextColor(getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.apply_theme_3thAct_title_Sub)).setTextColor(getSubTextColor());
        ((IconicsImageView) dialogLayout.findViewById(R.id.ll_apply_theme_3thAct_icon)).setColor(getIconColor());

        swApplyTheme_Viewer.setChecked(isApplyThemeOnImgAct());
        swApplyTheme_Viewer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        dialogBuilder.setNeutralButton(getString(R.string.cancel), null);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean(getString(R.string.preference_apply_theme_pager), swApplyTheme_Viewer.isChecked
                                                                                                ());
                int c = Color.alpha(transparencyColorPicker.getColor());
                editor.putInt(getString(R.string.preference_transparency), 255 - c);
                editor.apply();
                updateTheme();
            }
        });

        dialogBuilder.show();
    }

    private void updateAccentViewsColor(int color){
        txtGT.setTextColor(color);
        txtTT.setTextColor(color);
        txtPT.setTextColor(color);
        txtVT.setTextColor(color);

        updateSwitchColor(swDelayFullImage, color);
        updateSwitchColor(swNavBar, color);
        updateSwitchColor(swStatusBar, color);
        updateSwitchColor(swMaxLuminosita, color);
        updateSwitchColor(swPictureOrientation, color);
        updateSwitchColor(swInternalBrowser, color);
        updateSwitchColor(swAutoUpdate, color);
        updateSwitchColor(swIncludeVideo, color);
        updateSwitchColor(swSwipeDirection, color);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
        securityObj.updateSecuritySetting();
    }

    private void setTheme(){
        toolbar.setBackgroundColor(getPrimaryColor());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_arrow_back)
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
        setRecentApp(getString(R.string.settings));

        txtGT.setTextColor(getAccentColor());
        txtTT.setTextColor(getAccentColor());
        txtPT.setTextColor(getAccentColor());
        txtVT.setTextColor(getAccentColor());

        setScrollViewColor(scr);
        setThemeOnChangeListener();
    }

    private void setThemeOnChangeListener(){
        setScrollViewColor(scr);

        /** BackGround **/
        findViewById(R.id.setting_background).setBackgroundColor(getBackgroundColor());

        /** Cards **/
        int color = getCardBackgroundColor();
        ((CardView) findViewById(R.id.general_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(R.id.theme_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(R.id.preview_picture_setting_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(R.id.video_setting_card)).setCardBackgroundColor(color);

        /** Icons **/
        color = getIconColor();
        ((IconicsImageView) findViewById(R.id.ll_switch_picture_orientation_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.ll_switch_max_luminosita_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.ll_switch_full_resolution_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Traslucent_StatusBar_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.custom_3thAct_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.PrimaryColor_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.accentColor_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.basic_theme_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.NavBar_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Excluded_Album_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.internal_player_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.internal_include_video)).setColor(color);
        ((IconicsImageView) findViewById(R.id.auto_update_media_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.security_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.fab_options_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.map_provider_icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.media_viewer_swipe_direction_Icon)).setColor(color);

        /** TextViews **/
        color = getTextColor();
        ((TextView) findViewById(R.id.max_luminosita_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.full_resolution_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.picture_orientation_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.custom_3thAct_title)).setTextColor(color);
        ((TextView) findViewById(R.id.Traslucent_StatusBar_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.PrimaryColor_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.accentColor_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.basic_theme_item)).setTextColor(color);
        ((TextView) findViewById(R.id.NavBar_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Excluded_Album_Item_Title)).setTextColor(color);
        ((TextView) findViewById(R.id.internal_player_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.include_video_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.auto_update_media_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.security_item_title)).setTextColor(color);
        ((TextView) findViewById(R.id.fab_options_item_title)).setTextColor(color);
        ((TextView) findViewById(R.id.map_provider_item_title)).setTextColor(color);
        ((TextView) findViewById(R.id.media_viewer_swipe_direction_Item)).setTextColor(color);

        /** Sub Text Views**/
        color = getSubTextColor();
        ((TextView) findViewById(R.id.max_luminosita_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.full_resolution_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.custom_3thAct_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.picture_orientation_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.Traslucent_StatusBar_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.PrimaryColor_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.accentColor_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.basic_theme_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.NavBar_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.Excluded_Album_Item_Title_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.internal_player_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.include_video_Item_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.auto_update_media_Item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.security_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.fab_options_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.map_provider_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.media_viewer_swipe_direction_sub)).setTextColor(color);
    }
}
