package com.horaapps.leafpic.utils;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.R;
import com.horaapps.leafpic.Views.ThemedActivity;

import java.io.IOException;
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

        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.insert_text_dialog, null);
        TextView textViewTitle = (TextView) dialogLayout.findViewById(R.id.rename_title);

        ((CardView) dialogLayout.findViewById(R.id.rename_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
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
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.text_dialog, null);

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
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.progress_dialog, null);
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

        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.detail_dialog, null);

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
        textViewResolution.setText(f.getResolution());
        textViewData.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(f.getDateModified())));
        textViewType.setText(f.getMIME());
        textViewPath.setText(f.getPath());

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

        try {
            ExifInterface exif = new ExifInterface(f.getPath());
            if (exif.getAttribute(ExifInterface.TAG_MAKE) != null) {
                textViewDevice.setText(String.format("%s %s",
                        exif.getAttribute(ExifInterface.TAG_MAKE),
                        exif.getAttribute(ExifInterface.TAG_MODEL)));

                textViewEXIF.setText(String.format("f/%s ISO-%s %ss",
                        exif.getAttribute(ExifInterface.TAG_APERTURE),
                        exif.getAttribute(ExifInterface.TAG_ISO),
                        exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)));


                textViewOrientation.setText(String.format(Locale.getDefault(), "%d", f.getOrientation()));

                final float[] output= new float[2];
                if(exif.getLatLong(output)) {
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + output[0] + "," + output[1] + "&zoom=15&size="+400+"x"+400+"&scale=2&sensor=false&&markers=color:red%7Clabel:C%7C"+output[0]+","+output[1];
                    url = String.format(Locale.getDefault(),"http://staticmap.openstreetmap.de/staticmap.php" +
                            "?center=%f,%f&zoom=15&size=700x700&maptype=osmarenderer", output[0], output[1]);
                    Glide.with(activity.getApplicationContext())
                            .load(url)
                            .asBitmap()
                            .centerCrop()
                            .animate(R.anim.fade_in)
                            .into(imgMap);
                    imgMap.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=zoom", output[0], output[1]);
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                        }
                    });

                    textViewLocation.setText(String.format(Locale.getDefault(), "%f, %f", output[0], output[1]));
                    dialogLayout.findViewById(R.id.ll_location).setVisibility(View.VISIBLE);
                    dialogLayout.findViewById(R.id.ll_map).setVisibility(View.VISIBLE);

                }
                dialogLayout.findViewById(R.id.ll_exif).setVisibility(View.VISIBLE);
            }
            long dateTake;
            if (((dateTake = f.getDateEXIF()) != -1) && dateTake != f.getDateModified()) {
                textViewDateTaken.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(dateTake)));
                dialogLayout.findViewById(R.id.ll_date_taken).setVisibility(View.VISIBLE);
            }
        }
        catch (IOException e){ e.printStackTrace(); }

        ((CardView) dialogLayout.findViewById(R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        detailsDialogBuilder.setView(dialogLayout);

        return detailsDialogBuilder.create();
    }


}
