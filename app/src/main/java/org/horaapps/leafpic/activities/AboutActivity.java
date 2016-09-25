package org.horaapps.leafpic.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.CustomTabService;

import de.hdodenhof.circleimageview.CircleImageView;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Created by Jibo on 02/03/2016.
 */
public class AboutActivity extends ThemedActivity {

    private Toolbar toolbar;

    /**** CustomTabService*/
    private CustomTabService cts;

    /**** Scroll View*/
    private ScrollView scr;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_about);
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);

        setNavBarColor();
        cts = new CustomTabService(AboutActivity.this,getPrimaryColor());
        scr = (ScrollView)findViewById(org.horaapps.leafpic.R.id.aboutAct_scrollView);

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
        //toolbar.setTitle(getString(org.horaapps.leafpic.R.string.about));

        /**** Status Bar ****/
        setStatusBarColor();

        /**** Nav Bar *******/
        setNavBarColor();

        /**** Recent App ****/
        setRecentApp(getString(org.horaapps.leafpic.R.string.about));

        /**** Title Cards ***/
        int color=getAccentColor();
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_app_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_special_thanks_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_support_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_license_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_special_thanks_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_donald_mail_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_mail_item)).setTextColor(color);
        /***** LeafPic Header *****/
        /*
        Glide.with(this)
                .load("https://lh5.googleusercontent.com/-aX4YeN8Cvdc/VizLlektHNI/AAAAAAAATE0/c8NUmcEs46QTVf-HITTbvo3e5dja6JwoQCL0B/w958-h539-no/flat_landscape_wallpaper_by_othrod-d8crs50.png")
                .priority(Priority.HIGH)
                .animate(R.anim.fade_in)
                .into((ImageView) findViewById(R.id.leafpic_header));
        */
        /***** Donald Card *****/
        /***** Images *****/
        Glide.with(this)
                .load("https://lh3.googleusercontent.com/-4lGmk-K4r4U/Vw1Vj8yERrI/AAAAAAAANww/FIsb58PcO-U-9AfD8FXfruK1c75SZ184QCL0B/w958-h539-no/asd.png")
                .placeholder(getPlaceHolder())
                .priority(Priority.HIGH)
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into((ImageView) findViewById(org.horaapps.leafpic.R.id.donald_header_img));
        Glide.with(this)
                .load("https://lh5.googleusercontent.com/-kp20brbsTS0/VLQv60zDLQI/AAAAAAAAD9s/Wu-g8p-OvdISYmyEC9aCQXNaxxUJYfD0QCL0B/w776-h779-no/IMG_20150112_164721.jpg")
                .priority(Priority.HIGH)
                .error(new IconicsDrawable(this, "gmd-person").sizeDp(90).color(getIconColor()).paddingDp(24))
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into((CircleImageView) findViewById(org.horaapps.leafpic.R.id.donald_profile_img));
        ((CircleImageView) findViewById(org.horaapps.leafpic.R.id.donald_profile_img)).setBorderColor(getInvertedBackgroundColor());
        /***** Object *****/
        ((CardView) findViewById(org.horaapps.leafpic.R.id.about_donald_card)).setCardBackgroundColor(getCardBackgroundColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donald_shtjefni)).setTextColor(getTextColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.donald_description)).setTextColor(getSubTextColor());
        color=getAccentColor();
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_donald_mail_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_donald_googleplus_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_donald_github_item)).setTextColor(color);

        /***** Gilbert Card *****/
        /***** Images *****/
        Glide.with(this)
                .load("https://lh6.googleusercontent.com/-CQSWRHA3PMU/U1giCTxx3LI/AAAAAAAAAZU/YVnUYwwnNOEE7ob0LyHmRnbUtEtC5znIQCL0B/w958-h639-no/1397233014-game-over-samus.jpg")
                .priority(Priority.HIGH)
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into((ImageView) findViewById(org.horaapps.leafpic.R.id.gilbert_header_img));
        Glide.with(this)
                .load("https://lh6.googleusercontent.com/-gucGwwJrFMg/U4IErjai3SI/AAAAAAAAANI/YGGxrdWO88cIsIpYrYxaq2KjSDfinLTmACL0B/s779-no/PicsArt_1387801769612.jpg")
                .priority(Priority.HIGH)
                .error(new IconicsDrawable(this, "gmd-person").sizeDp(90).color(getIconColor()).paddingDp(24))
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into((CircleImageView) findViewById(org.horaapps.leafpic.R.id.gilbert_profile_img));
        ((CircleImageView) findViewById(org.horaapps.leafpic.R.id.gilbert_profile_img)).setBorderColor(getInvertedBackgroundColor());

        /***** Object *****/
        ((CardView) findViewById(org.horaapps.leafpic.R.id.about_gilbert_card)).setCardBackgroundColor(getCardBackgroundColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.gilbert_ndresaj)).setTextColor(getTextColor());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.gilbert_description)).setTextColor(getSubTextColor());
        color=getAccentColor();
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_mail_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_googleplus_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_github_item)).setTextColor(color);

        /***** ScrolView *****/
        setScrollViewColor(scr);

        setThemeOnChangeListener();
        setUpActions();
    }

    private void setUpActions(){

        //GitHub
        findViewById(org.horaapps.leafpic.R.id.ll_about_support_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cts.launchUrl("https://github.com/HoraApps/LeafPic");
            }
        });
        ///Report bug
        findViewById(org.horaapps.leafpic.R.id.ll_about_report_bug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cts.launchUrl("https://github.com/HoraApps/LeafPic/issues");
            }
        });
        //Crowdin
        findViewById(org.horaapps.leafpic.R.id.ll_about_support_translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://crowdin.com/project/leafpic");}
        });

        //Donate
        findViewById(org.horaapps.leafpic.R.id.ll_about_support_donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, DonateActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /*** Donald Shtjefni ***/
        //G+
        findViewById(org.horaapps.leafpic.R.id.about_author_donald_googleplus_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://plus.google.com/103359244653769120543/about");}
        });
        //Github
        findViewById(org.horaapps.leafpic.R.id.about_author_donald_github_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://github.com/DNLDsht");}
        });
        //Send Mail
        findViewById(org.horaapps.leafpic.R.id.about_author_donald_mail_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: dnld.sht@gmail.com"));
                try {
                    startActivity(intent);
                } catch (Exception e){
                    Toast.makeText(AboutActivity.this, getString(org.horaapps.leafpic.R.string.send_mail_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*** Gilbert Ndresaj ***/
        //Github
        findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_googleplus_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://plus.google.com/118430643662868782426/about");}
        });
        //Github
        findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_github_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://github.com/Mow3l");}
        });
        //Send Mail
        findViewById(org.horaapps.leafpic.R.id.about_author_gilbert_mail_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto: jibo95@gmail.com"));
                try {
                    startActivity(intent);
                } catch (Exception e){
                    Toast.makeText(AboutActivity.this, getString(org.horaapps.leafpic.R.string.send_mail_error), Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*** SPECIAL THANKS ***/
        /*** Patryk Goworowski ***/
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_patryk_goworowski_item_sub)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_patryk_goworowski_item_sub)).setLinkTextColor(getAccentColor());

        //License
        findViewById(org.horaapps.leafpic.R.id.ll_about_license).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cts.launchUrl("https://github.com/HoraApps/LeafPic/blob/master/LICENSE");}
        });

        //Libs
        findViewById(org.horaapps.leafpic.R.id.ll_about_libs).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {licenseDialog();}
        });

    }

    private void setThemeOnChangeListener(){

        /** BackGround **/
        findViewById(org.horaapps.leafpic.R.id.about_background).setBackgroundColor(getBackgroundColor());

        /** Cards **/
        int color = getCardBackgroundColor();
        ((CardView) findViewById(org.horaapps.leafpic.R.id.about_app_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.about_special_thanks_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.about_support_card)).setCardBackgroundColor(color);
        ((CardView) findViewById(org.horaapps.leafpic.R.id.about_license_card)).setCardBackgroundColor(color);
        //cvSpecialThanks.setBackgroundColor(color);

        /** Icons **/
        //ABOUT APP
        color = getIconColor();
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_libs_icon)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_license_icon)).setColor(color);

        //ABOUT SUPPORT
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_support_translate_icon)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_support_rate_icon)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_support_github_icon)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_support_report_bug_icon)).setColor(color);
        ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.about_support_donate_icon)).setColor(color);

        /** TextViews **/
        color = getTextColor();
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_libs_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_app_light_description)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_support_rate_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_support_translate_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_support_github_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_license_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_support_report_bug_item)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.about_support_donate_item)).setTextColor(color);

        /** Sub Text Views**/
        color = getSubTextColor();
        ((TextView) findViewById(R.id.about_version_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_version_item_sub)).setText(BuildConfig.VERSION_NAME);
        ((TextView) findViewById(R.id.about_libs_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_patryk_goworowski_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_community_members_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_community_you_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_support_rate_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_support_translate_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_support_github_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_license_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_support_report_bug_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.about_support_donate_item_sub)).setTextColor(color);
        ((TextView) findViewById(R.id.donald_shtjefni_role)).setTextColor(color);
        ((TextView) findViewById(R.id.gilbert_ndresaj_role)).setTextColor(color);
    }

    private void licenseDialog() {
        // TODO: 10/07/16 ~Jibe rifai sta roba please!
        final Notices notices = new Notices();
        notices.addNotice(new Notice("Glide", "http://github.com/bumptech/glide", "Copyright 2014 Google, Inc. All rights reserved.", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Ion", "http://github.com/koush/ion", "Copyright 2013 Koushik Dutta (2013)", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Android-Iconics", "http://github.com/mikepenz/Android-Iconics", "Copyright 2016 Mike Penz", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("AppIntro", "http://github.com/PaoloRotolo/AppIntro", "Copyright 2015 Paolo Rotolo\n"
                + "Copyright 2016 Maximilian Narr", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("uCrop", "http://github.com/Yalantis/uCrop", "Copyright 2016, Yalantis", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("ShiftColorPicker", "http://github.com/DASAR/ShiftColorPicker", "Copyright (c) 2015 Bogdasarov Bogdan", new MITLicense()));
        notices.addNotice(new Notice("PhotoView", "http://github.com/chrisbanes/PhotoView", "Copyright 2011, 2012 Chris Banes.", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("CircleImageView", "https://github.com/hdodenhof/CircleImageView", "Copyright 2014 - 2015 Henning Dodenhof", new ApacheSoftwareLicense20()));

        new LicensesDialog.Builder(this)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .setThemeResourceId(getDialogStyle())
                .build()
                .show();
    }
}