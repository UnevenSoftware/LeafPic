package org.horaapps.leafpic.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.drew.lang.GeoLocation;
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.adapters.ProgressAdapter;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.metadata.MediaDetailsMap;
import org.horaapps.leafpic.data.metadata.MetadataHelper;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Locale;

import in.uncod.android.bypass.Bypass;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by dnld on 19/05/16.
 */
public class AlertDialogsHelper {

    public static  AlertDialog getInsertTextDialog(ThemedActivity activity, EditText editText, @StringRes int title) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_insert_text, null);
        TextView textViewTitle = dialogLayout.findViewById(R.id.rename_title);

        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.dialog_chose_provider_title)).setCardBackgroundColor(activity.getCardBackgroundColor());
        textViewTitle.setBackgroundColor(activity.getPrimaryColor());
        textViewTitle.setText(title);
        ThemeHelper.setCursorColor(editText, activity.getTextColor());

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        editText.setSingleLine(true);
        editText.getBackground().mutate().setColorFilter(activity.getTextColor(), PorterDuff.Mode.SRC_IN);
        editText.setTextColor(activity.getTextColor());

        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editText, null);
        } catch (Exception ignored) { }

        ((RelativeLayout) dialogLayout.findViewById(org.horaapps.leafpic.R.id.container_edit_text)).addView(editText);

        dialogBuilder.setView(dialogLayout);
        return dialogBuilder.create();
    }

    public static AlertDialog getTextDialog(ThemedActivity activity, @StringRes int title, @StringRes int Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity,activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView dialogTitle = dialogLayout.findViewById(R.id.text_dialog_title);
        TextView dialogMessage = dialogLayout.findViewById(R.id.text_dialog_message);

        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogMessage.setTextColor(activity.getTextColor());
        builder.setView(dialogLayout);
        return builder.create();
    }

    public static AlertDialog getProgressDialogWithErrors(ThemedActivity activity, @StringRes int title, ProgressAdapter adapter, int max) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_list_progress, null);
        final int[] progress = {0};
        TextView dialogTitle = dialogLayout.findViewById(R.id.text_dialog_title);
        TextView progressMessage = dialogLayout.findViewById(R.id.name_folder);
        ((ProgressBar) dialogLayout.findViewById(org.horaapps.leafpic.R.id.progress_dialog_loading)).getIndeterminateDrawable()
                .setColorFilter(activity.getPrimaryColor(), android.graphics.PorterDuff.Mode.SRC_ATOP);

        adapter.setListener(item -> {
            progress[0]++;
            dialogTitle.setText(activity.getString(title, progress[0], max));
            progressMessage.setText(item.getName());
        });

        RecyclerView rv = dialogLayout.findViewById(R.id.rv_progress);
        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setHasFixedSize(true);
        rv.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));
        rv.setAdapter(adapter);


        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        dialogTitle.setText(activity.getString(title, progress[0], max));
        builder.setView(dialogLayout);
        return builder.create();
    }

    public static AlertDialog getProgressDialog(final ThemedActivity activity,  String title, String message){
        AlertDialog.Builder progressDialog = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_progress, null);
        TextView dialogTitle = dialogLayout.findViewById(R.id.progress_dialog_title);
        TextView dialogMessage = dialogLayout.findViewById(R.id.progress_dialog_text);

        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.progress_dialog_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        ((ProgressBar) dialogLayout.findViewById(org.horaapps.leafpic.R.id.progress_dialog_loading)).getIndeterminateDrawable().setColorFilter(activity.getPrimaryColor(), android.graphics
                .PorterDuff.Mode.SRC_ATOP);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogMessage.setTextColor(activity.getTextColor());

        progressDialog.setCancelable(false);
        progressDialog.setView(dialogLayout);
        return progressDialog.create();
    }

    public static AlertDialog getDetailsDialog(final ThemedActivity activity, final Media f) {
        AlertDialog.Builder detailsDialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        MetadataHelper mdhelper = new MetadataHelper();
        MediaDetailsMap<String, String> mainDetails = mdhelper.getMainDetails(activity, f);
        final View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_media_detail, null);
        ImageView imgMap = dialogLayout.findViewById(R.id.photo_map);
        dialogLayout.findViewById(org.horaapps.leafpic.R.id.details_title).setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());

        final GeoLocation location;
        if ((location = f.getGeoLocation()) != null) {

            StaticMapProvider staticMapProvider = StaticMapProvider.fromValue(
                    Hawk.get(activity.getString(R.string.preference_map_provider), StaticMapProvider.GOOGLE_MAPS.getValue()));

            Glide.with(activity.getApplicationContext())
                    .load(staticMapProvider.getUrl(location))
                    .into(imgMap);

            imgMap.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(String.format(Locale.ENGLISH, "geo:%f,%f?z=%d", location.getLatitude(), location.getLongitude(), 17))));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, R.string.no_app_to_perform, Toast.LENGTH_SHORT).show();
                    }

                }
            });

            imgMap.setVisibility(View.VISIBLE);
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.details_title).setVisibility(View.GONE);

        } else imgMap.setVisibility(View.GONE);

        final TextView showMoreText = dialogLayout.findViewById(R.id.details_showmore);
        showMoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDetails(dialogLayout, activity, f);
                showMoreText.setVisibility(View.GONE);
            }
        });

        detailsDialogBuilder.setView(dialogLayout);
        loadDetails(dialogLayout,activity, mainDetails);
        return detailsDialogBuilder.create();
    }

    private static void loadDetails(View dialogLayout, ThemedActivity activity, MediaDetailsMap<String, String> metadata) {
        LinearLayout detailsTable = dialogLayout.findViewById(R.id.ll_list_details);

        int tenPxInDp = Measure.pxToDp (10, activity);
        int hundredPxInDp = Measure.pxToDp (125, activity);//more or less an hundred. Did not used weight for a strange bug

        for (int index : metadata.getKeySet()) {
            LinearLayout row = new LinearLayout(activity.getApplicationContext());
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(activity.getApplicationContext());
            TextView value = new TextView(activity.getApplicationContext());
            label.setText(metadata.getLabel(index));
            label.setLayoutParams((new LinearLayout.LayoutParams(hundredPxInDp, LinearLayout.LayoutParams.WRAP_CONTENT)));
            value.setText(metadata.getValue(index));
            value.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)));
            label.setTextColor(activity.getTextColor());
            label.setTypeface(null, Typeface.BOLD);
            label.setGravity(Gravity.END);
            label.setTextSize(16);
            value.setTextColor(activity.getTextColor());
            value.setTextSize(16);
            value.setPaddingRelative(tenPxInDp, 0, tenPxInDp, 0);
            row.addView(label);
            row.addView(value);
            detailsTable.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private static void showMoreDetails(View dialogLayout, ThemedActivity activity, Media media) {
        MediaDetailsMap<String, String> metadata = new MediaDetailsMap<>();//media.getAllDetails();
        loadDetails(dialogLayout ,activity , metadata);
    }

    public static AlertDialog showChangelogDialog(final ThemedActivity activity) {
        final AlertDialog.Builder changelogDialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_changelog, null);

        TextView dialogTitle = dialogLayout.findViewById(R.id.dialog_changelog_title);
        TextView dialogMessage = dialogLayout.findViewById(R.id.dialog_changelog_text);
        CardView cvBackground = dialogLayout.findViewById(R.id.dialog_changelog_card);
        ScrollView scrChangelog = dialogLayout.findViewById(R.id.changelog_scrollview);

        cvBackground.setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        activity.getThemeHelper().setScrollViewColor(scrChangelog);

        dialogTitle.setText(activity.getString(R.string.changelog));

        Bypass bypass = new Bypass(activity);

        String markdownString;
        try {
            markdownString = getChangeLogFromAssets(activity);
        } catch (IOException e) {
            CustomTabService.openUrl(activity, "https://github.com/HoraApps/LeafPic/blob/dev/CHANGELOG.md");
            return null;
        }
        CharSequence string = bypass.markdownToSpannable(markdownString);
        dialogMessage.setText(string);
        dialogMessage.setMovementMethod(LinkMovementMethod.getInstance());
        dialogMessage.setTextColor(activity.getTextColor());

        changelogDialogBuilder.setView(dialogLayout);
        changelogDialogBuilder.setPositiveButton(activity.getString(R.string.ok_action).toUpperCase(), null);
        changelogDialogBuilder.setNeutralButton(activity.getString(R.string.show_full).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CustomTabService.openUrl(activity, "https://github.com/HoraApps/LeafPic/blob/dev/CHANGELOG.md");
            }
        });
        return changelogDialogBuilder.show();
    }

    private static String getChangeLogFromAssets(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("latest_changelog.md");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int i;
        while ((i = inputStream.read()) != -1)
            outputStream.write(i);

        inputStream.close();
        return outputStream.toString();
    }
}
