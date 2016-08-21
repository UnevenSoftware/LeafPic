package org.horaapps.leafpic.util;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.drew.lang.GeoLocation;

import org.horaapps.leafpic.Activities.SettingsActivity;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.Views.ThemedActivity;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.base.MediaDetailsMap;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.horaapps.leafpic.SecretConstants.MAP_BOX_TOKEN;

/**
 * Created by dnld on 19/05/16.
 */
public class AlertDialogsHelper {

    public static  AlertDialog getInsertTextDialog(final ThemedActivity activity, AlertDialog.Builder dialogBuilder , EditText editText, @StringRes int title) {

        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_insert_text, null);
        TextView textViewTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.rename_title);

        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.dialog_chose_provider_title)).setCardBackgroundColor(activity.getCardBackgroundColor());
        textViewTitle.setBackgroundColor(activity.getPrimaryColor());
        textViewTitle.setText(title);
        ThemeHelper.setCursorDrawableColor(editText, activity.getTextColor());

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

    public static AlertDialog getTextDialog(final ThemedActivity activity, AlertDialog.Builder textDialogBuilder, @StringRes int title, @StringRes int Message){
        View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_text, null);

        TextView dialogTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_message);

        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogMessage.setTextColor(activity.getTextColor());
        textDialogBuilder.setView(dialogLayout);
        return textDialogBuilder.create();
    }

    public static AlertDialog getProgressDialog(final ThemedActivity activity, AlertDialog.Builder progressDialog, String title, String message){
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

    public static AlertDialog getDetailsDialog(final ThemedActivity activity, AlertDialog.Builder detailsDialogBuilder, final Media f) {
        MediaDetailsMap<String, String> mainDetails = f.getMainDetails(activity.getApplicationContext());
        final View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_media_detail, null);
        ImageView imgMap = (ImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_map);
        dialogLayout.findViewById(org.horaapps.leafpic.R.id.details_title).setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());

        final GeoLocation location;
        if ((location = f.getGeoLocation()) != null) {
            PreferenceUtil SP = PreferenceUtil.getInstance(activity.getApplicationContext());

            String url;
            switch (SP.getInt(activity.getString(org.horaapps.leafpic.R.string.preference_map_provider),
                    SettingsActivity.GOOGLE_MAPS_PROVIDER)) {
                case SettingsActivity.GOOGLE_MAPS_PROVIDER:
                default:
                    url = String.format(Locale.getDefault(), "http://maps.google.com/maps/api/staticmap" +
                                                                     "?center=%f,%f&zoom=15&size=500x300&scale=2&sensor=false", location.getLatitude(), location.getLongitude());
                    break;
                case SettingsActivity.OSM_MAP_BOX:
                    url = String.format(Locale.getDefault(), "https://api.mapbox.com/v4/mapbox.streets/%f,%f,15/500x300.jpg?access_token=%s",
                            location.getLongitude(), location.getLatitude(), MAP_BOX_TOKEN);

                    break;
                case SettingsActivity.OSM_MAP_BOX_DARK:
                    url = String.format(Locale.getDefault(), "https://api.mapbox.com/v4/mapbox.dark/%f,%f,15/500x300.jpg?access_token=%s",
                            location.getLongitude(), location.getLatitude(), MAP_BOX_TOKEN);

                    break;
                case SettingsActivity.OSM_MAP_BOX_LIGHT:
                    url = String.format(Locale.getDefault(), "https://api.mapbox.com/v4/mapbox.light/%f,%f,15/500x300.jpg?access_token=%s",
                            location.getLongitude(), location.getLatitude(), MAP_BOX_TOKEN);

                    break;
                case SettingsActivity.OSM_TYLER_PROVIDER:
                    url = String.format(Locale.getDefault(), "https://tyler-demo.herokuapp.com/" +
                                                                     "?greyscale=false&lat=%f&lon=%f&zoom=15&width=500&height=300" +
                                                                     "&tile_url=http://[abcd].tile.stamen.com/watercolor/{zoom}/{x}/{y}.jpg", location.getLatitude(), location.getLongitude());

                    break;
            }

            Glide.with(activity.getApplicationContext())
                    .load(url)
                    .asBitmap()
                    .centerCrop()
                    .animate(org.horaapps.leafpic.R.anim.fade_in)
                    .into(imgMap);

            imgMap.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=%d", location.getLatitude(), location.getLongitude(), 17);
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            });

            imgMap.setVisibility(View.VISIBLE);
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.details_title).setVisibility(View.GONE);

        }

        final TextView showMoreText = (TextView) dialogLayout.findViewById(R.id.details_showmore);
        showMoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDetails(dialogLayout, activity, f);
                showMoreText.setVisibility(View.INVISIBLE);
            }
        });

        detailsDialogBuilder.setView(dialogLayout);
        loadDetails(dialogLayout,activity, mainDetails);
        return detailsDialogBuilder.create();
    }

    static void loadDetails(View dialogLayout, ThemedActivity activity, MediaDetailsMap<String, String> metadata) {
        LinearLayout detailsTable = (LinearLayout) dialogLayout.findViewById(R.id.ll_list_details);
        float scale = activity.getResources().getDisplayMetrics().density;
        int tenPxInDp = (int) (10 * scale + 0.5f);

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
            value.setTextColor(activity.getTextColor());
            value.setTextSize(16);
            value.setPaddingRelative(tenPxInDp, 0, 0, 0);
            row.addView(label);
            row.addView(value);
            detailsTable.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    static void showMoreDetails(View dialogLayout, ThemedActivity activity, Media media) {

        MediaDetailsMap<String, String> metadata = media.getAllDetails();
        loadDetails(dialogLayout ,activity , metadata);
    }
}
