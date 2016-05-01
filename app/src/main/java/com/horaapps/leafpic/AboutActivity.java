package com.horaapps.leafpic;

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
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Created by Jibo on 02/03/2016.
 */
public class AboutActivity extends ThemedActivity {

    Toolbar toolbar;

    /**** Title Cards ***/
    TextView txtLP;//LEAFPIC
    TextView txtAT;
    TextView txtSU;

    /**** Buttons ***/

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtLP = (TextView) findViewById(R.id.about_app_title);
        txtAT = (TextView) findViewById(R.id.about_authors_title);
        txtSU = (TextView) findViewById(R.id.about_support_title);
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
        toolbar.setTitle(getString(R.string.about));

        /**** Status Bar ****/
        setStatusBarColor();

        /**** Nav Bar *******/
        setNavBarColor();

        /**** Recent App ****/
        setRecentApp(getString(R.string.about));

        /**** Title Cards ***/
        txtAT.setTextColor(getAccentColor());
        txtLP.setTextColor(getAccentColor());
        txtSU.setTextColor(getAccentColor());

        /***** Buttons *****/

        setThemeOnChangeListener();
        ClickListeners();
    }

    public void ClickListeners(){

        ///GitHub
        findViewById(R.id.ll_about_support_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/HoraApps/LeafPic"));
                startActivity(i);
            }
        });

        //Crowdin
        findViewById(R.id.ll_about_support_translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://crowdin.com/project/leafpic"));
                startActivity(i);
            }
        });

        //Donald
        findViewById(R.id.ll_donald).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/DNLDsht"));
                startActivity(i);
            }
        });

        //Gilbert
        findViewById(R.id.ll_gilbert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/Mow3l"));
                startActivity(i);
            }
        });

        //License
        findViewById(R.id.ll_about_license).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/HoraApps/LeafPic/blob/master/LICENSE"));
                startActivity(i);
            }
        });

        //Libs
        findViewById(R.id.ll_about_libs).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                licenseDialog();
            }
        });

    }

    public void setThemeOnChangeListener(){

        /** BackGround **/
        LinearLayout bg = (LinearLayout) findViewById(R.id.about_background);
        bg.setBackgroundColor(getBackgroundColor());

        /** Cards **/
        CardView cvApp = (CardView) findViewById(R.id.about_app_card);
        CardView cvAuthor = (CardView) findViewById(R.id.about_authors_card);
        CardView cvSupport = (CardView) findViewById(R.id.about_support_card);

        int color = getCardBackgroundColor();
        cvApp.setCardBackgroundColor(color);
        cvAuthor.setCardBackgroundColor(color);
        cvSupport.setCardBackgroundColor(color);

        /** Icons **/
        //ABOUT APP
        IconicsImageView imgAAV = (IconicsImageView) findViewById(R.id.about_version_icon);
        IconicsImageView imgAALL = (IconicsImageView) findViewById(R.id.about_libs_icon);
        IconicsImageView imgALicense = (IconicsImageView) findViewById(R.id.about_license_icon);

        //ABOUT AUTHOR
        IconicsImageView imgDonald = (IconicsImageView) findViewById(R.id.about_author_donald_icon);
        IconicsImageView imgGilbert = (IconicsImageView) findViewById(R.id.about_author_gilbert_icon);

        //ABOUT SUPPORT
        IconicsImageView imgSRate = (IconicsImageView) findViewById(R.id.about_support_translate_icon);
        IconicsImageView imgSTranslate = (IconicsImageView) findViewById(R.id.about_support_rate_icon);
        IconicsImageView imgSGitHub = (IconicsImageView) findViewById(R.id.about_support_github_icon);

        color = getIconColor();
        imgAAV.setColor(color);
        imgAALL.setColor(color);
        imgDonald.setColor(color);
        imgGilbert.setColor(color);
        imgSRate.setColor(color);
        imgSTranslate.setColor(color);
        imgSGitHub.setColor(color);
        imgALicense.setColor(color);

        /** TextViews **/
        TextView txtAV = (TextView) findViewById(R.id.about_version_item);
        TextView txtAL = (TextView) findViewById(R.id.about_libs_item);
        TextView txtDName = (TextView) findViewById(R.id.about_author_donald_item);
        TextView txtGName = (TextView) findViewById(R.id.about_author_gilbert_item);
        TextView txtSRate = (TextView) findViewById(R.id.about_support_rate_item);
        TextView txtSTranslate = (TextView) findViewById(R.id.about_support_translate_item);
        TextView txtSGitHub = (TextView) findViewById(R.id.about_support_github_item);
        TextView txtALicense = (TextView) findViewById(R.id.about_license_item);
        TextView txtALicense_Sub = (TextView) findViewById(R.id.about_license_item_sub);

        color=getTextColor();
        txtAV.setTextColor(color);
        txtAL.setTextColor(color);
        txtDName.setTextColor(color);
        txtGName.setTextColor(color);
        txtSRate.setTextColor(color);
        txtSTranslate.setTextColor(color);
        txtSGitHub.setTextColor(color);
        txtALicense.setTextColor(color);

        /** Sub Text Views**/
        TextView txtAV_Sub = (TextView) findViewById(R.id.about_version_item_sub);
        TextView txtAL_Sub = (TextView) findViewById(R.id.about_libs_item_sub);
        TextView txtDName_Sub = (TextView) findViewById(R.id.about_author_donald_item_sub);
        TextView txtGName_Sub = (TextView) findViewById(R.id.about_author_gilbert_item_sub);
        TextView txtSRate_Sub = (TextView) findViewById(R.id.about_support_rate_item_sub);
        TextView txtSTranslate_Sub = (TextView) findViewById(R.id.about_support_translate_item_sub);
        TextView txtSGitHub_Sub = (TextView) findViewById(R.id.about_support_github_item_sub);

        txtAV_Sub.setText(BuildConfig.VERSION_NAME);

        color=getSubTextColor();
        txtAV_Sub.setTextColor(color);
        txtAL_Sub.setTextColor(color);
        txtDName_Sub.setTextColor(color);
        txtGName_Sub.setTextColor(color);
        txtSRate_Sub.setTextColor(color);
        txtSTranslate_Sub.setTextColor(color);
        txtSGitHub_Sub.setTextColor(color);
        txtALicense_Sub.setTextColor(color);

    }

    public void licenseDialog(){
        final Notices notices = new Notices();
        notices.addNotice(new Notice("Glide", "http://github.com/bumptech/glide", "Copyright 2014 Google, Inc. All rights reserved.", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Ion", "http://github.com/koush/ion", "Copyright 2013 Koushik Dutta (2013)", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Android-Iconics", "http://github.com/mikepenz/Android-Iconics", "Copyright 2016 Mike Penz", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("AppIntro", "http://github.com/PaoloRotolo/AppIntro", "Copyright 2015 Paolo Rotolo\n"
                + "Copyright 2016 Maximilian Narr", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("uCrop", "http://github.com/Yalantis/uCrop", "Copyright 2016, Yalantis", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("ShiftColorPicker", "http://github.com/DASAR/ShiftColorPicker", "Copyright (c) 2015 Bogdasarov Bogdan", new MITLicense()));
        notices.addNotice(new Notice("material-ripple", "http://github.com/balysv/material-ripple", "Copyright 2015 Balys Valentukevicius", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("PhotoView", "http://github.com/chrisbanes/PhotoView", "Copyright 2011, 2012 Chris Banes.", new ApacheSoftwareLicense20()));

        new LicensesDialog.Builder(this)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .setThemeResourceId(getDialogStyle())
                .build()
                .show();
    }
}