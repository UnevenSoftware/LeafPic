package com.horaapps.leafpic.Adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.horaapps.leafpic.Data.Media;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.ThemeHelper;
import com.koushikdutta.ion.Ion;
import com.horaapps.leafpic.R;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;


/**
 * Created by dnld on 1/7/16.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private ArrayList<Media> medias;

    private BitmapDrawable drawable;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public MediaAdapter(ArrayList<Media> ph , Context context) {
        medias = ph;
        updatePlaceholder(context);
    }

    public void updatePlaceholder(Context context) {
        drawable = (BitmapDrawable) ThemeHelper.getPlaceHolder(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final MediaAdapter.ViewHolder holder, int position) {

        Media f = medias.get(position);

        holder.icon.setVisibility(View.GONE);

        if (f.isGif()) {
            Ion.with(holder.imageView.getContext())
                    .load(f.getPath())
                    .intoImageView(holder.imageView);
            holder.gifIcon.setVisibility(View.VISIBLE);
        } else {
            Glide.with(holder.imageView.getContext())
                    .load(f.getUri())
                    .asBitmap()
                    .signature(new StringSignature(f.getPath() + "-" + f.getDateModified()))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .placeholder(drawable)
                    .animate(R.anim.fade_in)
                    .into(holder.imageView);
            holder.gifIcon.setVisibility(View.GONE);
        }

        if(f.isVideo()) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(f.getName());
            holder.path.setTextColor(ContextCompat.getColor(holder.path.getContext(), R.color.md_dark_primary_text));
            holder.path.setBackgroundColor(
                    ColorPalette.getTransparentColor(
                            ContextCompat.getColor(holder.path.getContext(), R.color.md_black_1000), 100));
            holder.icon.setIcon(GoogleMaterial.Icon.gmd_play_circle_filled);
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        holder.path.setTag(position);

        if (f.isSelected()) {
            holder.icon.setIcon(GoogleMaterial.Icon.gmd_done);
            holder.icon.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.layout.setPadding(15,15,15,15);
        } else {
            holder.imageView.clearColorFilter();
            holder.layout.setPadding(0,0,0,0);
        }
    }

    @Override
    public int getItemCount() {
        return medias.size();
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void swapDataSet(ArrayList<Media> asd) {
        medias = asd;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; View gifIcon, layout;
        TextView path; IconicsImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.media_card_layout);
            imageView = (ImageView) itemView.findViewById(R.id.photo_preview);
            gifIcon = itemView.findViewById(R.id.gif_icon);
            icon = (IconicsImageView) itemView.findViewById(R.id.icon);
            path = (TextView) itemView.findViewById(R.id.photo_path);
        }
    }
}



