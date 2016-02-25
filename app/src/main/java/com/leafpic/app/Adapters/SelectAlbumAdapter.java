package com.leafpic.app.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;
import com.leafpic.app.utils.ImageLoaderUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class SelectAlbumAdapter extends RecyclerView.Adapter<SelectAlbumAdapter.ViewHolder> {

    ArrayList<Album> albums;
    SharedPreferences SP;

    private View.OnClickListener mOnClickListener;

    public SelectAlbumAdapter(ArrayList<Album> ph ,Context ctx) {
        albums = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_album_card, parent, false);
        v.setOnClickListener(mOnClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectAlbumAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        a.setPath();

        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        holder.name.setText(Html.fromHtml("<i><font>" + a.DisplayName + "</font></i>"));
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & SP.getInt("accent_color", Color.rgb(0, 77, 64))));
        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                "color='#FFFFFF'> Photos</font>"));
        holder.name.setTag(a.Path);
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView picture;
        TextView name, nPhotos;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
        }
    }
}



