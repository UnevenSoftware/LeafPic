package org.horaapps.leafpic.util;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.drew.lang.GeoLocation;
import org.horaapps.leafpic.Data.Media;
import org.horaapps.leafpic.Activities.SettingsActivity;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.Views.ThemedActivity;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

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

        final View dialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_media_detail, null);

        TextView textViewSize = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_size);
        TextView textViewType = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_type);
        TextView textViewResolution = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_resolution);
        TextView textViewData = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_date);
        TextView textViewDateTaken = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.date_taken);
        TextView textViewPath = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_path);
        TextView textViewDevice = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_device);
        TextView textViewEXIF = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_exif);
        TextView textViewOrientation = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.orientation_exif);
        TextView textViewLocation = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_location);

        ImageView imgMap = (ImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_map);

        dialogLayout.findViewById(org.horaapps.leafpic.R.id.details_title).setBackgroundColor(activity.getPrimaryColor());

        textViewSize.setText(f.getHumanReadableSize());
        textViewData.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(f.getDateModified())));
        textViewType.setText(f.getMIME());
        textViewPath.setText(f.getDisplayName());

        /** details labels **/
        int color = activity.getTextColor();

        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_date)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_path)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_resolution)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_type)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_size)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_device)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_exif)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_location)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_date_taken)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.label_orientation)).setTextColor(color);

        /** details info **/
        color = activity.getTextColor();

        textViewData.setTextColor(color);
        textViewDateTaken.setTextColor(color);
        textViewPath.setTextColor(color);
        textViewResolution.setTextColor(color);
        textViewType.setTextColor(color);
        textViewSize.setTextColor(color);
        textViewDevice.setTextColor(color);
        textViewEXIF.setTextColor(color);
        textViewLocation.setTextColor(color);
        textViewOrientation.setTextColor(color);

        String asd = f.getResolution();
        if (asd != null) {
            textViewResolution.setText(asd);
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.ll_resolution).setVisibility(View.VISIBLE);
        }

        if ((asd = f.getCameraInfo()) != null) {
            textViewDevice.setText(asd);
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.ll_device).setVisibility(View.VISIBLE);
        }

        if ((asd = f.getExifInfo()) != null) {
            textViewEXIF.setText(asd);
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.ll_camera_details).setVisibility(View.VISIBLE);
        }


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

            textViewLocation.setText(location.toDMSString());
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.ll_location).setVisibility(View.VISIBLE);
            imgMap.setVisibility(View.VISIBLE);
        }
        int orientation;
        if ((orientation = f.getOrientation()) != -1) {
            dialogLayout.findViewById(org.horaapps.leafpic.R.id.ll_orientation_details).setVisibility(View.VISIBLE);
            textViewOrientation.setText(String.format(Locale.getDefault(), "%d", orientation));
        }
        // TODO: 06/08/16 fix date
        long dateTake = f.getDateTaken();
        if (dateTake != -1) {
            Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
            c.setTimeInMillis(dateTake);
            c2.setTimeInMillis(f.getDateModified());
            if (!(c.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) && !(c.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR))) {
                textViewDateTaken.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(dateTake)));
                dialogLayout.findViewById(org.horaapps.leafpic.R.id.ll_date_taken).setVisibility(View.VISIBLE);
            }
        }

        final TextView showMoreText = (TextView) dialogLayout.findViewById(R.id.details_showmore);
        showMoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDetails(dialogLayout, activity, f);
                showMoreText.setVisibility(View.INVISIBLE);
            }
        });

        ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        detailsDialogBuilder.setView(dialogLayout);
        return detailsDialogBuilder.create();
    }

    static void showMoreDetails(View dialogLayout, ThemedActivity activity, Media media) {
        Map<String, Object> metadata = media.getAllDetails();
        TableLayout detailsTable = (TableLayout) dialogLayout.findViewById(R.id.ll_detail_dialog);
        float scale = activity.getResources().getDisplayMetrics().density;
        int tenPxInDp = (int) (10 * scale + 0.5f);

        for (String metadataKey : metadata.keySet()) {
            TableRow row = new TableRow(activity.getApplicationContext());

            TextView metaDataKey = new TextView(activity.getApplicationContext());
            TextView metaDataValue = new TextView(activity.getApplicationContext());
            metaDataKey.setText(metadataKey);
            metaDataKey.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            metaDataValue.setText(metadata.get(metadataKey).toString());
            metaDataValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            metaDataKey.setTextColor(activity.getTextColor());
            metaDataKey.setTypeface(null, Typeface.BOLD);
            metaDataKey.setGravity(Gravity.END);
            metaDataValue.setTextColor(activity.getTextColor());
            metaDataValue.setTextSize(16);
            metaDataValue.setPaddingRelative(tenPxInDp, 0, 0, 0);
            row.addView(metaDataKey);
            row.addView(metaDataValue);
            detailsTable.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }

    }
}
