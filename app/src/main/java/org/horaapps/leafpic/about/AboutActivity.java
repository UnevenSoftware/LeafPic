package org.horaapps.leafpic.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.DonateActivity;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.ApplicationUtils;
import org.horaapps.leafpic.util.ChromeCustomTabs;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ui.ThemedTextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.horaapps.leafpic.util.ServerConstants.GITHUB_CALVIN;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_GILBERT;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_LEAFPIC;
import static org.horaapps.leafpic.util.ServerConstants.GOOGLE_ABOUT_CALVIN;
import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_CROWDIN;
import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_ISSUES;
import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_LICENSE;
import static org.horaapps.leafpic.util.ServerConstants.MAIL_CALVIN;
import static org.horaapps.leafpic.util.ServerConstants.MAIL_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.MAIL_GILBERT;
import static org.horaapps.leafpic.util.ServerConstants.TWITTER_ABOUT_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.TWITTER_ABOUT_GILBERT;

/**
 * The Activity to show About application
 * <p>
 * Includes the following data:
 * - Developers
 * - Translators
 * - Relevant app links
 */
public class AboutActivity extends ThemedActivity implements ContactListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.about_version_item_sub)
    ThemedTextView appVersion;
    @BindView(R.id.aboutAct_scrollView) ScrollView aboutScrollView;
    @BindView(R.id.about_developer_donald)
    AboutCreator aboutDonald;
    @BindView(R.id.about_developer_gilbert)
    AboutCreator aboutGilbert;
    @BindView(R.id.about_patryk_goworowski_item_sub)
    ThemedTextView specialThanksPatryk;
    @BindView(R.id.about_link_changelog) AboutLink linkChangelog;
    @BindView(R.id.list_contributors)
    RecyclerView rvContributors;
    ContributorsAdapter contributorsAdapter;

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
        appVersion.setText(ApplicationUtils.getAppVersion());
        linkChangelog.setDescription(ApplicationUtils.getAppVersion());


        ArrayList<Contributor> contributors = new ArrayList<>(1);

        /* Calvin */
        Contributor calvin = new Contributor(
                getString(R.string.developer_calvin_name),
                getString(R.string.about_developer_calvin_description),
                R.drawable.calvin_profile);
        calvin.setEmail(MAIL_CALVIN);
        calvin.addSocial(getString(R.string.google_plus_link), GOOGLE_ABOUT_CALVIN);
        calvin.addSocial(getString(R.string.github), GITHUB_CALVIN);
        contributors.add(calvin);


        contributorsAdapter = new ContributorsAdapter(getApplicationContext(), contributors, this);
        rvContributors.setHasFixedSize(true);
        rvContributors.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvContributors.setAdapter(contributorsAdapter);


        /* Donald */
        ArrayList<Contact> donaldContacts = new ArrayList<>();
        donaldContacts.add(new Contact(TWITTER_ABOUT_DONALD, getString(R.string.twitter_link)));
        donaldContacts.add(new Contact(GITHUB_DONALD, getString(R.string.github_link)));
        aboutDonald.setupListeners(this, MAIL_DONALD, donaldContacts);

        /* Jibo */
        ArrayList<Contact> jiboContacts = new ArrayList<>();
        jiboContacts.add(new Contact(TWITTER_ABOUT_GILBERT, getString(R.string.twitter_link)));
        jiboContacts.add(new Contact(GITHUB_GILBERT, getString(R.string.github_link)));
        aboutGilbert.setupListeners(this, MAIL_GILBERT, jiboContacts);

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

        specialThanksPatryk.setLinkTextColor(getAccentColor());
    }

    @Override
    public void onContactClicked(Contact contact) {
        chromeTabs.launchUrl(contact.getValue());
    }

    @Override
    public void onMailClicked(String mail) {
        mail(mail);
    }
}
