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
import com.bumptech.glide.signature.MediaStoreSignature;
import com.horaapps.leafpic.Base.Media;
import com.koushikdutta.ion.Ion;
import com.horaapps.leafpic.R;

import java.util.ArrayList;

//import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;

/**
 * Created by dnld on 1/7/16.
 */

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {//implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate

    ArrayList<Media> medias;
    SharedPreferences SP;

    BitmapDrawable drawable;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public PhotosAdapter(ArrayList<Media> ph , Context context) {
        medias = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        switch (SP.getInt("basic_theme", 1)){
            case 2: drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty));break;
            case 3: drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty_amoled));break;
            case 1:
            default: drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty_white));break;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card, parent, false);

        //int width=v.getLayoutParams().width;
        //v.setLayoutParams(new FrameLayout.LayoutParams(v.getWidth(), v.getWidth()));//width , width

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
        byte[] thumnail = f.getThumnail();

        if (thumnail != null) {
            Glide.with(holder.imageView.getContext())
                    .load(thumnail)
                    .centerCrop()
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
                        .signature(new MediaStoreSignature(f.getMIME(), f.getDateModified(), f.getOrientation()))
                        .centerCrop()
                        .placeholder(drawable)
                        //.placeholder(SP.getBoolean("set_dark_theme", true) ? R.drawable.ic_empty : R.drawable.ic_empty_white)
                        .animate(R.anim.fade_in)
                        .into(holder.imageView);
                holder.gifIcon.setVisibility(View.GONE);
                holder.videoIcon.setVisibility(f.isVideo() ? View.VISIBLE : View.GONE);
            }
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

    public void updateDataset(ArrayList<Media> asd) {
        medias = asd;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, selectHolder, gifIcon, videoIcon;
        TextView path;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.photo_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            gifIcon = (ImageView) itemView.findViewById(R.id.type_icon);
            videoIcon = (ImageView) itemView.findViewById(R.id.video_indicator);
            path = (TextView) itemView.findViewById(R.id.photo_path);
        }
    }
}



