package com.leafpic.app.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;

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

        Context cntx = holder.picture.getContext();

        a.setPath();

        Glide.with(cntx)
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        String textColor = SP.getBoolean("set_dark_theme", false) ? "#FAFAFA" : "#2b2b2b";
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & SP.getInt("accent_color", ContextCompat.getColor(cntx, R.color.accent_green))));

        holder.name.setText(Html.fromHtml("<i><font color='" + textColor + "'>" + a.DisplayName + "</font></i>"));
        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                "color='" + textColor + "'> " + (a.getImagesCount() == 1 ? cntx.getString(R.string.Singular_Photo) : cntx.getString(R.string.Plural_Photos)) + "</font>"));
        holder.name.setTag(a.Path);

        if (SP.getBoolean("set_dark_theme", false))
            holder.card_layout.setBackgroundColor(ContextCompat.getColor(cntx, R.color.unselected_album));
        else holder.card_layout.setBackgroundColor(ContextCompat.getColor(cntx, R.color.background_material_light));
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
        TextView name, nPhotos;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
            card_layout = (LinearLayout) itemView.findViewById(R.id.s_linear_card_Text);
        }
    }
}



