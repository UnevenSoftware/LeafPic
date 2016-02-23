package com.leafpic.app;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


public class SettingsActivity extends AppCompatActivity {

    SharedPreferences SP;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initUiTweaks();

        //FOR ADDING TOOLBAR
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.setting_toolbar, root, false);
        root.addView(bar, 0); // insert at top

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int primaryColor = SP.getInt("primary_color", Color.rgb(0, 150, 136));//TEAL CARD BG DEFAULT
        String hexPrimaryColor = String.format("#%06X", (0xFFFFFF & primaryColor));
        bar.setBackgroundColor(Color.parseColor(hexPrimaryColor));


        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SP.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(
                            SharedPreferences prefs, String key) {
                        if (!key.equals("nav_bar")) {
                            initUiTweaks();
                            return;
                        }
                        //System.out.println(key);
                    }
                });
        /*
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if (!key.equals("nav_bar")) {
                    return;
                }

                getActivity().finish();
                final Intent intent = getActivity().getIntent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().startActivity(intent);

            }
        };
        */

    }

    @Override
    public void onResume() {
        initUiTweaks();
        super.onResume();
    }

    public void initUiTweaks() {
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //int accentColor = SP.getInt("accent_color", Color.rgb(0, 77, 64));//TEAL COLOR DEFAULT
        //String hexAccentColor = String.format("#%06X", (0xFFFFFF & accentColor));
        int primaryColor = SP.getInt("primary_color", Color.rgb(0, 150, 136));//TEAL CARD BG DEFAULT
        String hexPrimaryColor = String.format("#%06X", (0xFFFFFF & primaryColor));

        /**** Status Bar */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(hexPrimaryColor));
        }
        boolean NavBar = SP.getBoolean("nav_bar", false);
        /**** Nav Bar ****/
        if (NavBar)
            getWindow().setNavigationBarColor(Color.parseColor(hexPrimaryColor));
        else getWindow().setNavigationBarColor(getColor(R.color.md_black_1000));




        // Tool Bar
        //bar.setBackgroundColor(Color.parseColor(hexPrimaryColor));




        /*
        if (SP.getBoolean("set_dark_theme", false)){
            setTheme(R.style.PreferencesThemeLight);
        }else {
            setTheme(R.style.PreferencesThemeDark);
        }
        */
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
                    int[] accentColors = {
                            getColor(R.color.accent_red),
                            getColor(R.color.accent_pink),
                            getColor(R.color.accent_purple),
                            getColor(R.color.accent_deep_purple),
                            getColor(R.color.accent_indago),
                            getColor(R.color.accent_blue),
                            getColor(R.color.accent_cyan),
                            getColor(R.color.accent_teal),
                            getColor(R.color.accent_green),
                            getColor(R.color.accent_yellow),
                            getColor(R.color.accent_amber),
                            getColor(R.color.accent_orange),
                            getColor(R.color.accent_brown)
                    };
                    final AlertDialog.Builder AccentPikerDialog;
                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    if (SP.getBoolean("set_dark_theme", false) == true) {
                        AccentPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Dark);
                    } else {
                        AccentPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Light);
                    }
                    final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
                    final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerAccent);

                    colorPicker.setColors(accentColors);
                    colorPicker.setSelectedColor(R.color.md_red_500);
                    CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_accent_card);

                    if (SP.getBoolean("set_dark_theme", false) == false) {
                        cv.setBackgroundColor(getColor(R.color.cp_PrimaryLight));
                    } else {cv.setBackgroundColor(getColor(R.color.cp_PrimaryDark));}

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
                            dialog.cancel();
                        }
                    });
                    AccentPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("accent_color", colorPicker.getColor());
                            editor.commit();
                            Toast.makeText(getBaseContext(), "Selected color " + colorPicker.getColor(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    AccentPikerDialog.show();
                    //int color = colorPicker.getColor();
                    return false;
                }
            });

            //PRIMARY COLOR PIKER*******************************************************************

            Preference primary_preference = findPreference("primary_color");
            primary_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int[] accentColors = {
                            getColor(R.color.accent_red),
                            getColor(R.color.accent_pink),
                            getColor(R.color.accent_purple),
                            getColor(R.color.accent_deep_purple),
                            getColor(R.color.accent_indago),
                            getColor(R.color.accent_blue),
                            getColor(R.color.accent_cyan),
                            getColor(R.color.accent_teal),
                            getColor(R.color.accent_green),
                            getColor(R.color.accent_yellow),
                            getColor(R.color.accent_amber),
                            getColor(R.color.accent_orange),
                            getColor(R.color.accent_brown),
                            getColor(R.color.accent_grey),
                            getColor(R.color.accent_black)
                    };
                    final AlertDialog.Builder PrimaryPikerDialog;
                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    if (SP.getBoolean("set_dark_theme", false) == true) {
                        PrimaryPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Dark);
                    } else {
                        PrimaryPikerDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialog_Light);
                    }
                    final View Accent_dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
                    final LineColorPicker colorPicker = (LineColorPicker) Accent_dialogLayout.findViewById(R.id.pickerPrimary);

                    colorPicker.setColors(accentColors);
                    colorPicker.setSelectedColor(R.color.md_red_500);
                    CardView cv = (CardView) Accent_dialogLayout.findViewById(R.id.cp_primary_card);

                    if (SP.getBoolean("set_dark_theme", false) == false) {
                        cv.setBackgroundColor(getColor(R.color.cp_PrimaryLight));
                    } else {cv.setBackgroundColor(getColor(R.color.cp_PrimaryDark));}

                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            TextView Title = (TextView) Accent_dialogLayout.findViewById(R.id.cp_primary_title);
                            Title.setBackgroundColor(c);
                        }
                    });
                    PrimaryPikerDialog.setView(Accent_dialogLayout);//IMPOSTO IL LAYOUT
                    PrimaryPikerDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    PrimaryPikerDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("primary_color", colorPicker.getColor());
                            editor.commit();
                            Toast.makeText(getBaseContext(), "Selected color " + colorPicker.getColor(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    PrimaryPikerDialog.show();
                    //int color = colorPicker.getColor();
                    return false;
                }
            });

        }
    }
}
