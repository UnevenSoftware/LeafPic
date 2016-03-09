package com.leafpic.app.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    ArrayList<Album> albums;
    SharedPreferences SP;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public AlbumsAdapter(ArrayList<Album> ph, Context ctx) {
        albums = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_card, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(
                MaterialRippleLayout.on(v)
                        .rippleOverlay(true)
                        .rippleAlpha(0.2f)
                        .rippleColor(0xFF585858)
                        .rippleHover(true)
                        .rippleDuration(1)
                        .create()
        );
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        a.setPath();

        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                //.signature(new MediaStoreSignature())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        /*ImageSize targetSize = new ImageSize(80, 50); // result Bitmap will be fit to this size
        ImageLoader.getInstance().loadImage(a.getPathCoverAlbum(), targetSize, ImageLoaderUtils.fullSizeOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.picture.setImageBitmap(loadedImage);
            }
        });*/
        /*ImageSize targetSize = new ImageSize(200, 300); // result Bitmap will be fit to this size
        ImageLoader.getInstance().loadImage(a.getPathCoverAlbum(), targetSize, ImageLoaderUtils.cropedOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.picture.setImageBitmap(loadedImage);
            }
        });*/
        //ImageLoader.getInstance().displayImage(, (ImageAware) targetSize,ImageLoaderUtils.fullSizeOptions);

        holder.name.setTag(a.Path);

        String hexPrimaryColor = String.format("#%06X", (0xFFFFFF & SP.getInt("primary_color", ContextCompat.getColor(holder.card_layout.getContext(), R.color.md_teal_500))));
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & SP.getInt("accent_color", ContextCompat.getColor(holder.card_layout.getContext(), R.color.md_orange_500))));

        if (hexAccentColor.toString().equals(hexPrimaryColor)) {
            float[] hsv = new float[3];
            int color = SP.getInt("accent_color", ContextCompat.getColor(holder.card_layout.getContext(), R.color.md_orange_500));
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.72f; // value component
            color = Color.HSVToColor(hsv);
            hexAccentColor= String.format("#%06X", (0xFFFFFF & color));
        }

        String textColor = SP.getBoolean("set_dark_theme", false) ? "#FAFAFA" : "#2b2b2b";

        if (a.isSelected()) {
            holder.card_layout.setBackgroundColor(Color.parseColor(hexPrimaryColor));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectHolder.setVisibility(View.VISIBLE);
            if (!SP.getBoolean("set_dark_theme", false)) textColor ="#FAFAFA";
        } else {
            holder.picture.clearColorFilter();
            holder.selectHolder.setVisibility(View.INVISIBLE);

            if (SP.getBoolean("set_dark_theme", true))
                holder.card_layout.setBackgroundColor(ContextCompat.getColor(holder.card_layout.getContext(),R.color.unselected_album));
            else holder.card_layout.setBackgroundColor(ContextCompat.getColor(holder.card_layout.getContext(), R.color.background_material_light));
        }
        holder.name.setText(Html.fromHtml("<i><font color='" + textColor + "'>" + a.DisplayName + "</font></i>"));
        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                "color='" + textColor + "'> " + (a.getImagesCount() == 1 ? "Photo" : "Photos") + "</font>"));
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card_layout;
        ImageView picture;
        ImageView selectHolder;
        TextView name;
        TextView nPhotos;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            card_layout = (LinearLayout) itemView.findViewById(R.id.linear_card_text);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
        }
    }


}



