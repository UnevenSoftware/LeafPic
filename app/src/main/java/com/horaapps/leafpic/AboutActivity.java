package com.horaapps.leafpic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.CustomTabService;
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
    TextView txtLP;
    TextView txtAT;
    TextView txtSU;
    TextView txtLI;
    TextView txtST;
    TextView txtSendMailDonald;
    TextView txtSendMailGilbert;

    /**** CustomTabService*/
    CustomTabService cts;

    /**** Buttons ***/

    /**** Scroll View*/
    ScrollView scr;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtLP = (TextView) findViewById(R.id.about_app_title);
        txtAT = (TextView) findViewById(R.id.about_authors_title);
        txtSU = (TextView) findViewById(R.id.about_support_title);
        txtLI = (TextView) findViewById(R.id.about_license_title);
        txtST = (TextView) findViewById(R.id.about_special_thanks_title);
        txtSendMailDonald = (TextView) findViewById(R.id.about_author_donald_mail_item);
        txtSendMailGilbert = (TextView) findViewById(R.id.about_author_gilbert_mail_item);
        setNavBarColor();
        cts = new CustomTabService(AboutActivity.this,getPrimaryColor());
        scr = (ScrollView)findViewById(R.id.aboutAct_scrollView);
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
        txtLI.setTextColor(getAccentColor());
        txtST.setTextColor(getAccentColor());
        txtSendMailDonald.setTextColor(getAccentColor());
        txtSendMailGilbert.setTextColor(getAccentColor());

        /***** Buttons *****/


        /***** ScrolView *****/
        setScrollViewColor(scr);

        setThemeOnChangeListener();
        ClickListeners();
    }

    public void ClickListeners(){

        ///GitHub
        findViewById(R.id.ll_about_support_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(Intent.ACTION_VIEW);
                //i.setData(Uri.parse("https://github.com/HoraApps/LeafPic"));
                //startActivity(i);
                cts.launchUrl("https://github.com/HoraApps/LeafPic");

            }
        });

        //Crowdin
        findViewById(R.id.ll_about_support_translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://crowdin.com/project/leafpic");}
        });

        /*** Donald Shtjefni ***/
        //G+
        findViewById(R.id.about_author_donald_googleplus_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://plus.google.com/103359244653769120543/about");}
        });
        //Github
        findViewById(R.id.about_author_donaldt_github_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://github.com/DNLDsht");}
        });
        //Send Mail
        findViewById(R.id.about_author_donald_mail_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"barbanish@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "LeafPic QUESTION!");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.about_send_mail).toString().toLowerCase()+"..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(AboutActivity.this, getString(R.string.send_mail_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*** Gilbert Ndresaj ***/
        //Github
        findViewById(R.id.about_author_gilbert_googleplus_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://plus.google.com/118430643662868782426/about");}
        });
        //Github
        findViewById(R.id.about_author_gilbert_github_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://github.com/Mow3l");}
        });
        //Send Mail
        findViewById(R.id.about_author_gilbert_mail_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"jibo95@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "LeafPic QUESTION!");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.about_send_mail).toString().toLowerCase()+"..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(AboutActivity.this, getString(R.string.send_mail_error), Toast.LENGTH_SHORT).show();
                }
            }
        });



        /*** SPECIAL THANKS ***/
        /*** Patryk Goworowski ***/
        //G+

        findViewById(R.id.about_patryk_goworowski_googleplus_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://plus.google.com/109304801957014561872/about");}
        });
        //Twitter
        findViewById(R.id.about_patryk_goworowski_twitter_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://twitter.com/socko_pack");}
        });




        //License
        findViewById(R.id.ll_about_license).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://github.com/HoraApps/LeafPic/blob/master/LICENSE");}
        });

        //Libs
        findViewById(R.id.ll_about_libs).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {licenseDialog();}
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
        CardView cvLicense = (CardView) findViewById(R.id.about_license_card);
        //CardView cvSpecialThanks = (CardView) findViewById(R.id.about_special_thanks_card);

        int color = getCardBackgroundColor();
        cvApp.setCardBackgroundColor(color);
        cvAuthor.setCardBackgroundColor(color);
        cvSupport.setCardBackgroundColor(color);
        cvLicense.setCardBackgroundColor(color);
        //cvSpecialThanks.setBackgroundColor(color);

        /** Icons **/
        //ABOUT APP
        //IconicsImageView imgAAV = (IconicsImageView) findViewById(R.id.about_version_icon);
        IconicsImageView imgAALL = (IconicsImageView) findViewById(R.id.about_libs_icon);
        IconicsImageView imgALicense = (IconicsImageView) findViewById(R.id.about_license_icon);

        //ABOUT AUTHOR
        IconicsImageView imgDonald = (IconicsImageView) findViewById(R.id.about_author_donald_icon);
        IconicsImageView imgGilbert = (IconicsImageView) findViewById(R.id.about_author_gilbert_icon);

        //SPECIAL THANKS
        IconicsImageView imgPatryk = (IconicsImageView) findViewById(R.id.about_patryk_goworowski_icon);

        //ABOUT SUPPORT
        IconicsImageView imgSRate = (IconicsImageView) findViewById(R.id.about_support_translate_icon);
        IconicsImageView imgSTranslate = (IconicsImageView) findViewById(R.id.about_support_rate_icon);
        IconicsImageView imgSGitHub = (IconicsImageView) findViewById(R.id.about_support_github_icon);

        color = getIconColor();
        //imgAAV.setColor(color);
        imgAALL.setColor(color);
        imgDonald.setColor(color);
        imgGilbert.setColor(color);
        imgPatryk.setColor(color);
        imgSRate.setColor(color);
        imgSTranslate.setColor(color);
        imgSGitHub.setColor(color);
        imgALicense.setColor(color);


        /** TextViews **/
        //TextView txtAV = (TextView) findViewById(R.id.about_version_item);
        TextView txtAL = (TextView) findViewById(R.id.about_libs_item);
        TextView txtLDesc = (TextView) findViewById(R.id.about_app_light_description);
        TextView txtDName = (TextView) findViewById(R.id.about_author_donald_item);
        TextView txtGName = (TextView) findViewById(R.id.about_author_gilbert_item);
        TextView txtPatrykName = (TextView) findViewById(R.id.about_patryk_goworowski_item);
        TextView txtSRate = (TextView) findViewById(R.id.about_support_rate_item);
        TextView txtSTranslate = (TextView) findViewById(R.id.about_support_translate_item);
        TextView txtSGitHub = (TextView) findViewById(R.id.about_support_github_item);
        TextView txtALicense = (TextView) findViewById(R.id.about_license_item);
        TextView txtALicense_Sub = (TextView) findViewById(R.id.about_license_item_sub);

        color=getTextColor();
        //txtAV.setTextColor(color);
        txtLDesc.setTextColor(color);
        txtAL.setTextColor(color);
        txtDName.setTextColor(color);
        txtGName.setTextColor(color);
        txtPatrykName.setTextColor(color);
        txtSRate.setTextColor(color);
        txtSTranslate.setTextColor(color);
        txtSGitHub.setTextColor(color);
        txtALicense.setTextColor(color);

        /** Sub Text Views**/
        TextView txtAV_Sub = (TextView) findViewById(R.id.about_version_item_sub);
        TextView txtAL_Sub = (TextView) findViewById(R.id.about_libs_item_sub);
        TextView txtDName_Sub = (TextView) findViewById(R.id.about_author_donald_item_sub);
        TextView txtGName_Sub = (TextView) findViewById(R.id.about_author_gilbert_item_sub);
        TextView txtPatrykName_Sub = (TextView) findViewById(R.id.about_patryk_goworowski_item_sub);
        TextView txtSRate_Sub = (TextView) findViewById(R.id.about_support_rate_item_sub);
        TextView txtSTranslate_Sub = (TextView) findViewById(R.id.about_support_translate_item_sub);
        TextView txtSGitHub_Sub = (TextView) findViewById(R.id.about_support_github_item_sub);

        txtAV_Sub.setText(BuildConfig.VERSION_NAME);

        color=getSubTextColor();
        txtAV_Sub.setTextColor(color);
        txtAL_Sub.setTextColor(color);
        txtDName_Sub.setTextColor(color);
        txtGName_Sub.setTextColor(color);
        txtPatrykName_Sub.setTextColor(color);
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