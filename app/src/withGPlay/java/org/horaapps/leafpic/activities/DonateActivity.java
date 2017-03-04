package org.horaapps.leafpic.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SecretConstants;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.CustomTabService;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.inapppurchase.IabHelper;
import org.horaapps.leafpic.util.inapppurchase.IabResult;
import org.horaapps.leafpic.util.inapppurchase.Purchase;

/**
 * Created by Jibo on 02/03/2016.
 */
public class DonateActivity extends ThemedActivity {

    private Toolbar toolbar;
    private CustomTabService cts;
    private ScrollView scr;
    private IabHelper mHelper;
    private SeekBar bar; private int progress = 2;
    private Button btnDonateIap;
    private Button btnDonatePP;

    private final IabHelper.OnConsumeFinishedListener mPurchaseFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                @Override
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isFailure()) {
                        Log.d("iap", "Error purchasing: " + result);


                    } else if (purchase.getSku().contains("donation")) {
                        Toast.makeText(DonateActivity.this, "Thanks!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_donate);
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
        bar = (SeekBar) findViewById(R.id.seek_bar_donations);

        setNavBarColor();
        cts = new CustomTabService(DonateActivity.this);
        scr = (ScrollView)findViewById(org.horaapps.leafpic.R.id.donateAct_scrollView);
        initUi();
        mHelper = new IabHelper(this, SecretConstants.getBase64EncodedPublicKey(getApplicationContext()));
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d("donateAct", "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null)
            mHelper.dispose();

        mHelper = null;
    }


    private void initUi(){

        /**** ToolBar *****/
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(
                new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_arrow_left)
                        .color(Color.WHITE)
                        .sizeDp(19));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(getString(org.horaapps.leafpic.R.string.donate));

        /**** DONATE PLAY STORE ****/
        btnDonateIap = (Button) findViewById(R.id.button_donate_play_store);
        btnDonateIap.setText(String.format("%s %d€", getString(R.string.donate).toUpperCase(), progress));

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) progress = 2;
                else progress = (i+1)*2;
                btnDonateIap.setText(String.format("%s %d€", getString(R.string.donate).toUpperCase(), progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        /**** DONATE PAY PAL ****/
        btnDonatePP = (Button) findViewById(R.id.button_donate_paypal);
        btnDonatePP.setText(getString(R.string.donate).toUpperCase());

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
                ClipData clip = ClipData.newPlainText("HoraApps BTC", ((TextView) v).getText());
                clipboard.setPrimaryClip(clip);
                StringUtils.showToast(getApplicationContext(),getString(org.horaapps.leafpic.R.string.address_copied));
                return true;
            }
        });

        findViewById(R.id.button_donate_play_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHelper != null) mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(DonateActivity.this, "donation_" + progress, 123, new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        mHelper.consumeAsync(info, mPurchaseFinishedListener);
                    }
                });
            }
        });
    }

    @Override
    public void updateUiElements() {
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(org.horaapps.leafpic.R.string.donate));

        /**** Buttons & Bar ****/
        themeSeekBar(bar);
        themeButton(btnDonateIap);
        themeButton(btnDonatePP);


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
    }
}