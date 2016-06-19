package com.horaapps.leafpic.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
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
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.ColorPalette;
import com.koushikdutta.ion.Ion;
import com.horaapps.leafpic.R;

import java.util.ArrayList;


/**
 * Created by dnld on 1/7/16.
 */

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    private ArrayList<Media> medias;

    private BitmapDrawable drawable;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public PhotosAdapter(ArrayList<Media> ph , Context context) {
        medias = ph;
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        updatePlaceholder(context, SP.getInt("basic_theme", ThemedActivity.LIGHT_THEME));
    }

    public void updatePlaceholder(Context context, int theme) {
        switch (theme){
            case ThemedActivity.DARK_THEME:
                drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty));
                break;
            case ThemedActivity.AMOLED_THEME: drawable = null; break;
            case ThemedActivity.LIGHT_THEME: default:
                drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty_white));
                break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        /*return new ViewHolder(
                MaterialRippleLayout.on(v)
                        .rippleOverlay(true)
                        .rippleAlpha(0.2f)
                        .rippleColor(0xFF585858)
                        .rippleHover(true)
                        .rippleDuration(1)
                        .create();
        );*/

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final PhotosAdapter.ViewHolder holder, int position) {

        Media f = medias.get(position);
        byte[] thumbnail = f.getThumbnail();

        if (thumbnail != null) {
            Glide.with(holder.imageView.getContext())
                    .load(thumbnail)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .placeholder(drawable)
                    .animate(R.anim.fade_in)
                    .into(holder.imageView);
        } else {
            if (f.isGif()) {
                Ion.with(holder.imageView.getContext())
                        .load(f.getPath())
                        .intoImageView(holder.imageView);
                holder.gifIcon.setVisibility(View.VISIBLE);
            } else {
                Glide.with(holder.imageView.getContext())
                        .load(f.getPath())
                        .asBitmap()
                        .signature(new StringSignature(f.getPath() + "-" + f.getDateModified()))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .placeholder(drawable)
                        .animate(R.anim.fade_in)
                        .into(holder.imageView);
                holder.gifIcon.setVisibility(View.GONE);
            }
        }

        if(f.isVideo()) {
            holder.videoIcon.setVisibility(View.VISIBLE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(f.getName());
            holder.path.setBackgroundColor(
                    ColorPalette.getTransparentColor(
                            ContextCompat.getColor(holder.path.getContext(), R.color.md_black_1000), 160));
        } else {
            holder.videoIcon.setVisibility(View.GONE);
            holder.path.setVisibility(View.GONE);

        }
        holder.path.setTag(position);

        if (f.isSelected()) {
            holder.selectHolder.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.imageView.setPadding(15,15,15,15);
        } else {
            holder.selectHolder.setVisibility(View.GONE);
            holder.imageView.clearColorFilter();
            holder.imageView.setPadding(0,0,0,0);
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

    public void updateDataSet(ArrayList<Media> asd) {
        medias = asd;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, selectHolder, gifIcon, videoIcon;
        TextView path;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.photo_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            gifIcon = (ImageView) itemView.findViewById(R.id.type_icon);
            videoIcon = (ImageView) itemView.findViewById(R.id.video_indicator);
            path = (TextView) itemView.findViewById(R.id.photo_path);
        }
    }
}



