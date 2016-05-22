package com.horaapps.leafpic;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    Toolbar toolbar;
    SharedPreferences SP;
    int BasicThemeValue;

    TextView txtGT;
    TextView txtTT;
    TextView txtPT;
    TextView txtVT;
    SecurityHelper securityObj;

    SwitchCompat swNavBar;
    SwitchCompat swStatusBar;
    SwitchCompat swMaxLuminosita;
    SwitchCompat swPictureOrientation;
    SwitchCompat swDelayFullImage;
    SwitchCompat swInternalBrowser;
    SwitchCompat swAutoUpdate;

    boolean maxLuminosita, pictureOrientation, delayfullimage,internalPlayer;

    ScrollView scr;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        securityObj = new SecurityHelper(SettingActivity.this);

        txtTT = (TextView) findViewById(R.id.theme_setting_title);
        txtGT = (TextView) findViewById(R.id.general_setting_title);
        txtPT = (TextView) findViewById(R.id.picture_setting_title);
        txtVT = (TextView) findViewById(R.id.video_setting_title);

        scr = (ScrollView)findViewById(R.id.settingAct_scrollView);


        setNavBarColor();
        maxLuminosita = SP.getBoolean("set_max_luminosita", false);
        pictureOrientation = SP.getBoolean("set_picture_orientation", false);
        delayfullimage = SP.getBoolean("set_delay_full_image", true);
        internalPlayer =  SP.getBoolean("set_internal_player", false);

        //EXCLUDED ALBUMS INTENT*****************************************
        LinearLayout ll_BT = (LinearLayout) findViewById(R.id.ll_basic_theme);
        ll_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasicThemeDialog();
            }
        });

        //SECURITY*****************************************
        LinearLayout ll_SR = (LinearLayout) findViewById(R.id.ll_security);
        ll_SR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!securityObj.isActiveSecurity())
                    startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
                else
                    askPasswordDialog();

            }
        });

        //PRIMARY COLOR PIKER*****************************************
        LinearLayout ll_PC = (LinearLayout) findViewById(R.id.ll_primaryColor);
        ll_PC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrimaryColorPikerDialogShow();
            }
        });

        //ACCENT COLOR PIKER*****************************************
        LinearLayout ll_AC = (LinearLayout) findViewById(R.id.ll_accentColor);
        ll_AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccentColorPikerDialogShow();
            }
        });

        //EXCLUDED ALBUMS INTENT*****************************************
        LinearLayout ll_EA = (LinearLayout) findViewById(R.id.ll_excluded_album);
        ll_EA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, ExcludedAlbumsActivity.class));
            }
        });

        //CUSTOMIZE PICTURE VIEWER DIALOG*****************************************
        LinearLayout ll_CPV = (LinearLayout) findViewById(R.id.ll_custom_thirdAct);
        ll_CPV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomizePictureViewerDialog();
            }
        });


        /*********** SW Internal Player ************/
        swInternalBrowser = (SwitchCompat) findViewById(R.id.set_internal_player);
        swInternalBrowser.setChecked(internalPlayer);
        swInternalBrowser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_internal_player", isChecked);
                editor.apply();
                updateSwitchColor(swInternalBrowser, getAccentColor());
            }
        });
        updateSwitchColor(swInternalBrowser, getAccentColor());
        /**** Switches ****/

        /*********** SW AUTO UPDATE MEDIA ************/
        swAutoUpdate = (SwitchCompat) findViewById(R.id.SetAutoUpdateMedia);
        swAutoUpdate.setChecked(SP.getBoolean("auto_update_media", false));
        swAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("auto_update_media", isChecked);
                editor.apply();
                updateSwitchColor(swAutoUpdate, getAccentColor());
            }
        });
        updateSwitchColor(swAutoUpdate, getAccentColor());

        /*********** SW Delay Full Image ************/
        swDelayFullImage = (SwitchCompat) findViewById(R.id.set_full_resolution);
        swDelayFullImage.setChecked(delayfullimage);
        swDelayFullImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_delay_full_image", isChecked);
                editor.apply();
                updateSwitchColor(swDelayFullImage, getAccentColor());
            }
        });
        updateSwitchColor(swDelayFullImage, getAccentColor());

        /*********** SW Picture_orientation ************/
        swPictureOrientation = (SwitchCompat) findViewById(R.id.set_picture_orientation);
        swPictureOrientation.setChecked(pictureOrientation);
        swPictureOrientation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_picture_orientation", isChecked);
                editor.apply();
                updateSwitchColor(swPictureOrientation, getAccentColor());
            }
        });
        updateSwitchColor(swPictureOrientation, getAccentColor());

        /*********** SW MAX LUMINOSITA ************/
        swMaxLuminosita = (SwitchCompat) findViewById(R.id.set_max_luminosita);
        swMaxLuminosita.setChecked(maxLuminosita);
        swMaxLuminosita.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_max_luminosita", isChecked);
                editor.apply();
                updateSwitchColor(swMaxLuminosita, getAccentColor());
            }
        });
        updateSwitchColor(swMaxLuminosita, getAccentColor());

        /*********** SW TRASLUCENT STATUS BAR ****************/
        swStatusBar=(SwitchCompat) findViewById(R.id.SetTraslucentStatusBar);
        swStatusBar.setChecked(isTraslucentStatusBar());
        swStatusBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_traslucent_statusbar", !isTraslucentStatusBar());
                editor.apply();
                updateTheme();
                setStatusBarColor();
                updateSwitchColor(swStatusBar, getAccentColor());
            }
        });
        updateSwitchColor(swStatusBar, getAccentColor());

        /*********** SW COLORED NAV BAR ****************/
        swNavBar=(SwitchCompat) findViewById(R.id.SetColoredNavBar);
        swNavBar.setChecked(isNavigationBarColored());
        swNavBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("nav_bar", !isNavigationBarColored());
                editor.apply();
                updateTheme();
                updateSwitchColor(swNavBar, getAccentColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isNavigationBarColored())
                        getWindow().setNavigationBarColor(getPrimaryColor());
                    else
                        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                }
            }
        });
        updateSwitchColor(swNavBar, getAccentColor());
    }

    /*public void updateSwitchColor(SwitchCompat sw,int color){
        if(sw.isChecked())
            sw.getThumbDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        else
            sw.getThumbDrawable().setColorFilter(getTextColor(), PorterDuff.Mode.MULTIPLY);
        sw.getTrackDrawable().setColorFilter(getBackgroundColor(), PorterDuff.Mode.MULTIPLY);
    }*/

    public void askPasswordDialog() {
        final AlertDialog.Builder passwordDialog = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        final View PasswordDialogLayout = getLayoutInflater().inflate(R.layout.password_dialog, null);
        final TextView passwordDialogTitle = (TextView) PasswordDialogLayout.findViewById(R.id.password_dialog_title);
        final CardView passwordDialogCard = (CardView) PasswordDialogLayout.findViewById(R.id.password_dialog_card);
        final EditText editxtPassword = (EditText) PasswordDialogLayout.findViewById(R.id.password_edittxt);

        passwordDialogTitle.setBackgroundColor(getPrimaryColor());
        passwordDialogCard.setBackgroundColor(getCardBackgroundColor());

        editxtPassword.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editxtPassword.setTextColor(getTextColor());

        passwordDialog.setView(PasswordDialogLayout);
        passwordDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (securityObj.checkPassword(editxtPassword.getText().toString()))
                    startActivity(new Intent(getApplicationContext(),SecurityActivity.class));
                else
                    Toast.makeText(passwordDialog.getContext(),R.string.wrong_password,Toast.LENGTH_SHORT).show();
            }
        });
        passwordDialog.setNegativeButton(getString(R.string.cancel), null);
        passwordDialog.show();
    }

    public void securityDialog(){

        final AlertDialog.Builder securityDialog = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        final boolean changedActiveSecurity = securityObj.isActiveSecurity();

        final View SecurityDialogLayout = getLayoutInflater().inflate(R.layout.security_dialog, null);

        final TextView securityDialogTitle = (TextView) SecurityDialogLayout.findViewById(R.id.security_title_dialog);
        final CardView securityDialogCard = (CardView) SecurityDialogLayout.findViewById(R.id.security_dialog_card);
        final LinearLayout llbody = (LinearLayout) SecurityDialogLayout.findViewById(R.id.ll_security_dialog_body);

        final IconicsImageView imgActiveSecurity = (IconicsImageView) SecurityDialogLayout.findViewById(R.id.active_security_icon);
        final TextView txtActiveSecurity = (TextView) SecurityDialogLayout.findViewById(R.id.active_security_item_title);
        final SwitchCompat swActiveSecurity = (SwitchCompat) SecurityDialogLayout.findViewById(R.id.active_security_switch);

        final EditText eTxtPasswordSecurity = (EditText) SecurityDialogLayout.findViewById(R.id.security_password_edittxt);

        final TextView txtApplySecurity = (TextView) SecurityDialogLayout.findViewById(R.id.security_body_apply_on);
        final IconicsImageView imgApplySecurityHidden = (IconicsImageView) SecurityDialogLayout.findViewById(R.id.security_body_apply_hidden_icon);
        final TextView txtApplySecurityHidden = (TextView) SecurityDialogLayout.findViewById(R.id.security_body_apply_hidden_title);
        final SwitchCompat swApplySecurityHidden = (SwitchCompat) SecurityDialogLayout.findViewById(R.id.security_body_apply_hidden_switch);

        final IconicsImageView imgApplySecurityDelete = (IconicsImageView) SecurityDialogLayout.findViewById(R.id.security_body_apply_delete_icon);
        final TextView txtApplySecurityDelete = (TextView) SecurityDialogLayout.findViewById(R.id.security_body_apply_delete_title);
        final SwitchCompat swApplySecurityDelete = (SwitchCompat) SecurityDialogLayout.findViewById(R.id.security_body_apply_delete_switch);


        /*** SETING DIALOG THEME ***/
        securityDialogTitle.setBackgroundColor(getPrimaryColor());
        securityDialogCard.setBackgroundColor(getCardBackgroundColor());

        /*
        eTxtPasswordSecurity.setTextColor(getAccentColor());
        eTxtPasswordSecurity.setHintTextColor(getAccentColor());
        eTxtPasswordSecurity.setBackgroundColor(getBackgroundColor());
        */
        eTxtPasswordSecurity.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
        eTxtPasswordSecurity.setTextColor(getTextColor());
        eTxtPasswordSecurity.setText(SP.getString("password_value", ""));

        /*ICONS*/
        int color = getIconColor();
        imgActiveSecurity.setColor(color);
        imgApplySecurityHidden.setColor(color);
        imgApplySecurityDelete.setColor(color);

        /*TEXTVIEWS*/
        color=getTextColor();
        txtActiveSecurity.setTextColor(color);
        txtApplySecurity.setTextColor(color);
        txtApplySecurityHidden.setTextColor(color);
        txtApplySecurityDelete.setTextColor(color);

        /** - SWITCHS - **/
        /** - ACTIVE SECURITY - **/
        swActiveSecurity.setChecked(securityObj.isActiveSecurity());
        swActiveSecurity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("active_security", isChecked);
                editor.apply();


                securityObj.updateSecuritySetting();
                updateSwitchColor(swActiveSecurity, getAccentColor());
                llbody.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        updateSwitchColor(swActiveSecurity, getAccentColor());
        llbody.setVisibility(swActiveSecurity.isChecked() ? View.VISIBLE : View.GONE);

        /** - ACTIVE SECURITY ON HIDDEN FOLDER - **/
        swApplySecurityHidden.setChecked(securityObj.isPasswordOnHidden());
        swApplySecurityHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("password_on_hidden", isChecked);
                editor.apply();

                securityObj.updateSecuritySetting();
                updateSwitchColor(swApplySecurityHidden, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityHidden, getAccentColor());

        /**ACTIVE SECURITY ON DELETE ACTION**/
        swApplySecurityDelete.setChecked(securityObj.isPasswordOnDelete());
        swApplySecurityDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("password_on_delete", isChecked);
                editor.apply();

                securityObj.updateSecuritySetting();
                updateSwitchColor(swApplySecurityDelete, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityDelete, getAccentColor());

        securityDialog.setView(SecurityDialogLayout);

        securityDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(swActiveSecurity.isChecked()) {
                    if (eTxtPasswordSecurity.length() > 3) {
                        if (!securityObj.checkPassword(eTxtPasswordSecurity.getText().toString())) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putString("password_value", eTxtPasswordSecurity.getText().toString());
                            editor.apply();
                            securityObj.updateSecuritySetting();
                            Toast.makeText(getApplicationContext(), R.string.remember_password_message, Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    } else
                        Toast.makeText(securityDialog.getContext(), R.string.error_password_length, Toast.LENGTH_SHORT).show();
                }
            }
        });
        securityDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (changedActiveSecurity != securityObj.isActiveSecurity()) {
                    SharedPreferences.Editor editor = SP.edit();
                    editor.putBoolean("active_security", changedActiveSecurity);
                    editor.apply();
                    securityObj.updateSecuritySetting();
                }
                dialog.cancel();
            }
        });
        securityDialog.show();
    }

    public void BasicThemeDialog(){
        final AlertDialog.Builder BasicThemeDialog = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        final View BasicTheme_dialogLayout = getLayoutInflater().inflate(R.layout.basic_theme_dialog, null);
        final TextView BasicTheme_Dialog_Title = (TextView) BasicTheme_dialogLayout.findViewById(R.id.basic_theme_title);
        final CardView BasicTheme_CV = (CardView) BasicTheme_dialogLayout.findViewById(R.id.basic_theme_card);

        final LinearLayout ll_white = (LinearLayout) BasicTheme_dialogLayout.findViewById(R.id.ll_white_basic_theme);
        final LinearLayout ll_dark = (LinearLayout) BasicTheme_dialogLayout.findViewById(R.id.ll_dark_basic_theme);
        final LinearLayout ll_darkAMOLED = (LinearLayout) BasicTheme_dialogLayout.findViewById(R.id.ll_dark_amoled_basic_theme);

        final IconicsImageView whiteSelect = (IconicsImageView) BasicTheme_dialogLayout.findViewById(R.id.white_basic_theme_select);
        final IconicsImageView darkSelect = (IconicsImageView) BasicTheme_dialogLayout.findViewById(R.id.dark_basic_theme_select);
        final IconicsImageView darkAmoledSelect = (IconicsImageView) BasicTheme_dialogLayout.findViewById(R.id.dark_amoled_basic_theme_select);

        whiteSelect.setVisibility(View.INVISIBLE);
        darkSelect.setVisibility(View.INVISIBLE);
        darkAmoledSelect.setVisibility(View.VISIBLE);
        switch (getBasicTheme()){
            case 1:
                whiteSelect.setVisibility(View.VISIBLE);
                darkSelect.setVisibility(View.INVISIBLE);
                darkAmoledSelect.setVisibility(View.INVISIBLE);
                break;
            case 2:
                whiteSelect.setVisibility(View.INVISIBLE);
                darkSelect.setVisibility(View.VISIBLE);
                darkAmoledSelect.setVisibility(View.INVISIBLE);
                break;
            case 3:
                whiteSelect.setVisibility(View.INVISIBLE);
                darkSelect.setVisibility(View.INVISIBLE);
                darkAmoledSelect.setVisibility(View.VISIBLE);
                break;
        }

        /**SET OBJ THEME**/
        BasicTheme_Dialog_Title.setBackgroundColor(getPrimaryColor());
        BasicTheme_CV.setCardBackgroundColor(getCardBackgroundColor());

        ll_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.VISIBLE);
                darkSelect.setVisibility(View.INVISIBLE);
                darkAmoledSelect.setVisibility(View.INVISIBLE);
                BasicThemeValue=1;
            }
        });
        ll_dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.INVISIBLE);
                darkSelect.setVisibility(View.VISIBLE);
                darkAmoledSelect.setVisibility(View.INVISIBLE);
                BasicThemeValue=2;
            }
        });
        ll_darkAMOLED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.INVISIBLE);
                darkSelect.setVisibility(View.INVISIBLE);
                darkAmoledSelect.setVisibility(View.VISIBLE);
                BasicThemeValue=3;
            }
        });
        BasicThemeDialog.setView(BasicTheme_dialogLayout);
        BasicThemeDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("basic_theme", BasicThemeValue);
                editor.apply();
                dialog.cancel();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        BasicThemeDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                BasicThemeValue=getBasicTheme();
            }
        });
        BasicThemeDialog.show();
    }

    public void PrimaryColorPikerDialogShow(){
        final AlertDialog.Builder PrimaryPikerDialog = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
        final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary);
        final LineColorPicker colorPicker2 = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary2);
        final TextView title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_primary_title);
        CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_primary_card);
        cv.setCardBackgroundColor(getCardBackgroundColor());

        colorPicker.setColors(ColorPalette.getBaseColors(getApplicationContext()));
        for (int i : colorPicker.getColors())
            for (int i2 : ColorPalette.getColors(getBaseContext(), i))
                if (i2 == getPrimaryColor()) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPalette.getColors(getBaseContext(), i));
                    colorPicker2.setSelectedColor(i2);
                    break;}

        title.setBackgroundColor(getPrimaryColor());

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTraslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(c);
                }

                toolbar.setBackgroundColor(c);
                title.setBackgroundColor(c);
                colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });
        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTraslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(c));
                    } else getWindow().setStatusBarColor(c);
                    if (isNavigationBarColored())
                        getWindow().setNavigationBarColor(c);
                    else
                        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                }
                toolbar.setBackgroundColor(c);
                title.setBackgroundColor(c);
            }
        });

        PrimaryPikerDialog.setView(Accent_dialogLayout);
        PrimaryPikerDialog.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTraslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                }
                toolbar.setBackgroundColor(getPrimaryColor());
                dialog.cancel();
            }
        });
        PrimaryPikerDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("primary_color", colorPicker2.getColor());
                editor.apply();
                updateTheme();
                setNavBarColor();
                setScrollViewColor(scr);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTraslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                    } else {
                        getWindow().setStatusBarColor(getPrimaryColor());
                    }
                }
            }
        });
        PrimaryPikerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTraslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                    if (isNavigationBarColored())
                        getWindow().setNavigationBarColor(getPrimaryColor());
                    else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                }
                toolbar.setBackgroundColor(getPrimaryColor());

            }
        });
        PrimaryPikerDialog.show();
    }

    public void AccentColorPikerDialogShow(){

        final AlertDialog.Builder AccentPikerDialog = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
        final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerAccent);
        final TextView title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_accent_title);
        CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_accent_card);
        cv.setCardBackgroundColor(getCardBackgroundColor());

        colorPicker.setColors(ColorPalette.getAccentColors(getApplicationContext()));
        colorPicker.setSelectedColor(getAccentColor());
        title.setBackgroundColor(getAccentColor());

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                title.setBackgroundColor(c);
                updateAccentViewsColor(colorPicker.getColor());

            }
        });
        AccentPikerDialog.setView(Accent_dialogLayout);

        AccentPikerDialog.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                updateAccentViewsColor(getAccentColor());
            }
        });
        AccentPikerDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("accent_color", colorPicker.getColor());
                editor.apply();
                updateTheme();
                updateAccentViewsColor(getAccentColor());
            }
        });
        AccentPikerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateAccentViewsColor(getAccentColor());
            }
        });
        AccentPikerDialog.show();
    }

    public void CustomizePictureViewerDialog(){

        final AlertDialog.Builder CustomizeViewer = new AlertDialog.Builder(SettingActivity.this, getDialogStyle());
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        final View CustomizeThird_dialogLayout = getLayoutInflater().inflate(R.layout.third_act_theme_dialog, null);
        final TextView txtTitle = (TextView) CustomizeThird_dialogLayout.findViewById(R.id.third_act_theme_title);
        final TextView txtAT3A = (TextView) CustomizeThird_dialogLayout.findViewById(R.id.apply_theme_3thAct_title);
        final TextView txtAT3A_Sub = (TextView) CustomizeThird_dialogLayout.findViewById(R.id.apply_theme_3thAct_title_Sub);
        final IconicsImageView imgAT3A = (IconicsImageView) CustomizeThird_dialogLayout.findViewById(R.id.ll_apply_theme_3thAct_icon);
        final SwitchCompat swApplyTheme_Viewer = (SwitchCompat) CustomizeThird_dialogLayout.findViewById(R.id.apply_theme_3th_act_enabled);

        CardView cv = (CardView) CustomizeThird_dialogLayout.findViewById(R.id.third_act_theme_card);
        cv.setCardBackgroundColor(getCardBackgroundColor());
        txtTitle.setBackgroundColor(getPrimaryColor());//or Getprimary
        txtAT3A.setTextColor(getTextColor());
        txtAT3A_Sub.setTextColor(getSubTextColor());
        imgAT3A.setColor(getIconColor());

        /**SWITCH**/
        swApplyTheme_Viewer.setChecked(isApplyThemeOnImgAct());
        swApplyTheme_Viewer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSwitchColor(swApplyTheme_Viewer, getAccentColor());
            }
        });
        updateSwitchColor(swApplyTheme_Viewer, getAccentColor());

        /**COLOR PICK**/
        final LineColorPicker TrasparentCP = (LineColorPicker) CustomizeThird_dialogLayout.findViewById(R.id.pickerTransparent);
        int[] colors = new int[10];
        for (int i=0; i<10;i++){
            colors[i]=(ColorPalette.getTransparentColor(getPrimaryColor(), ((100-(i*10))*255)/100));
        }
        TrasparentCP.setColors(colors);
        int transColor = ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency());
        TrasparentCP.setSelectedColor(transColor);

        /**TEXT VIEWS**/
        final TextView txtTrasparencyTit = (TextView) CustomizeThird_dialogLayout.findViewById(R.id.seek_bar_alpha_title);
        final TextView txtTrasparency_Sub = (TextView) CustomizeThird_dialogLayout.findViewById(R.id.seek_bar_alpha_title_Sub);
        txtTrasparencyTit.setTextColor(getTextColor());
        txtTrasparency_Sub.setTextColor(getSubTextColor());

        CustomizeViewer.setView(CustomizeThird_dialogLayout);
        CustomizeViewer.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        CustomizeViewer.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("apply_theme_img_act", swApplyTheme_Viewer.isChecked());
                int c = Color.alpha(TrasparentCP.getColor());
                editor.putInt("set_alpha", 255 - c);
                editor.apply();
                updateTheme();
            }
        });
        CustomizeViewer.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        CustomizeViewer.show();
    }

    public void updateAccentViewsColor(int color){
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
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
        securityObj.updateSecuritySetting();
    }

    public void setTheme(){

        /**** ToolBar *****/
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
        toolbar.setTitle(getString(R.string.settings));

        /**** Status Bar */
        setStatusBarColor();

        /**** Nav Bar ****/
        setNavBarColor();

        /**** Recent App */
        setRecentApp(getString(R.string.settings));

        txtGT.setTextColor(getAccentColor());
        txtTT.setTextColor(getAccentColor());
        txtPT.setTextColor(getAccentColor());
        txtVT.setTextColor(getAccentColor());


        setScrollViewColor(scr);

        setThemeOnChangeListener();
    }

    public void setThemeOnChangeListener(){

        setScrollViewColor(scr);

        /** BackGround **/
        LinearLayout bg = (LinearLayout) findViewById(R.id.setting_background);
        bg.setBackgroundColor(getBackgroundColor());

        /** Cards **/
        CardView cvGeneral = (CardView) findViewById(R.id.general_setting_card);
        CardView cvTheme = (CardView) findViewById(R.id.theme_setting_card);
        CardView cvPicture = (CardView) findViewById(R.id.preview_picture_setting_card);
        CardView cvVideo= (CardView) findViewById(R.id.video_setting_card);

        int color = getCardBackgroundColor();
        cvGeneral.setCardBackgroundColor(color);
        cvTheme.setCardBackgroundColor(color);
        cvPicture.setCardBackgroundColor(color);
        cvVideo.setCardBackgroundColor(color);

        /** Icons **/
        IconicsImageView imgOrient = (IconicsImageView) findViewById(R.id.ll_switch_picture_orientation_icon);
        IconicsImageView imgMax = (IconicsImageView) findViewById(R.id.ll_switch_max_luminosita_icon);
        IconicsImageView imgDelay = (IconicsImageView) findViewById(R.id.ll_switch_full_resolution_icon);

        IconicsImageView imgTSB = (IconicsImageView) findViewById(R.id.Traslucent_StatusBar_Icon);
        IconicsImageView imgC3A = (IconicsImageView) findViewById(R.id.custom_3thAct_icon);
        IconicsImageView imgPC = (IconicsImageView) findViewById(R.id.PrimaryColor_Icon);
        IconicsImageView imgAC = (IconicsImageView) findViewById(R.id.accentColor_Icon);
        IconicsImageView imgBT = (IconicsImageView) findViewById(R.id.basic_theme_icon);
        IconicsImageView imgNB = (IconicsImageView) findViewById(R.id.NavBar_Icon);
        IconicsImageView imgEA = (IconicsImageView) findViewById(R.id.Excluded_Album_Icon);
        IconicsImageView imgIN = (IconicsImageView) findViewById(R.id.internal_player_Icon);
        IconicsImageView imgAUM = (IconicsImageView) findViewById(R.id.auto_update_media_Icon);
        IconicsImageView imgSR = (IconicsImageView) findViewById(R.id.security_icon);


        color = getIconColor();
        imgMax.setColor(color);
        imgIN.setColor(color);
        imgDelay.setColor(color);
        imgC3A.setColor(color);
        imgTSB.setColor(color);
        imgPC.setColor(color);
        imgAC.setColor(color);
        imgBT.setColor(color);
        imgNB.setColor(color);
        imgOrient.setColor(color);
        imgEA.setColor(color);
        imgAUM.setColor(color);
        imgSR.setColor(color);

        /** TextViews **/
        TextView txtMax = (TextView) findViewById(R.id.max_luminosita_Item);
        TextView txtDelay = (TextView) findViewById(R.id.full_resolution_Item);
        TextView txtOrient = (TextView) findViewById(R.id.picture_orientation_Item);
        TextView txtC3AT = (TextView) findViewById(R.id.custom_3thAct_title);
        TextView txtTSB = (TextView) findViewById(R.id.Traslucent_StatusBar_Item);
        TextView txtPC = (TextView) findViewById(R.id.PrimaryColor_Item);
        TextView txtAC = (TextView) findViewById(R.id.accentColor_Item);
        TextView txtBTI = (TextView) findViewById(R.id.basic_theme_item);
        TextView txtNB = (TextView) findViewById(R.id.NavBar_Item);
        TextView txtEAT = (TextView) findViewById(R.id.Excluded_Album_Item_Title);
        TextView txtInt = (TextView) findViewById(R.id.internal_player_Item);
        TextView txtAUM = (TextView) findViewById(R.id.auto_update_media_Item);
        TextView txtSR = (TextView) findViewById(R.id.security_item_title);

        color=getTextColor();
        txtInt.setTextColor(color);
        txtMax.setTextColor(color);
        txtOrient.setTextColor(color);
        txtC3AT.setTextColor(color);
        txtTSB.setTextColor(color);
        txtPC.setTextColor(color);
        txtAC.setTextColor(color);
        txtBTI.setTextColor(color);
        txtNB.setTextColor(color);
        txtEAT.setTextColor(color);
        txtDelay.setTextColor(color);
        txtAUM.setTextColor(color);
        txtSR.setTextColor(color);

        /** Sub Text Views**/
        TextView txtMax_Sub = (TextView) findViewById(R.id.max_luminosita_Item_Sub);
        TextView txtDelay_Sub = (TextView) findViewById(R.id.full_resolution_Item_Sub);
        TextView txtOrient_Sub = (TextView) findViewById(R.id.picture_orientation_Item_Sub);
        TextView txtC3A_Sub = (TextView) findViewById(R.id.custom_3thAct_Sub);
        TextView txtTSB_Sub = (TextView) findViewById(R.id.Traslucent_StatusBar_Item_Sub);
        TextView txtPC_Sub = (TextView) findViewById(R.id.PrimaryColor_Item_Sub);
        TextView txtAC_Sub = (TextView) findViewById(R.id.accentColor_Item_Sub);
        TextView txtBTI_Sub = (TextView) findViewById(R.id.basic_theme_item_sub);
        TextView txtNB_Sub = (TextView) findViewById(R.id.NavBar_Item_Sub);
        TextView txtEAT_Sub = (TextView) findViewById(R.id.Excluded_Album_Item_Title_Sub);
        TextView txtInt_Sub = (TextView) findViewById(R.id.internal_player_Item_Sub);
        TextView txtAUM_Sub = (TextView) findViewById(R.id.auto_update_media_Item_sub);
        TextView txtSR_Sub = (TextView) findViewById(R.id.security_item_sub);


        color=getSubTextColor();
        txtInt_Sub.setTextColor(color);
        txtDelay_Sub.setTextColor(color);
        txtMax_Sub.setTextColor(color);
        txtOrient_Sub.setTextColor(color);
        txtC3A_Sub.setTextColor(color);
        txtTSB_Sub.setTextColor(color);
        txtPC_Sub.setTextColor(color);
        txtAC_Sub.setTextColor(color);
        txtBTI_Sub.setTextColor(color);
        txtNB_Sub.setTextColor(color);
        txtEAT_Sub.setTextColor(color);
        txtAUM_Sub.setTextColor(color);
        txtSR_Sub.setTextColor(color);

    }
}
