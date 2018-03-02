package org.horaapps.leafpic.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.ApplicationUtils;
import org.horaapps.leafpic.util.ChromeCustomTabs;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.leafpic.views.AboutDeveloper;
import org.horaapps.leafpic.views.AboutLink;
import org.horaapps.liz.ThemedActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.horaapps.leafpic.util.ServerConstants.GITHUB_CALVIN;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_GILBERT;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_LEAFPIC;
import static org.horaapps.leafpic.util.ServerConstants.GOOGLE_ABOUT_CALVIN;
import static org.horaapps.leafpic.util.ServerConstants.GOOGLE_ABOUT_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.GOOGLE_ABOUT_GILBERT;
import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_CROWDIN;
import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_ISSUES;
import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_LICENSE;
import static org.horaapps.leafpic.util.ServerConstants.MAIL_CALVIN;
import static org.horaapps.leafpic.util.ServerConstants.MAIL_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.MAIL_GILBERT;

/**
 * The Activity to show About application
 * <p>
 * Includes the following data:
 * - Developers
 * - Translators
 * - Relevant app links
 */
public class AboutActivity extends ThemedActivity implements AboutDeveloper.LinkListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.about_version_item_sub) TextView appVersion;
    @BindView(R.id.about_app_title) TextView appTitle;
    @BindView(R.id.about_support_title) TextView appSupportTitle;
    @BindView(R.id.about_app_light_description) TextView aboutAppLightDescription;
    @BindView(R.id.about_special_thanks_title) TextView specialThanksTitle;
    @BindView(R.id.about_community_members_sub) TextView aboutCommunityMembers;
    @BindView(R.id.about_community_you_sub) TextView aboutCommunityYou;
    @BindView(R.id.aboutAct_scrollView) ScrollView aboutScrollView;
    @BindView(R.id.about_background) View aboutBackground;
    @BindView(R.id.about_donald_card) CardView donaldCardView;
    @BindView(R.id.about_calvin_card) CardView calvinCardView;
    @BindView(R.id.about_gilbert_card) CardView gilbertCardView;
    @BindView(R.id.about_app_card) CardView aboutAppCardView;
    @BindView(R.id.about_special_thanks_card) CardView specialThanksCardView;
    @BindView(R.id.about_support_card) CardView aboutSupportCardView;
    @BindView(R.id.about_developer_donald) AboutDeveloper aboutDonald;
    @BindView(R.id.about_developer_calvin) AboutDeveloper aboutCalvin;
    @BindView(R.id.about_developer_gilbert) AboutDeveloper aboutGilbert;
    @BindView(R.id.about_patryk_goworowski_item_sub) TextView specialThanksPatryk;
    @BindView(R.id.about_link_changelog) AboutLink linkChangelog;

    private ChromeCustomTabs chromeTabs;
    private int emojiEasterEggCount = 0;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        chromeTabs = new ChromeCustomTabs(AboutActivity.this);

        initUi();
    }

    @Override
    public void onDestroy() {
        chromeTabs.destroy();
        super.onDestroy();
    }

    @OnClick(R.id.about_link_report_bug)
    public void onReportBug() {
        chromeTabs.launchUrl(LEAFPIC_ISSUES);
    }

    @OnClick(R.id.about_link_translate)
    public void onTranslate() {
        chromeTabs.launchUrl(LEAFPIC_CROWDIN);
    }

    @OnClick(R.id.about_link_rate)
    public void onRate() {
        // TODO: Link to app store
    }

    @OnClick(R.id.about_link_github)
    public void onGitHub() {
        chromeTabs.launchUrl(GITHUB_LEAFPIC);
    }

    @OnClick(R.id.about_link_donate)
    public void onDonate() {
        DonateActivity.startActivity(this);
    }

    @OnClick(R.id.about_link_license)
    public void onLicense() {
        chromeTabs.launchUrl(LEAFPIC_LICENSE);
    }

    @OnClick(R.id.about_link_changelog)
    public void onChangelog() {
        AlertDialog alertDialog = AlertDialogsHelper.showChangelogDialog(this);
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.ok_action).toUpperCase(),
                (dialogInterface, i) -> {
                });
        alertDialog.show();
    }

    //TODO: EMOJI EASTER EGG - NOTHING TO SHOW
    private void emojiEasterEgg() {
        emojiEasterEggCount++;
        if (emojiEasterEggCount > 3) {
            boolean showEasterEgg = Prefs.showEasterEgg();
            Toast.makeText(this,
                    (!showEasterEgg ? this.getString(R.string.easter_egg_enable) : this.getString(R.string.easter_egg_disable))
                            + " " + this.getString(R.string.emoji_easter_egg), Toast.LENGTH_SHORT).show();
            Prefs.setShowEasterEgg(!showEasterEgg);
            emojiEasterEggCount = 0;
        } else
            Toast.makeText(getBaseContext(), String.valueOf(emojiEasterEggCount), Toast.LENGTH_SHORT).show();
    }

    private void mail(String mail) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + mail));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.send_mail_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        aboutDonald.setLinkListener(this);
        aboutGilbert.setLinkListener(this);
        aboutCalvin.setLinkListener(this);
        appVersion.setText(ApplicationUtils.getAppVersion());
        linkChangelog.setDescription(ApplicationUtils.getAppVersion());
        aboutGilbert.setOnClickListener(v -> emojiEasterEgg());
        specialThanksPatryk.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setScrollViewColor(aboutScrollView);
        setStatusBarColor();
        setNavBarColor();

        int accentColor = getAccentColor();
        int cardBackgroundColor = getCardBackgroundColor();
        int backgroundColor = getBackgroundColor();
        int textColor = getTextColor();
        int subTextColor = getSubTextColor();

        specialThanksPatryk.setLinkTextColor(accentColor);
        specialThanksTitle.setTextColor(accentColor);
        appTitle.setTextColor(accentColor);
        appSupportTitle.setTextColor(accentColor);

        appVersion.setTextColor(subTextColor);
        specialThanksPatryk.setTextColor(subTextColor);
        aboutAppLightDescription.setTextColor(textColor);
        aboutCommunityMembers.setTextColor(subTextColor);
        aboutCommunityYou.setTextColor(subTextColor);

        donaldCardView.setCardBackgroundColor(cardBackgroundColor);
        calvinCardView.setCardBackgroundColor(cardBackgroundColor);
        gilbertCardView.setCardBackgroundColor(cardBackgroundColor);
        aboutAppCardView.setCardBackgroundColor(cardBackgroundColor);
        specialThanksCardView.setCardBackgroundColor(cardBackgroundColor);
        aboutSupportCardView.setCardBackgroundColor(cardBackgroundColor);

        aboutBackground.setBackgroundColor(backgroundColor);
    }

    @Override
    public void onMailClicked(int developerId) {
        switch (developerId) {
            case R.id.about_developer_donald:
                mail(MAIL_DONALD);
                break;

            case R.id.about_developer_calvin:
                mail(MAIL_CALVIN);
                break;

            case R.id.about_developer_gilbert:
                mail(MAIL_GILBERT);
                break;
        }
    }

    @Override
    public void onGithubClicked(int developerId) {
        switch (developerId) {
            case R.id.about_developer_donald:
                chromeTabs.launchUrl(GITHUB_DONALD);
                break;

            case R.id.about_developer_calvin:
                chromeTabs.launchUrl(GITHUB_CALVIN);
                break;

            case R.id.about_developer_gilbert:
                chromeTabs.launchUrl(GITHUB_GILBERT);
                break;
        }
    }

    @Override
    public void onGooglePlusClicked(int developerId) {
        switch (developerId) {
            case R.id.about_developer_donald:
                chromeTabs.launchUrl(GOOGLE_ABOUT_DONALD);
                break;

            case R.id.about_developer_calvin:
                chromeTabs.launchUrl(GOOGLE_ABOUT_CALVIN);
                break;

            case R.id.about_developer_gilbert:
                chromeTabs.launchUrl(GOOGLE_ABOUT_GILBERT);
                break;
        }
    }
}
