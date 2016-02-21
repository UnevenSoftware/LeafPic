package com.leafpic.app;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
            Preference p = findPreference("primary_color");
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int[] j =  {Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW};
                    /**/
                    //SHIFT COLOR PIKLER
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.colorpiker);
                    dialog.setTitle("Primary Color");
                    LineColorPicker colorPicker;
                    colorPicker = (LineColorPicker) findViewById(R.id.picker);

                    // set color palette
                    colorPicker.setColors(j);

                    // set selected color [optional]
                    colorPicker.setSelectedColor(Color.RED);

                    // set on change listener
                    colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int c) {
                            //Log.d(TAG, "Selected color " + Integer.toHexString(c));
                            Toast.makeText(getActivity(), "Selected color " + Integer.toHexString(c),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    // get selected color
                    int color = colorPicker.getColor();
                    dialog.show();












                    /*
                    //LOBSTER
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.colorpiker);
                    dialog.setTitle("Primary Color");

                    // set the custom dialog components - text, image and button

                    LobsterPicker lobsterPicker = (LobsterPicker) findViewById(R.id.lobsterpicker);
                    LobsterShadeSlider shadeSlider = (LobsterShadeSlider) findViewById(R.id.shadeslider);
                    //To retrieve the selected color use
                    lobsterPicker.getColor();

                    //You'r also able to add a listener
                    lobsterPicker.addOnColorListener(new OnColorListener() {
                        @Override
                        public void onColorChanged(@ColorInt int color) {

                        }

                        @Override
                        public void onColorSelected(@ColorInt int color) {

                        }
                    });
                    dialog.show();
                    */





                    /*
                    //NORMAL
                    // custom dialog
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.custom_color_piker_primary);
                    dialog.setTitle("Primary Color");

                    // set the custom dialog components - text, image and button
                    TextView grey = (TextView) dialog.findViewById(R.id.grey);
                    TextView green = (TextView) dialog.findViewById(R.id.green);
                    TextView amber = (TextView) dialog.findViewById(R.id.amber);
                    TextView orange = (TextView) dialog.findViewById(R.id.orange);

                    Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    */
                    return false;
                }
            });
        }
    }
}
