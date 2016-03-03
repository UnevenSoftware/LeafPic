package com.leafpic.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leafpic.app.Views.ThemedActivity;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by Jibo on 02/03/2016.
 */
public class SettingTry extends ThemedActivity {

    Toolbar toolbar;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        setTheme();

        //////////////////////////////////////////////////////////////
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
    }

    public void setTheme(){

        /**** ToolBar *****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        /**** Status Bar */
        getWindow().setStatusBarColor(getPrimaryColor());

        /**** Nav Bar ****/
        if (isNavigationBarColored())
            getWindow().setNavigationBarColor(getPrimaryColor());
        else getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));

        /**** Recent App */
        setRecentApp("Setting");
        setIconBackgroundText();
    }

    public void setIconBackgroundText(){
        //BackGround
        LinearLayout bg = (LinearLayout) findViewById(R.id.setting_background);
        //Card
        CardView cvGeneral = (CardView) findViewById(R.id.general_setting_card);
        CardView cvTheme = (CardView) findViewById(R.id.theme_setting_card);
        //Linear Layout

        SwitchCompat swCollaps=(SwitchCompat) findViewById(R.id.SetCollapsingToolbar);
        swCollaps.setChecked(thereIsCollapsing());

        SwitchCompat swDarkTheme=(SwitchCompat) findViewById(R.id.SetDarkTheme);
        swDarkTheme.setChecked(isDarkTheme());

        if(isDarkTheme()) {
            cvGeneral.setBackgroundColor(ContextCompat.getColor(SettingTry.this, R.color.md_dark_cards));
            cvTheme.setBackgroundColor(ContextCompat.getColor(SettingTry.this, R.color.md_dark_cards));
            bg.setBackgroundColor(getBackgroundColor());

            //GENERAL
            //TEXT AND ICON
            TextView txtGeneralTitle = (TextView) findViewById(R.id.general_setting_title);
            TextView txtCollaps = (TextView) findViewById(R.id.collapsing_toolbar_Item);
            ImageView imgCollapsIcon = (ImageView) findViewById(R.id.collapsing_toolbar_Icon);
            //SET COLOR
            txtGeneralTitle.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));
            txtCollaps.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));
            imgCollapsIcon.setImageResource(R.mipmap.ic_gradient_white_24dp);


            //THEME
            //TEXT AND ICON
            TextView txtTT = (TextView) findViewById(R.id.theme_setting_title);
            TextView txtPC = (TextView) findViewById(R.id.PrimaryColor_Item);
            TextView txtAC = (TextView) findViewById(R.id.accentColor_Item);
            TextView txtDT = (TextView) findViewById(R.id.DarkTheme_Item);
            TextView txtNB = (TextView) findViewById(R.id.NavBar_Item);

            txtTT.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));
            txtPC.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));
            txtAC.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));
            txtDT.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));
            txtNB.setTextColor(ContextCompat.getColor(SettingTry.this, R.color.cp_TextDark));

            ImageView imgPC = (ImageView) findViewById(R.id.PrimaryColor_Icon);
            ImageView imgAC = (ImageView) findViewById(R.id.accentColor_Icon);
            ImageView imgDT = (ImageView) findViewById(R.id.DarkTheme_Icon);
            ImageView imgNB = (ImageView) findViewById(R.id.NavBar_Icon);

            imgPC.setImageResource(R.mipmap.ic_color_lens_white_24dp);
            imgAC.setImageResource(R.mipmap.ic_colorize_white_24dp);
            imgDT.setImageResource(R.mipmap.ic_invert_colors_white_24dp);
            imgNB.setImageResource(R.mipmap.ic_video_label_white_24dp);
            ////////////

        } else {
            cvGeneral.setBackgroundColor( ContextCompat.getColor(SettingTry.this, R.color.md_light_cards));
            cvTheme.setBackgroundColor(ContextCompat.getColor(SettingTry.this, R.color.md_light_cards));
            bg.setBackgroundColor(getBackgroundColor());
        }
    }
}
