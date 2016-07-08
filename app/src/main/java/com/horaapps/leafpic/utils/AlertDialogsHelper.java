package com.horaapps.leafpic.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.drew.lang.GeoLocation;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.R;
import com.horaapps.leafpic.SettingsActivity;
import com.horaapps.leafpic.Views.ThemedActivity;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dnld on 19/05/16.
 */
public class AlertDialogsHelper {

    public static  AlertDialog getInsertTextDialog(final ThemedActivity activity, AlertDialog
            .Builder dialogBuilder , EditText editText, String title) {

        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_insert_text, null);
        TextView textViewTitle = (TextView) dialogLayout.findViewById(R.id.rename_title);

        ((CardView) dialogLayout.findViewById(R.id.dialog_chose_provider_title)).setCardBackgroundColor(activity.getCardBackgroundColor());
        textViewTitle.setBackgroundColor(activity.getPrimaryColor());
        textViewTitle.setText(title);
        ThemedActivity.setCursorDrawableColor(editText, activity.getTextColor());

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

        ((RelativeLayout) dialogLayout.findViewById(R.id.container_edit_text)).addView(editText);

        dialogBuilder.setView(dialogLayout);
        return dialogBuilder.create();
    }

    public static AlertDialog getTextDialog(final ThemedActivity activity, AlertDialog.Builder textDialogBuilder, String title, String Message){
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.text_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(R.id.text_dialog_message);

        ((CardView) dialogLayout.findViewById(R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogMessage.setTextColor(activity.getTextColor());
        textDialogBuilder.setView(dialogLayout);
        return textDialogBuilder.create();
    }

    public static AlertDialog getProgressDialog(final ThemedActivity activity, AlertDialog.Builder progressDialog, String title, String message){
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_progress, null);
        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.progress_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(R.id.progress_dialog_text);

        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(R.id.progress_dialog_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        ((ProgressBar) dialogLayout.findViewById(R.id.progress_dialog_loading)).getIndeterminateDrawable().setColorFilter(activity.getPrimaryColor(), android.graphics
                .PorterDuff.Mode.SRC_ATOP);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogMessage.setTextColor(activity.getTextColor());

        progressDialog.setCancelable(false);
        progressDialog.setView(dialogLayout);
        return progressDialog.create();
    }

    public static AlertDialog getDetailsDialog(final ThemedActivity activity, AlertDialog.Builder detailsDialogBuilder, Media f) {

        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_media_detail, null);

        TextView textViewSize = (TextView) dialogLayout.findViewById(R.id.photo_size);
        TextView textViewType = (TextView) dialogLayout.findViewById(R.id.photo_type);
        TextView textViewResolution = (TextView) dialogLayout.findViewById(R.id.photo_resolution);
        TextView textViewData = (TextView) dialogLayout.findViewById(R.id.photo_date);
        TextView textViewDateTaken = (TextView) dialogLayout.findViewById(R.id.date_taken);
        TextView textViewPath = (TextView) dialogLayout.findViewById(R.id.photo_path);
        TextView textViewDevice = (TextView) dialogLayout.findViewById(R.id.photo_device);
        TextView textViewEXIF = (TextView) dialogLayout.findViewById(R.id.photo_exif);
        TextView textViewOrientation = (TextView) dialogLayout.findViewById(R.id.orientation_exif);
        TextView textViewLocation = (TextView) dialogLayout.findViewById(R.id.photo_location);

        ImageView imgMap = (ImageView) dialogLayout.findViewById(R.id.photo_map);

        dialogLayout.findViewById(R.id.details_title).setBackgroundColor(activity.getPrimaryColor());

        textViewSize.setText(f.getHumanReadableSize());
        textViewData.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(f.getDateModified())));
        textViewType.setText(f.getMIME());
        textViewPath.setText(f.getDisplayName());

        /** details labels **/
        int color = activity.getTextColor();

        ((TextView) dialogLayout.findViewById(R.id.label_date)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_path)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_resolution)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_type)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_size)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_device)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_exif)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_location)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_date_taken)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.label_orientation)).setTextColor(color);

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
            dialogLayout.findViewById(R.id.ll_resolution).setVisibility(View.VISIBLE);
        }

        if ((asd = f.getCameraInfo()) != null) {
            textViewDevice.setText(asd);
            dialogLayout.findViewById(R.id.ll_device).setVisibility(View.VISIBLE);
        }

        if ((asd = f.getExifInfo()) != null) {
            textViewEXIF.setText(asd);
            dialogLayout.findViewById(R.id.ll_camera_details).setVisibility(View.VISIBLE);
        }


        final GeoLocation location;
        if((location = f.getGeoLocation()) != null) {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            String url;
            switch (SP.getInt(activity.getString(R.string.preference_map_provider),
                    SettingsActivity.GOOGLE_MAPS_PROVIDER)) {
                case SettingsActivity.GOOGLE_MAPS_PROVIDER:
                default:
                    url = String.format(Locale.getDefault(),"http://maps.google.com/maps/api/staticmap" +
                        "?center=%f,%f&zoom=15&size=500x300&scale=2&sensor=false", location.getLatitude(), location.getLongitude());
                    break;
                case SettingsActivity.OSM_DE_PROVIDER:
                    url = String.format(Locale.getDefault(),"http://staticmap.openstreetmap.de/staticmap.php" +
                        "?center=%f,%f&zoom=15&size=500x300&maptype=osmarenderer", location.getLatitude(), location.getLongitude());
                    break;
                case SettingsActivity.OSM_TYLER_PROVIDER:
                    url = String.format(Locale.getDefault(),"https://tyler-demo.herokuapp.com/" +
                        "?greyscale=false&lat=%f&lon=%f&zoom=15&width=500&height=300" +
                        "&tile_url=http://[abcd].tile.stamen.com/watercolor/{zoom}/{x}/{y}.jpg", location.getLatitude(), location.getLongitude());
                    Log.d("url",url);
                    break;
            }

            Glide.with(activity.getApplicationContext())
                    .load(url)
                    .asBitmap()
                    .centerCrop()
                    .animate(R.anim.fade_in)
                    .into(imgMap);
            imgMap.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=%d", location.getLatitude(), location.getLongitude(), 17);
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }
            });

            textViewLocation.setText(location.toDMSString());
            dialogLayout.findViewById(R.id.ll_location).setVisibility(View.VISIBLE);
            imgMap.setVisibility(View.VISIBLE);
            //dialogLayout.findViewById(R.id.ll_map).setVisibility(View.VISIBLE);

        }
        int orientation;
        if ((orientation = f.getOrientation()) != -1) {
            dialogLayout.findViewById(R.id.ll_orientation_details).setVisibility(View.VISIBLE);
            textViewOrientation.setText(String.format(Locale.getDefault(), "%d", orientation));
        }
        long dateTake;
        if (((dateTake = f.getDateTaken()) != -1) && dateTake != f.getDateModified()) {
            textViewDateTaken.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(dateTake)));
            dialogLayout.findViewById(R.id.ll_date_taken).setVisibility(View.VISIBLE);
        }
        ((CardView) dialogLayout.findViewById(R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        detailsDialogBuilder.setView(dialogLayout);

        return detailsDialogBuilder.create();
    }


}
