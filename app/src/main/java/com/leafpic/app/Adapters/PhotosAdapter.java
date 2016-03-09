package com.leafpic.app.Adapters;

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
import com.koushikdutta.ion.Ion;
import com.leafpic.app.Base.Media;
import com.leafpic.app.R;

import java.util.ArrayList;

//import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;

/**
 * Created by dnld on 1/7/16.
 */

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {//implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate

    ArrayList<Media> medias;
    SharedPreferences SP;

    BitmapDrawable drawable;// = ((BitmapDrawable) ContextCompat.getDrawable(holder.path.getContext(), R.drawable.ic_empty));
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public PhotosAdapter(ArrayList<Media> ph ,Context context) {
        medias = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        if(SP.getBoolean("set_dark_theme", true))
            drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty));
        else drawable = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_empty_white));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card, parent, false);
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

    //@Override
    public double aspectRatioForIndex(int index) {
        Media f = medias.get(index);
        if (index > medias.size()) return 1.0;
        return f.width / (double) f.height;
        // Return the aspect ratio of your image at the given index
    }

    @Override
    public void onBindViewHolder(final PhotosAdapter.ViewHolder holder, int position) {

        Media f = medias.get(position);
        Glide.clear(holder.imageView);//fix corruption

        if (f.isGif()) {
            Ion.with(holder.imageView.getContext())
                    .load(f.Path)
                    .intoImageView(holder.imageView);
            holder.gifIcon.setVisibility(View.VISIBLE);
        } else {
            holder.gifIcon.setVisibility(View.INVISIBLE);

            Glide.with(holder.imageView.getContext())
                    .load(f.Path)
                    .asBitmap()
                    .signature(new MediaStoreSignature(f.MIME, Long.parseLong(f.DateModified), 0))
                    .centerCrop()
                    .placeholder(R.drawable.ic_empty)
                    .into(holder.imageView);
        }


        holder.path.setTag(f.Path);

        if (f.isSelected()) {
            holder.selectHolder.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.imageView.setPadding(15,15,15,15);
        } else {
            holder.selectHolder.setVisibility(View.INVISIBLE);
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

    public void removeItemAt(int pos) {
        //Log.wtf("asdasd",getItemCount()+"");
        //medias.remove(pos);
        //notifyItemRemoved(pos);
        //Log.wtf("asdasd",getItemCount()+"");
        // notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, selectHolder, gifIcon;
        TextView path;


        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.photo_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            gifIcon = (ImageView) itemView.findViewById(R.id.type_icon);
            path = (TextView) itemView.findViewById(R.id.photo_path);
        }
    }
}



