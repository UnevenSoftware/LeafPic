package com.leafpic.app;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leafpic.app.Base.ColorPalette;
import com.leafpic.app.Views.ThemedActivity;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


public class SettingsActivity extends ThemedActivity{

    SharedPreferences SP;
    Toolbar bar;
    LinearLayout root;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //FOR ADDING TOOLBAR
        root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(bar, 0); // insert at top

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        bar.setTitle("Settings");
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
        SP.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(
                    SharedPreferences prefs, String key) {
            updateTheme();
            initUiTweaks();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        initUiTweaks();

    }

    public void initUiTweaks() {

        /**** Status Bar */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getPrimaryColor());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), drawable.getBitmap(), getPrimaryColor()));

            if (isNavigationBarColored())
                getWindow().setNavigationBarColor(getPrimaryColor());
            else getWindow().setNavigationBarColor(getColor(R.color.md_black_1000));
        }
        bar.setBackgroundColor(getPrimaryColor());


        if (isDarkTheme()){
           // root.setBackgroundColor(getColor(R.color.act_bg_dark));
            //setTheme(R.style.PreferencesThemeDark);

        }else {
            //root.setBackgroundColor(getColor(R.color.act_bg_light));
            //setTheme(R.style.PreferencesThemeLight);
            }

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
                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    if (isDarkTheme())
                        AccentPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Dark);
                    else
                        AccentPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Light);

                    final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
                    final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerAccent);

                    colorPicker.setColors(ColorPalette.getDominantColors(getApplicationContext()));
                    colorPicker.setSelectedColor(R.color.md_red_500);
                    CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_accent_card);

                    if (!isDarkTheme())
                        cv.setBackgroundColor(getColor(R.color.cp_PrimaryLight));
                    else cv.setBackgroundColor(getColor(R.color.cp_PrimaryDark));

                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            TextView Title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_accent_title);
                            Title.setBackgroundColor(c);
                        }
                    });
                    AccentPikerDialog.setView(Accent_dialogLayout);//IMPOSTO IL LAYOUT
                    AccentPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bar.setBackgroundColor(getPrimaryColor());
                            dialog.cancel();
                        }
                    });
                    AccentPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("accent_color", colorPicker.getColor());
                            editor.apply();
                            //TODO change accent color stuff in settings act
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
                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    if (isDarkTheme())
                        PrimaryPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Dark);
                     else
                        PrimaryPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Light);

                    final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
                    final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary);

                    colorPicker.setColors(ColorPalette.getDominantColors(getApplicationContext()));
                    colorPicker.setSelectedColor(R.color.md_red_500);
                    CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_primary_card);

                    if (!isDarkTheme())
                        cv.setBackgroundColor(getColor(R.color.cp_PrimaryLight));
                     else cv.setBackgroundColor(getColor(R.color.cp_PrimaryDark));

                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            TextView Title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_primary_title);
                            Title.setBackgroundColor(c);
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                getWindow().setStatusBarColor(c);
                            bar.setBackgroundColor(c);
                        }
                    });
                    PrimaryPikerDialog.setView(Accent_dialogLayout);//IMPOSTO IL LAYOUT
                    PrimaryPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                getWindow().setStatusBarColor(getPrimaryColor());

                            bar.setBackgroundColor(getPrimaryColor());
                            dialog.cancel();
                        }
                    });
                    PrimaryPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("primary_color", colorPicker.getColor());
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
