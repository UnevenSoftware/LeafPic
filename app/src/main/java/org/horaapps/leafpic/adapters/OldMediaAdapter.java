package org.horaapps.leafpic.adapters;

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
import com.koushikdutta.ion.Ion;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.ThemeHelper;

import java.util.ArrayList;


/**
 * Created by dnld on 1/7/16.
 */

public class OldMediaAdapter extends RecyclerView.Adapter<OldMediaAdapter.ViewHolder> {

    private ArrayList<Media> medias;

    private BitmapDrawable drawable;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public OldMediaAdapter(ArrayList<Media> ph , Context context) {
        medias = ph;
        updatePlaceholder(context);
    }

    public void updatePlaceholder(Context context) {
        drawable = (BitmapDrawable) ThemeHelper.getPlaceHolder(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(org.horaapps.leafpic.R.layout.card_photo, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final OldMediaAdapter.ViewHolder holder, int position) {

        Media f = medias.get(position);

        holder.path.setTag(f);

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
                    .signature(f.getSignature())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    //.placeholder(drawable)
                    .animate(R.anim.fade_in)//TODO:DONT WORK WELL
                    .into(holder.imageView);
            holder.gifIcon.setVisibility(View.GONE);
        }

        if(f.isVideo()) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(f.getName());
            holder.path.setTextColor(ContextCompat.getColor(holder.path.getContext(), org.horaapps.leafpic.R.color.md_dark_primary_text));
            holder.path.setBackgroundColor(
                    ColorPalette.getTransparentColor(
                            ContextCompat.getColor(holder.path.getContext(), org.horaapps.leafpic.R.color.md_black_1000), 100));
            holder.icon.setIcon(CommunityMaterial.Icon.cmd_play_circle);
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
            holder.path.animate().alpha(1).setDuration(250);

        } else {
            holder.icon.setVisibility(View.GONE);
            holder.path.setVisibility(View.GONE);

            holder.icon.animate().alpha(0).setDuration(250);
            holder.path.animate().alpha(0).setDuration(250);
        }

        if (f.isSelected()) {
            holder.icon.setIcon(CommunityMaterial.Icon.cmd_check);
            holder.icon.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.layout.setPadding(15,15,15,15);
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
            //holder.layout.setBackgroundColor(ThemeHelper.getPrimaryColor(holder.path.getContext()));
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
        medias.clear();
        medias.addAll(asd);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; View gifIcon, layout;
        TextView path; IconicsImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(org.horaapps.leafpic.R.id.media_card_layout);
            imageView = (ImageView) itemView.findViewById(org.horaapps.leafpic.R.id.photo_preview);
            gifIcon = itemView.findViewById(org.horaapps.leafpic.R.id.gif_icon);
            icon = (IconicsImageView) itemView.findViewById(org.horaapps.leafpic.R.id.icon);
            path = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.photo_path);
        }
    }
}



