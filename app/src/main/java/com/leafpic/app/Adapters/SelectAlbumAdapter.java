package com.leafpic.app.Adapters;

import android.content.SharedPreferences;
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

import java.util.ArrayList;

public class SelectAlbumAdapter extends RecyclerView.Adapter<SelectAlbumAdapter.ViewHolder> {

    ArrayList<Album> albums;
    private int layout_ID;

    private View.OnClickListener mOnClickListener;

    public SelectAlbumAdapter(ArrayList<Album> ph, int id) {
        albums = ph;
        layout_ID = id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout_ID, parent, false);
        v.setOnClickListener(mOnClickListener);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectAlbumAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        a.setPath();

        Glide.clear(holder.picture);
        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        holder.name.setText(Html.fromHtml("<i><font>" + a.DisplayName + "</font></i>"));

        SharedPreferences SP;
        SP = PreferenceManager.getDefaultSharedPreferences(holder.picture.getContext());
        String SColor = SP.getString("PrefColor", "#03A9F4");
        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + SColor + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
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



