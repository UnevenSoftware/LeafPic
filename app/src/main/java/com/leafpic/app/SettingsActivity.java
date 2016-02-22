package com.leafpic.app;

import android.content.SharedPreferences;
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.status_bar));

            SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean NavBar = SP.getBoolean("nav_bar", false);
            /**** Nav Bar ****/
            if (NavBar)
                getWindow().setNavigationBarColor(getColor(R.color.toolbar));
            else getWindow().setNavigationBarColor(getColor(R.color.md_black_1000));

            /**** Status Bar */
            getWindow().setStatusBarColor(getColor(R.color.primary));
            /*
            if (SP.getBoolean("set_dark_theme", false)){
                setTheme(R.style.PreferencesThemeLight);
            }else {
                setTheme(R.style.PreferencesThemeDark);
            }
            */
        }
    }

    public class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);



            //ACCENT COLOR PIKER
            Preference accent_preference = findPreference("accent_color");
            accent_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int[] cpColors = {
                            getColor(R.color.accent_red),
                            getColor(R.color.accent_pink),
                            getColor(R.color.accent_purple),
                            getColor(R.color.accent_deep_purple),
                            getColor(R.color.accent_indago),
                            getColor(R.color.accent_blue),
                            getColor(R.color.accent_cyan),
                            getColor(R.color.accent_teal),
                            getColor(R.color.accent_green),
                    };

                    final View dialoglayout = getLayoutInflater().inflate(R.layout.color_piker_accent, null);
                    final AlertDialog.Builder builder12 = new AlertDialog.Builder(SettingsActivity.this);
                    final LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.pickerAccent);

                    // set color palette
                    colorPicker.setColors(cpColors);
                    colorPicker.setSelectedColor(R.color.accent_red);
                    //Object
                    TextView Ok = (TextView) dialoglayout.findViewById(R.id.cp_accent_ok);
                    CardView cv=(CardView) dialoglayout.findViewById(R.id.cp_accent_card);

                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    if (SP.getBoolean("set_dark_theme", false)==false){
                        cv.setBackgroundColor(getColor(R.color.cardview_light_background));
                        Ok.setTextColor(getColor(R.color.md_black_1000));
                    }else{
                        cv.setBackgroundColor(getColor(R.color.cardview_dark_background));
                        Ok.setTextColor(getColor(R.color.md_white_1000));
                    }

                    // set on change listener
                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            TextView Title = (TextView) dialoglayout.findViewById(R.id.cp_accent_title);
                            TextView Ok = (TextView) dialoglayout.findViewById(R.id.cp_accent_ok);
                            Title.setBackgroundColor(c);
                            Ok.setTextColor(c);
                            //Toast.makeText(getBaseContext(), "Selected color " + Integer.toHexString(c),
                            //        Toast.LENGTH_SHORT).show();
                        }
                    });
                    Ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("accent_color", colorPicker.getColor());
                            editor.commit();
                            //finish();chiude l'activity
                        }
                    });
                    builder12.setView(dialoglayout);
                    builder12.show();
                    //int color = colorPicker.getColor();
                    return false;
                }
            });









            //PRIMARY COLOR PIKER
            Preference primary_preference = findPreference("primary_color");
            primary_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int[] cpColors = {
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
                    };

                    final View dialoglayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
                    final AlertDialog.Builder builder12 = new AlertDialog.Builder(SettingsActivity.this);
                    final LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.pickerPrimary);

                    // set color palette
                    colorPicker.setColors(cpColors);
                    colorPicker.setSelectedColor(R.color.accent_red);
                    //Object
                    TextView Ok = (TextView) dialoglayout.findViewById(R.id.cp_primary_ok);
                    CardView cv=(CardView) dialoglayout.findViewById(R.id.cp_primary_card);

                    SP = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                    if (SP.getBoolean("set_dark_theme", false)==false){
                        cv.setBackgroundColor(getColor(R.color.cardview_light_background));
                        Ok.setTextColor(getColor(R.color.md_black_1000));
                    }else{
                        cv.setBackgroundColor(getColor(R.color.cardview_dark_background));
                        Ok.setTextColor(getColor(R.color.md_white_1000));
                    }

                    // set on change listener
                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            TextView Title = (TextView) dialoglayout.findViewById(R.id.cp_primary_title);
                            TextView Ok = (TextView) dialoglayout.findViewById(R.id.cp_primary_ok);
                            Title.setBackgroundColor(c);
                            Ok.setTextColor(c);
                            //Toast.makeText(getBaseContext(), "Selected color " + Integer.toHexString(c),
                            //        Toast.LENGTH_SHORT).show();
                        }
                    });
                    Ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor editor = SP.edit();
                            editor.putInt("Primary_Color", colorPicker.getColor());
                            editor.commit();
                            //finish();chiude l'activity
                        }
                    });
                    builder12.setView(dialoglayout);
                    builder12.show();
                    //int color = colorPicker.getColor();
                    return false;
                }
            });
        }
    }
}
