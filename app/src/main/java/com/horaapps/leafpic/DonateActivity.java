package com.horaapps.leafpic;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

/**
 * Created by Jibo on 02/03/2016.
 */
public class DonateActivity extends ThemedActivity {

    Toolbar toolbar;

    /**** Title Cards ***/
    TextView txtGP;
    TextView txtPP;
    TextView txtBT;

    /**** Buttons ***/

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtGP = (TextView) findViewById(R.id.donate_googleplay_item_title);
        txtPP = (TextView) findViewById(R.id.donate_paypal_item_title);
        txtBT = (TextView) findViewById(R.id.donate_bitcoin_item_title);
        setNavBarColor();
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
        toolbar.setTitle(getString(R.string.donate));

        /**** Status Bar ****/
        setStatusBarColor();

        /**** Nav Bar *******/
        setNavBarColor();

        /**** Recent App ****/
        setRecentApp(getString(R.string.donate));

        /**** Title Cards ***/
        txtGP.setTextColor(getAccentColor());
        txtPP.setTextColor(getAccentColor());
        txtBT.setTextColor(getAccentColor());

        /***** Buttons *****/

        setThemeOnChangeListener();
    }





    public void setThemeOnChangeListener(){

        /** BackGround **/
        LinearLayout bg = (LinearLayout) findViewById(R.id.donate_background);
        bg.setBackgroundColor(getBackgroundColor());

        /** Cards **/
        CardView cvGP = (CardView) findViewById(R.id.donate_googleplay_card);
        CardView cvPP = (CardView) findViewById(R.id.donate_paypal_card);
        CardView cvBT = (CardView) findViewById(R.id.donate_bitcoin_card);


        int color = getCardBackgroundColor();
        cvGP.setCardBackgroundColor(color);
        cvPP.setCardBackgroundColor(color);
        cvBT.setCardBackgroundColor(color);
        /** Icons **/
        //ABOUT APP
        IconicsImageView imgGP = (IconicsImageView) findViewById(R.id.donate_googleplay_icon_title);
        IconicsImageView imgPP = (IconicsImageView) findViewById(R.id.donate_paypal_icon_title);
        IconicsImageView imgBT = (IconicsImageView) findViewById(R.id.donate_bitcoin_icon_title);

        color = getIconColor();
        imgGP.setColor(color);
        imgPP.setColor(color);
        imgBT.setColor(color);


        /** TextViews **/
        TextView txtGP_CS = (TextView) findViewById(R.id.donate_googleplay_item);
        TextView txtPP_CS = (TextView) findViewById(R.id.donate_paypal_item);
        TextView txtBT_CS = (TextView) findViewById(R.id.donate_bitcoin_item);

        color=getTextColor();
        txtGP_CS.setTextColor(color);
        txtPP_CS.setTextColor(color);
        txtBT_CS.setTextColor(color);


        txtPP_CS.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://www.paypal.me/DonaldShtjefni"));
                startActivity(i);
                return true;
            }
        });

        /** ACTIONS **/
        txtBT_CS.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied to Clipboard", ((TextView) v).getText());
                clipboard.setPrimaryClip(clip);
                StringUtils.showToast(getApplicationContext(),getString(R.string.address_copied));
                return true;
            }
        });
    }
}