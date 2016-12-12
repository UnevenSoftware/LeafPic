package org.horaapps.leafpic.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.settings.CardViewStyleSetting;
import org.horaapps.leafpic.settings.ColorsSetting;
import org.horaapps.leafpic.settings.GeneralSetting;
import org.horaapps.leafpic.settings.MapProviderSetting;
import org.horaapps.leafpic.settings.SinglePhotoSetting;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.Security;

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

    private SwitchCompat swAutoUpdate;
    private SwitchCompat swUseMediaStore;
    private SwitchCompat swIncludeVideo;
    private SwitchCompat swSwipeDirection;
    private SwitchCompat swShowFab;
    private SwitchCompat swSubScaling;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        SP = PreferenceUtil.getInstance(getApplicationContext());

        txtTT = (TextView) findViewById(R.id.theme_setting_title);
        txtGT = (TextView) findViewById(R.id.general_setting_title);
        txtPT = (TextView) findViewById(R.id.picture_setting_title);
        txtVT = (TextView) findViewById(R.id.video_setting_title);
        txtAT = (TextView) findViewById(R.id.advanced_setting_title);

        scr = (ScrollView)findViewById(R.id.settingAct_scrollView);

        initUi();

        /*** BASIC THEME ***/
        findViewById(R.id.ll_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorsSetting(SettingsActivity.this, SP).chooseBaseTheme();
            }
        });

        /*** CARD VIEW STYLE ***/
        findViewById(R.id.ll_card_view_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CardViewStyleSetting(SettingsActivity.this, SP).show();
            }
        });

        /*** SECURITY ***/
        findViewById(R.id.ll_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Security.isPasswordSet(getApplicationContext()))
                    askPasswordDialog();
                else startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
            }
        });

        /*** PRIMARY COLOR PIKER ***/
        findViewById(R.id.ll_primaryColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorsSetting(SettingsActivity.this, SP).chooseColor(R.string.primary_color, new ColorsSetting.ColorChooser() {
                    @Override
                    public void onColorSelected(int color) {
                        SP.putInt(getString(R.string.preference_primary_color), color);
                        updateViewsWithPrimaryColor(color);
                    }

                    @Override
                    public void onDialogDismiss() {
                        updateViewsWithPrimaryColor(getPrimaryColor());
                    }

                    @Override
                    public void onColorChanged(int color) {
                        updateViewsWithPrimaryColor(color);
                    }
                }, getPrimaryColor());
            }
        });

        /*** ACCENT COLOR PIKER ***/
        findViewById(R.id.ll_accentColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorsSetting(SettingsActivity.this, SP).chooseColor(R.string.accent_color, new ColorsSetting.ColorChooser() {
                    @Override
                    public void onColorSelected(int color) {
                        SP.putInt(getString(R.string.preference_accent_color), color);
                        updateViewsWithAccentColor(color);
                    }

                    @Override
                    public void onDialogDismiss() {
                        updateViewsWithAccentColor(getAccentColor());
                    }

                    @Override
                    public void onColorChanged(int color) {
                        updateViewsWithAccentColor(color);
                    }
                }, getAccentColor());
            }
        });

        /*** EXCLUDED ALBUMS INTENT ***/
        findViewById(R.id.ll_white_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), WhiteListActivity.class));
            }
        });

        /*** CUSTOMIZE PICTURE VIEWER DIALOG ***/
        findViewById(R.id.ll_custom_thirdAct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SinglePhotoSetting(SettingsActivity.this, SP).show();
            }
        });

        /*** MAP PROVIDER DIALOG ***/
        findViewById(R.id.ll_map_provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MapProviderSetting(SettingsActivity.this, SP).choseProvider();
            }
        });

        /*** MULTI COLUMN DIALOG ***/
        findViewById(R.id.ll_n_columns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GeneralSetting(SettingsActivity.this, SP).editNumberOfColumns();
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
                setSwitchColor(getAccentColor(), swShowFab);
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
                setSwitchColor(getAccentColor(), swSubScaling);
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
                setSwitchColor(getAccentColor(), swIncludeVideo);
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
                setSwitchColor(getAccentColor(), swSwipeDirection);
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
                setSwitchColor(getAccentColor(), swAutoUpdate);
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
                setSwitchColor(getAccentColor(), swUseMediaStore);
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
                setSwitchColor(getAccentColor(), swDelayFullImage);
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
                setSwitchColor(getAccentColor(), swPictureOrientation);
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
                setSwitchColor(getAccentColor(), swMaxLuminosity);
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
                setSwitchColor(getAccentColor(), swStatusBar);
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
                setSwitchColor(getAccentColor(), swNavBar);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setNavigationBarColor(isNavigationBarColored() ? getPrimaryColor() : ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));

            }
        });
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

    public void updateViewsWithAccentColor(int color){
        setTextViewColor(color, txtAT, txtGT, txtPT, txtTT, txtVT);
        setSwitchColor(color,
                swDelayFullImage,
                swNavBar,
                swStatusBar,
                swMaxLuminosity,
                swPictureOrientation,
                swAutoUpdate,
                swIncludeVideo,
                swSwipeDirection,
                swUseMediaStore,
                swShowFab,
                swSubScaling);
    }

    public void updateViewsWithPrimaryColor(int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTranslucentStatusBar()) {
                getWindow().setStatusBarColor(ColorPalette.getObscuredColor(color));
            } else getWindow().setStatusBarColor(color);
            if (isNavigationBarColored())
                getWindow().setNavigationBarColor(color);
            else
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.md_black_1000));
        }
        toolbar.setBackgroundColor(color);
        setScrollViewColor(scr);
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }



    @Override
    public void updateUiElements(){

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
        ((IconicsImageView) findViewById(R.id.white_list_icon)).setColor(color);
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
        ((TextView) findViewById(R.id.white_list_item_title)).setTextColor(color);
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
        ((TextView) findViewById(R.id.white_list_item_title_sub)).setTextColor(color);
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
