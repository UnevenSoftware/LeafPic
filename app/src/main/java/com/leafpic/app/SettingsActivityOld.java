package com.leafpic.app;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leafpic.app.utils.ColorPalette;
import com.leafpic.app.Views.ThemedActivity;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


public class SettingsActivityOld extends ThemedActivity{
    /* TODO rewrite all settings activity  */

    SharedPreferences SP;
    Toolbar bar;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(bar, 0); // insert at top

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        bar.setTitle(getString(R.string.action_settings));
        bar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_arrow_back)
                .color(Color.WHITE)
                .sizeDp(18));
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initUiTweaks();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        /*SP.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(
                    SharedPreferences prefs, String key) {
                if (key.equals("nav_bar")) {
                    updateTheme();
                   // initUiTweaks();
                }
            }
        });*/

    }


    @Override
    public void onPostResume() {
        super.onPostResume();
        initUiTweaks();
    }

    public void initUiTweaks() {
        getWindow().setStatusBarColor(getPrimaryColor());

        if (isNavigationBarColored())
            getWindow().setNavigationBarColor(getPrimaryColor());
        else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(),R.color.md_black_1000));

        bar.setBackgroundColor(getPrimaryColor());

        setRecentApp(getString(R.string.app_name));
    }

    public class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);


            //ACCENT COLOR PIKER********************************************************************

            Preference accent_preference = findPreference("accent_color");
            accent_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final AlertDialog.Builder AccentPikerDialog;
                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivityOld.this);
                    if (isDarkTheme())
                        AccentPikerDialog = new AlertDialog.Builder(SettingsActivityOld.this, R.style.AlertDialog_Dark);
                    else
                        AccentPikerDialog = new AlertDialog.Builder(SettingsActivityOld.this, R.style.AlertDialog_Light);

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
                        }
                    });

                    AccentPikerDialog.setView(Accent_dialogLayout);

                    AccentPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AccentPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("accent_color", colorPicker.getColor());
                            editor.apply();
                        }
                    });
                    AccentPikerDialog.show();
                    return false;
                }
            });

            //PRIMARY COLOR PIKER*******************************************************************

            Preference primary_preference = findPreference("primary_color");
            primary_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final AlertDialog.Builder PrimaryPikerDialog;
                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivityOld.this);

                    if (isDarkTheme()) PrimaryPikerDialog = new AlertDialog.Builder(SettingsActivityOld.this, R.style.AlertDialog_Dark);
                    else PrimaryPikerDialog = new AlertDialog.Builder(SettingsActivityOld.this, R.style.AlertDialog_Light);

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
                                break;
                            }



                    /*colorPicker.setSelectedColor(getPrimaryColor());

                    colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));

                    colorPicker2.setSelectedColor(colorPicker.getColor());*/

                    title.setBackgroundColor(getPrimaryColor());
                    if (!isDarkTheme())
                        cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.cp_PrimaryLight));
                    else cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryDark));

                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            title.setBackgroundColor(c);
                            getWindow().setStatusBarColor(c);
                            bar.setBackgroundColor(c);

                            //
                            colorPicker2.setColors(ColorPalette.getColors(getApplicationContext(), colorPicker.getColor()));
                            colorPicker2.setSelectedColor(colorPicker.getColor());
                        }
                    });
                    colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            title.setBackgroundColor(c);
                            getWindow().setStatusBarColor(c);
                            bar.setBackgroundColor(c);
                        }
                    });

                    PrimaryPikerDialog.setView(Accent_dialogLayout);
                    PrimaryPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getWindow().setStatusBarColor(getPrimaryColor());
                            bar.setBackgroundColor(getPrimaryColor());
                            dialog.cancel();
                        }
                    });
                    PrimaryPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("primary_color", colorPicker2.getColor());
                            editor.apply();
                            updateTheme();
                            initUiTweaks();
                        }
                    });
                    PrimaryPikerDialog.show();
                    return false;

                }
            });

        }
    }
}
