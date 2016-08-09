package org.horaapps.leafpic.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.horaapps.leafpic.Views.ThemedActivity;
import org.horaapps.leafpic.utils.CustomTabService;
import org.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

/**
 * Created by Jibo on 02/03/2016.
 */
public class DonateActivity extends ThemedActivity {

    private Toolbar toolbar;

    /**** Title Cards ***/
    private CustomTabService cts;

    /**** Scroll View*/
    private ScrollView scr;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_donate);
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);

        setNavBarColor();
        cts = new CustomTabService(DonateActivity.this, getPrimaryColor());
        scr = (ScrollView)findViewById(org.horaapps.leafpic.R.id.donateAct_scrollView);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        setTheme();
    }

    private void setTheme(){

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
        toolbar.setTitle(getString(org.horaapps.leafpic.R.string.donate));

        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(org.horaapps.leafpic.R.string.donate));

        /**** Title Cards ***/
        ((TextView) findViewById(org.horaapps.leafpic.R.id.team_name)).setTextColor(getAccentColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_googleplay_item_title)).setTextColor(getAccentColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_paypal_item_title)).setTextColor(getAccentColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_bitcoin_item_title)).setTextColor(getAccentColor());

        /** BackGround **/
        findViewById(org.horaapps.leafpic.R.id.donate_background).setBackgroundColor(getBackgroundColor());

        /** Cards **/
        int color = getCardBackgroundColor();

        ((CardView) findViewById(org.horaapps.leafpic.R.id.donate_googleplay_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.donate_paypal_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.donate_bitcoin_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.donate_header_card)).setCardBackgroundColor(color);

        /** Icons **/
        color = getIconColor();

        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.donate_googleplay_icon_title)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.donate_paypal_icon_title)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.donate_bitcoin_icon_title)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.donate_header_icon)).setColor(color);

        /** TextViews **/
        color = getTextColor();

        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_googleplay_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_paypal_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_bitcoin_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donate_header_item)).setTextColor(color);

        /***** ScrolView *****/
        setScrollViewColor(scr);

        /** ACTIONS **/
        findViewById(org.horaapps.leafpic.R.id.button_donate_paypal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cts.launchUrl("https://www.paypal.me/HoraApps");
            }
        });

        findViewById(org.horaapps.leafpic.R.id.donate_bitcoin_item).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied to Clipboard", ((TextView) v).getText());
                clipboard.setPrimaryClip(clip);
                StringUtils.showToast(getApplicationContext(),getString(org.horaapps.leafpic.R.string.address_copied));
                return true;
            }
        });
    }
}