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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;

import java.util.ArrayList;

public class PickAlbumAdapter extends RecyclerView.Adapter<PickAlbumAdapter.ViewHolder> {

    ArrayList<Album> albums;
    SharedPreferences SP;

    private View.OnClickListener mOnClickListener;

    public PickAlbumAdapter(ArrayList<Album> ph, Context ctx) {
        albums = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_album_card, parent, false);
        v.setOnClickListener(mOnClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PickAlbumAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);

        a.setPath();

        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        String textColor = SP.getBoolean("set_dark_theme", false) ? "#FAFAFA" : "#2b2b2b";

        holder.name.setText(Html.fromHtml("<i><font color='" + textColor + "'>" + a.DisplayName + "</font></i>"));

        holder.name.setTag(a.Path);
        holder.card_layout.setBackgroundColor(Color.TRANSPARENT);

    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card_layout;
        ImageView picture;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            name = (TextView) itemView.findViewById(R.id.album_name);
            card_layout = (LinearLayout) itemView.findViewById(R.id.s_linear_card_Text);
        }
    }
}



