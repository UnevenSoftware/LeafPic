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
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.drew.lang.GeoLocation;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.activities.theme.ThemeHelper;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.metadata.MediaDetailsMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Locale;

import in.uncod.android.bypass.Bypass;

/**
 * Created by dnld on 19/05/16.
 */
public class AlertDialogsHelper {

    public static  AlertDialog getInsertTextDialog(ThemedActivity activity, EditText editText, @StringRes int title) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_insert_text, null);
        TextView textViewTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.rename_title);

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
        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_text, null);

        TextView dialogTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_message);

        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogMessage.setTextColor(activity.getTextColor());
        builder.setView(dialogLayout);
        return builder.create();
    }

    public static AlertDialog getProgressDialog(final ThemedActivity activity,  String title, String message){
        AlertDialog.Builder progressDialog = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_progress, null);
        TextView dialogTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.progress_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.progress_dialog_text);

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
        MediaDetailsMap<String, String> mainDetails = new MediaDetailsMap<>();//f.getMainDetails(activity.getApplicationContext());
        final View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_media_detail, null);
        ImageView imgMap = (ImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_map);
        dialogLayout.findViewById(org.horaapps.leafpic.R.id.details_title).setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());

        final GeoLocation location;
        if ((location = f.getGeoLocation()) != null) {

            StaticMapProvider staticMapProvider = StaticMapProvider.fromValue(
                    PreferenceUtil.getInt(activity, activity.getString(R.string.preference_map_provider), StaticMapProvider.GOOGLE_MAPS.getValue()));

            Glide.with(activity.getApplicationContext())
                    .load(staticMapProvider.getUrl(location))
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .animate(org.horaapps.leafpic.R.anim.fade_in)
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

        final TextView showMoreText = (TextView) dialogLayout.findViewById(R.id.details_showmore);
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
        LinearLayout detailsTable = (LinearLayout) dialogLayout.findViewById(R.id.ll_list_details);

        int tenPxInDp = Measure.pxToDp (10, activity);

        for (int index : metadata.getKeySet()) {
            LinearLayout row = new LinearLayout(activity.getApplicationContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(10);

            TextView label = new TextView(activity.getApplicationContext());
            TextView value = new TextView(activity.getApplicationContext());
            label.setText(metadata.getLabel(index));
            label.setLayoutParams((new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f)));
            value.setText(metadata.getValue(index));
            value.setLayoutParams((new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7f)));
            label.setTextColor(activity.getTextColor());
            label.setTypeface(null, Typeface.BOLD);
            label.setGravity(Gravity.END);
            label.setTextSize(16);
            value.setTextColor(activity.getTextColor());
            value.setTextSize(16);
            value.setPaddingRelative(tenPxInDp, 0, 0, 0);
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

        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.dialog_changelog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(R.id.dialog_changelog_text);
        CardView cvBackground = (CardView) dialogLayout.findViewById(R.id.dialog_changelog_card);
        ScrollView scrChangelog = (ScrollView) dialogLayout.findViewById(R.id.changelog_scrollview);

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
