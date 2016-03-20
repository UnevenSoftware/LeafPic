package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.ColorPalette;
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

    TextView txtGT;
    TextView txtTT;
    TextView txtPT;

    TextView txtATT;//APPLY THEME TITLE
    TextView txtSBT;//TITLE
    TextView txtSBC;//COUNT

    SwitchCompat swCollaps;
    SwitchCompat swDarkTheme;
    SwitchCompat swNavBar;
    SwitchCompat swStatusBar;
    SwitchCompat swMaxLuminosita;
    SwitchCompat swPictureOrientation;
    SwitchCompat swApplyTheme3thAct;

    SeekBar sbAlpha;

    boolean maxLuminosita, pictureOrientation;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        txtTT = (TextView) findViewById(R.id.theme_setting_title);
        txtGT = (TextView) findViewById(R.id.general_setting_title);
        txtPT = (TextView) findViewById(R.id.picture_setting_title);

        txtATT = (TextView) findViewById(R.id.apply_theme_3thAct_title);
        txtSBT = (TextView) findViewById(R.id.seek_bar_alpha_title);
        txtSBC = (TextView) findViewById(R.id.seek_bar_alpha_count);

        setNavBarColor();
        maxLuminosita = SP.getBoolean("set_max_luminosita", false);
        pictureOrientation = SP.getBoolean("set_picture_orientation", false);

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


        /**** SEEK BAR ****/
        sbAlpha = (SeekBar) findViewById(R.id.seek_bar_alpha);
        txtSBC.setText(((SP.getInt("set_alpha", 0))*100)/255 + "%");
        txtSBC.setTextColor(getAccentColor());
        sbAlpha.setProgress(SP.getInt("set_alpha", 0));

        sbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = SP.getInt("set_alpha", 0);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                txtSBC.setText((progress * 100)/255 + "%" /*+ "/" + seekBar.getMax()*/);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txtSBC.setText((progress * 100)/255  + "%" /*+ "/" + seekBar.getMax()*/);
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("set_alpha", progress);
                editor.apply();
            }
        });


        /**** Switches ****/

        /*********** SW Enable_Trasparency ************/
        swApplyTheme3thAct = (SwitchCompat) findViewById(R.id.apply_theme_3th_act_enabled);
        swApplyTheme3thAct.setChecked(isApplyThemeOnImgAct());
        swApplyTheme3thAct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("apply_theme_img_act", isChecked);
                editor.apply();
                updateSwitchColor(swApplyTheme3thAct);
                sbAlpha.setEnabled(isChecked);
            }
        });
        sbAlpha.setEnabled(swApplyTheme3thAct.isChecked());
        updateSwitchColor(swApplyTheme3thAct);

        /*********** SW Picture_orientation ************/
        swPictureOrientation = (SwitchCompat) findViewById(R.id.set_picture_orientation);
        swPictureOrientation.setChecked(pictureOrientation);
        swPictureOrientation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_picture_orientation", isChecked);
                editor.apply();
                updateSwitchColor(swPictureOrientation);
            }
        });
        updateSwitchColor(swPictureOrientation);

        /*********** SW MAX LUMINOSITA ************/
        swMaxLuminosita = (SwitchCompat) findViewById(R.id.set_max_luminosita);
        swMaxLuminosita.setChecked(maxLuminosita);
        swMaxLuminosita.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_max_luminosita", isChecked);
                editor.apply();
                updateSwitchColor(swMaxLuminosita);
            }
        });
        updateSwitchColor(swMaxLuminosita);

        /*********** SW COLLAPSING TOOLBAR ************/
        swCollaps=(SwitchCompat) findViewById(R.id.SetCollapsingToolbar);
        swCollaps.setChecked(isCollapsingToolbar());
        swCollaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_collaps_toolbar", !isCollapsingToolbar());
                editor.apply();
                updateSwitchColor(swCollaps);
            }
        });
        updateSwitchColor(swCollaps);
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
                updateSwitchColor(swStatusBar);
            }
        });
        updateSwitchColor(swStatusBar);

        /*********** SW DARK THEME ********************/
        swDarkTheme=(SwitchCompat) findViewById(R.id.SetDarkTheme);
        swDarkTheme.setChecked(isDarkTheme());
        swDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("set_dark_theme", !isDarkTheme());
                editor.apply();
                updateSwitchColor(swDarkTheme);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        updateSwitchColor(swDarkTheme);

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
                updateSwitchColor(swNavBar);
                if (isNavigationBarColored())
                    getWindow().setNavigationBarColor(getPrimaryColor());
                else
                    getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
            }
        });
        updateSwitchColor(swNavBar);

    }

    public void updateSwitchColor(SwitchCompat sw){
        if(sw.isChecked())
            sw.getThumbDrawable().setColorFilter(getAccentColor(), PorterDuff.Mode.MULTIPLY);
        else
            sw.getThumbDrawable().setColorFilter(getTextColor(), PorterDuff.Mode.MULTIPLY);

        sw.getTrackDrawable().setColorFilter(getBackgroundColor(), PorterDuff.Mode.MULTIPLY);

    }

    public void PrimaryColorPikerDialogShow(){
        final AlertDialog.Builder PrimaryPikerDialog;
        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

        if (isDarkTheme()) PrimaryPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Dark);
        else PrimaryPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Light);

        final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
        final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary);
        final LineColorPicker colorPicker2 = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary2);
        final TextView title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_primary_title);
        CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_primary_card);

        colorPicker.setColors(ColorPalette.getBaseColors(getApplicationContext()));
        for (int i : colorPicker.getColors())
            for (int i2 : ColorPalette.getColors(getBaseContext(), i))
                if (i2 == getPrimaryColor()) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPalette.getColors(getBaseContext(), i));
                    colorPicker2.setSelectedColor(i2);
                    break;}

        title.setBackgroundColor(getPrimaryColor());
        if (!isDarkTheme())
            cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.cp_PrimaryLight));
        else cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryDark));

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if(isTraslucentStatusBar()) {
                    getWindow().setStatusBarColor(getOscuredColor(getPrimaryColor()));
                } else getWindow().setStatusBarColor(c);

                toolbar.setBackgroundColor(c);
                title.setBackgroundColor(c);
                colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });
        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if(isTraslucentStatusBar()) {
                    getWindow().setStatusBarColor(getOscuredColor(c));
                } else getWindow().setStatusBarColor(c);

                toolbar.setBackgroundColor(c);
                title.setBackgroundColor(c);
                if (isNavigationBarColored())
                    getWindow().setNavigationBarColor(c);
                else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
            }
        });

        PrimaryPikerDialog.setView(Accent_dialogLayout);
        PrimaryPikerDialog.setNeutralButton(getString(R.string.cancel_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isTraslucentStatusBar()) {
                    getWindow().setStatusBarColor(getOscuredColor(getPrimaryColor()));
                } else getWindow().setStatusBarColor(getPrimaryColor());

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
                if(isTraslucentStatusBar()) {
                    getWindow().setStatusBarColor(getOscuredColor(getPrimaryColor()));
                } else {
                    getWindow().setStatusBarColor(getPrimaryColor());
                }
            }
        });
        PrimaryPikerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                if(isTraslucentStatusBar()) {
                    getWindow().setStatusBarColor(getOscuredColor(getPrimaryColor()));
                } else getWindow().setStatusBarColor(getPrimaryColor());

                toolbar.setBackgroundColor(getPrimaryColor());
                if (isNavigationBarColored())
                    getWindow().setNavigationBarColor(getPrimaryColor());
                else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
            }
        });
        PrimaryPikerDialog.show();
    }

    public void AccentColorPikerDialogShow(){

        final AlertDialog.Builder AccentPikerDialog;

        SP = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);
        if (isDarkTheme())
            AccentPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Dark);
        else
            AccentPikerDialog = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog_Light);

        final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
        final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerAccent);
        final TextView title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_accent_title);
        CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_accent_card);

        colorPicker.setColors(ColorPalette.getAccentColors(getApplicationContext()));
        colorPicker.setSelectedColor(getAccentColor());
        title.setBackgroundColor(getAccentColor());

        if (!isDarkTheme())
            cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryLight));
        else cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.cp_PrimaryDark));

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                title.setBackgroundColor(c);
                txtGT.setTextColor(colorPicker.getColor());
                txtTT.setTextColor(colorPicker.getColor());
                txtPT.setTextColor(colorPicker.getColor());
                txtSBC.setTextColor(colorPicker.getColor());

                if(swCollaps.isChecked()) swCollaps.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);
                if(swDarkTheme.isChecked()) swDarkTheme.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);
                if(swNavBar.isChecked()) swNavBar.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);
                if(swStatusBar.isChecked()) swStatusBar.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);
                if(swMaxLuminosita.isChecked()) swMaxLuminosita.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);
                if(swPictureOrientation.isChecked()) swPictureOrientation.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);
                if(swApplyTheme3thAct.isChecked()) swApplyTheme3thAct.getThumbDrawable().setColorFilter(colorPicker.getColor(), PorterDuff.Mode.MULTIPLY);

            }
        });

        AccentPikerDialog.setView(Accent_dialogLayout);

        AccentPikerDialog.setNeutralButton(getString(R.string.cancel_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                txtGT.setTextColor(getAccentColor());
                txtTT.setTextColor(getAccentColor());
                txtPT.setTextColor(getAccentColor());
                txtSBC.setTextColor(getAccentColor());

                updateSwitchColor(swCollaps);
                updateSwitchColor(swDarkTheme);
                updateSwitchColor(swNavBar);
                updateSwitchColor(swStatusBar);
                updateSwitchColor(swMaxLuminosita);
                updateSwitchColor(swPictureOrientation);
                updateSwitchColor(swApplyTheme3thAct);
            }
        });
        AccentPikerDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putInt("accent_color", colorPicker.getColor());
                editor.apply();
                updateTheme();
                txtGT.setTextColor(getAccentColor());
                txtPT.setTextColor(getAccentColor());
                txtTT.setTextColor(getAccentColor());
                txtSBC.setTextColor(getAccentColor());

                updateSwitchColor(swCollaps);
                updateSwitchColor(swDarkTheme);
                updateSwitchColor(swNavBar);
                updateSwitchColor(swStatusBar);
                updateSwitchColor(swMaxLuminosita);
                updateSwitchColor(swPictureOrientation);
                updateSwitchColor(swApplyTheme3thAct);

            }
        });
        AccentPikerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                txtGT.setTextColor(getAccentColor());
                txtTT.setTextColor(getAccentColor());
                txtPT.setTextColor(getAccentColor());
                txtSBC.setTextColor(getAccentColor());

                updateSwitchColor(swCollaps);
                updateSwitchColor(swDarkTheme);
                updateSwitchColor(swNavBar);
                updateSwitchColor(swStatusBar);
                updateSwitchColor(swMaxLuminosita);
                updateSwitchColor(swPictureOrientation);
                updateSwitchColor(swApplyTheme3thAct);
            }
        });
        AccentPikerDialog.show();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
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
        if (isNavigationBarColored())
            getWindow().setNavigationBarColor(getPrimaryColor());
        else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));

        /**** Recent App */
        setRecentApp(getString(R.string.settings));

        txtGT.setTextColor(getAccentColor());
        txtTT.setTextColor(getAccentColor());
        txtPT.setTextColor(getAccentColor());

        setThemeOnChangeListener();
    }

    public void setThemeOnChangeListener(){

        /** BackGround **/

        LinearLayout bg = (LinearLayout) findViewById(R.id.setting_background);

        /** Cards **/

        CardView cvGeneral = (CardView) findViewById(R.id.general_setting_card);
        CardView cvTheme = (CardView) findViewById(R.id.theme_setting_card);
        CardView cvPicture = (CardView) findViewById(R.id.preview_picture_setting_card);

        /** Icons **/

        IconicsImageView imgOrient = (IconicsImageView) findViewById(R.id.ll_switch_picture_orientation_icon);
        IconicsImageView imgMax = (IconicsImageView) findViewById(R.id.ll_switch_max_luminosita_icon);
        IconicsImageView imgTSB = (IconicsImageView) findViewById(R.id.Traslucent_StatusBar_Icon);
        IconicsImageView imgCI = (IconicsImageView) findViewById(R.id.collapsing_toolbar_Icon);
        IconicsImageView imgAT = (IconicsImageView) findViewById(R.id.ll_apply_theme_3thAct_icon);
        IconicsImageView imgPC = (IconicsImageView) findViewById(R.id.PrimaryColor_Icon);
        IconicsImageView imgAC = (IconicsImageView) findViewById(R.id.accentColor_Icon);
        IconicsImageView imgDT = (IconicsImageView) findViewById(R.id.DarkTheme_Icon);
        IconicsImageView imgNB = (IconicsImageView) findViewById(R.id.NavBar_Icon);

        int color = isDarkTheme()
                ? ColorPalette.getLightBackgroundColor(getApplicationContext())
                : ColorPalette.getDarkBackgroundColor(getApplicationContext());

        imgMax.setColor(color);
        imgAT.setColor(color);
        imgCI.setColor(color);
        imgTSB.setColor(color);
        imgPC.setColor(color);
        imgAC.setColor(color);
        imgDT.setColor(color);
        imgNB.setColor(color);
        imgOrient.setColor(color);

        /** TextViews **/

        TextView txtMax = (TextView) findViewById(R.id.max_luminosita_Item);
        TextView txtOrient = (TextView) findViewById(R.id.picture_orientation_Item);
        TextView txtATT = (TextView) findViewById(R.id.apply_theme_3thAct_title);
        TextView txtSBT = (TextView) findViewById(R.id.seek_bar_alpha_title);
        TextView txtC = (TextView) findViewById(R.id.collapsing_toolbar_Item);
        TextView txtTSB = (TextView) findViewById(R.id.Traslucent_StatusBar_Item);
        TextView txtPC = (TextView) findViewById(R.id.PrimaryColor_Item);
        TextView txtAC = (TextView) findViewById(R.id.accentColor_Item);
        TextView txtDT = (TextView) findViewById(R.id.DarkTheme_Item);
        TextView txtNB = (TextView) findViewById(R.id.NavBar_Item);


        txtMax.setTextColor(color);
        txtOrient.setTextColor(color);
        txtATT.setTextColor(color);
        txtSBT.setTextColor(color);
        txtC.setTextColor(color);
        txtTSB.setTextColor(color);
        txtPC.setTextColor(color);
        txtAC.setTextColor(color);
        txtDT.setTextColor(color);
        txtNB.setTextColor(color);


        color = ContextCompat.getColor(SettingActivity.this, isDarkTheme()
                ? R.color.md_dark_cards
                : R.color.md_light_cards);

        cvGeneral.setBackgroundColor(color);
        cvTheme.setBackgroundColor(color);
        cvPicture.setBackgroundColor(color);

        bg.setBackgroundColor(getBackgroundColor());
    }
}
