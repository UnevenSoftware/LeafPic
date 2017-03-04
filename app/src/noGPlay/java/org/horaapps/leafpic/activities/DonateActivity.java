package org.horaapps.leafpic.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.CustomTabService;
import org.horaapps.leafpic.util.StringUtils;

/**
 * Created by Jibo on 02/03/2016.
 */
public class DonateActivity extends ThemedActivity {

    private Toolbar toolbar;
    private CustomTabService cts;
    private ScrollView scr;
    private Button btnDonatePP;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        scr = (ScrollView)findViewById(R.id.donateAct_scrollView);
        iniUi();
        cts = new CustomTabService(DonateActivity.this);
    }


    private void iniUi() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.donate_googleplay_card).setVisibility(View.GONE);

        ((Button) findViewById(R.id.button_donate_paypal)).setText(getString(R.string.donate).toUpperCase());
        findViewById(R.id.button_donate_paypal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cts.launchUrl("https://www.paypal.me/HoraApps");
            }
        });

        findViewById(R.id.donate_bitcoin_item).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("HoraApps BTC", ((TextView) v).getText());
                clipboard.setPrimaryClip(clip);
                StringUtils.showToast(getApplicationContext(), getString(R.string.address_copied));
                return true;
            }
        });
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        setStatusBarColor();
        setNavBarColor();
        themeButton(btnDonatePP);
        setRecentApp(getString(R.string.donate));

        ((TextView) findViewById(R.id.team_name)).setTextColor(getAccentColor());
        ((TextView) findViewById(R.id.donate_googleplay_item_title)).setTextColor(getAccentColor());
        ((TextView) findViewById(R.id.donate_paypal_item_title)).setTextColor(getAccentColor());
        ((TextView) findViewById(R.id.donate_bitcoin_item_title)).setTextColor(getAccentColor());

        findViewById(R.id.donate_background).setBackgroundColor(getBackgroundColor());

        int color = getCardBackgroundColor();
        ((CardView) findViewById(R.id.donate_header_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(R.id.donate_paypal_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(R.id.donate_bitcoin_card)).setCardBackgroundColor(color);

        color = getIconColor();
        ((IconicsImageView) findViewById(R.id.donate_paypal_icon_title)).setColor(color);
        ((IconicsImageView) findViewById(R.id.donate_bitcoin_icon_title)).setColor(color);
        ((IconicsImageView) findViewById(R.id.donate_header_icon)).setColor(color);

        color = getTextColor();
        ((TextView) findViewById(R.id.donate_paypal_item)).setTextColor(color);
        ((TextView) findViewById(R.id.donate_bitcoin_item)).setTextColor(color);
        ((TextView) findViewById(R.id.donate_header_item)).setTextColor(color);

        setScrollViewColor(scr);
    }
}