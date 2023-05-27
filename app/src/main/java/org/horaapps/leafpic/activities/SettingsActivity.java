package org.horaapps.leafpic.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.orhanobut.hawk.Hawk;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.settings.CardViewStyleSetting;
import org.horaapps.leafpic.settings.ColorsSetting;
import org.horaapps.leafpic.settings.GeneralSetting;
import org.horaapps.leafpic.settings.MapProviderSetting;
import org.horaapps.leafpic.settings.SinglePhotoSetting;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.views.SettingWithSwitchView;
import org.horaapps.liz.ColorPalette;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ViewUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * The Settings Activity used to select settings.
 */
public class SettingsActivity extends ThemedActivity {

    private Toolbar toolbar;

    @BindView(R.id.option_max_brightness)
    SettingWithSwitchView optionMaxBrightness;

    @BindView(R.id.option_picture_orientation)
    SettingWithSwitchView optionOrientation;

    @BindView(R.id.option_full_resolution)
    SettingWithSwitchView optionDelayFullRes;

    @BindView(R.id.option_auto_update_media)
    SettingWithSwitchView optionAutoUpdateMedia;

    @BindView(R.id.option_include_video)
    SettingWithSwitchView optionIncludeVideo;

    @BindView(R.id.option_swipe_direction)
    SettingWithSwitchView optionSwipeDirection;

    @BindView(R.id.option_fab)
    SettingWithSwitchView optionShowFab;

    @BindView(R.id.option_statusbar)
    SettingWithSwitchView optionStatusbar;

    @BindView(R.id.option_colored_navbar)
    SettingWithSwitchView optionColoredNavbar;

    @BindView(R.id.option_sub_scaling)
    SettingWithSwitchView optionSubScaling;

    @BindView(R.id.option_disable_animations)
    SettingWithSwitchView optionDisableAnimations;

    private Unbinder unbinder;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        unbinder = ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        optionStatusbar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                updateTheme();
                setStatusBarColor();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ViewUtil.hasNavBar(this)) {
                optionColoredNavbar.setOnClickListener(new View.OnClickListener() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View view) {
                        updateTheme();
                        getWindow().setNavigationBarColor(isNavigationBarColored() ? getPrimaryColor() : ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                    }
                });
            } else
                optionColoredNavbar.setVisibility(View.GONE);
        }
        ScrollView scrollView = findViewById(R.id.settingAct_scrollView);
        setScrollViewColor(scrollView);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        findViewById(org.horaapps.leafpic.R.id.setting_background).setBackgroundColor(getBackgroundColor());
        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(org.horaapps.leafpic.R.string.settings));
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = getThemeHelper().getPrimaryColor();
            if (isTranslucentStatusBar())
                getWindow().setStatusBarColor(ColorPalette.getObscuredColor(color));
            else
                getWindow().setStatusBarColor(color);
            if (isNavigationBarColored())
                getWindow().setNavigationBarColor(color);
            else
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.md_black_1000));
        }
    }

    @OnClick(R.id.ll_basic_theme)
    public void onChangeThemeClicked(View view) {
        new ColorsSetting(SettingsActivity.this).chooseBaseTheme();
    }

    @OnClick(R.id.ll_card_view_style)
    public void onChangeCardViewStyleClicked(View view) {
        new CardViewStyleSetting(SettingsActivity.this).show();
    }

    @OnClick(R.id.ll_security)
    public void onSecurityClicked(View view) {
        if (Security.isPasswordSet()) {
            askPassword();
        } else
            startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
    }

    private void askPassword() {
        Security.authenticateUser(SettingsActivity.this, new Security.AuthCallBack() {

            @Override
            public void onAuthenticated() {
                startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
            }

            @Override
            public void onError() {
                Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.ll_primaryColor)
    public void onChangePrimaryColorClicked(View view) {
        final int originalColor = getPrimaryColor();
        new ColorsSetting(SettingsActivity.this).chooseColor(R.string.primary_color, new ColorsSetting.ColorChooser() {

            @Override
            public void onColorSelected(int color) {
                Hawk.put(getString(R.string.preference_primary_color), color);
                updateTheme();
                updateUiElements();
            }

            @Override
            public void onDialogDismiss() {
                Hawk.put(getString(R.string.preference_primary_color), originalColor);
                updateTheme();
                updateUiElements();
            }

            @Override
            public void onColorChanged(int color) {
                Hawk.put(getString(R.string.preference_primary_color), color);
                updateTheme();
                updateUiElements();
            }
        }, getPrimaryColor());
    }

    @OnClick(R.id.ll_accentColor)
    public void onChangeAccentColorClicked(View view) {
        final int originalColor = getAccentColor();
        new ColorsSetting(SettingsActivity.this).chooseColor(R.string.accent_color, new ColorsSetting.ColorChooser() {

            @Override
            public void onColorSelected(int color) {
                Hawk.put(getString(R.string.preference_accent_color), color);
                updateTheme();
                updateUiElements();
            }

            @Override
            public void onDialogDismiss() {
                Hawk.put(getString(R.string.preference_accent_color), originalColor);
                updateTheme();
                updateUiElements();
            }

            @Override
            public void onColorChanged(int color) {
                Hawk.put(getString(R.string.preference_accent_color), color);
                updateTheme();
                updateUiElements();
            }
        }, getAccentColor());
    }

    @OnClick(R.id.ll_custom_icon_color)
    public void onChangedCustomIconClicked(View view) {
        updateTheme();
        updateUiElements();
    }

    @OnClick(R.id.ll_white_list)
    public void onWhiteListClicked(View view) {
        startActivity(new Intent(getApplicationContext(), BlackWhiteListActivity.class));
    }

    @OnClick(R.id.ll_custom_thirdAct)
    public void onCustomThirdActClicked(View view) {
        new SinglePhotoSetting(SettingsActivity.this).show();
    }

    @OnClick(R.id.ll_map_provider)
    public void onMapProviderClicked(View view) {
        new MapProviderSetting(SettingsActivity.this).choseProvider();
    }

    @OnClick(R.id.ll_n_columns)
    public void onChangeColumnsClicked(View view) {
        new GeneralSetting(SettingsActivity.this).editNumberOfColumns();
    }
}
