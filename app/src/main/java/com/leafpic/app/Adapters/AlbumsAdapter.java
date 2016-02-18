package com.leafpic.app.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    ArrayList<Album> albums;
    private int layout_ID;

    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public AlbumsAdapter(ArrayList<Album> ph, int id) {
        albums = ph;
        layout_ID = id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout_ID, parent, false);
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumsAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        a.setPath();

        Glide.clear(holder.picture);
        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        SharedPreferences SP;
        SP = PreferenceManager.getDefaultSharedPreferences(holder.picture.getContext());
        String titcolor;

        boolean darkTheme = SP.getBoolean("set_dark_theme", false);
        if (darkTheme==true){
            titcolor="#FAFAFA";
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.background_material_dark));
        }else {
            titcolor="4#24242";
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.background_material_light));
        }

        holder.name.setText(Html.fromHtml("<i><font color='" + titcolor + "'>" + a.DisplayName + "</font></i>"));


        String SColor = SP.getString("PrefColor", "#03A9F4");

        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + SColor + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                "color='#FFFFFF'> Photos</font>"));//FFFFFF
        holder.name.setTag(a.Path);

        if (a.isSelected()) {
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.selected_album));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectHolder.setVisibility(View.VISIBLE);

        } else {
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.unselected_album));
            holder.picture.clearColorFilter();
            holder.selectHolder.setVisibility(View.INVISIBLE);

        }

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
        RelativeLayout card_layout;
        ImageView picture;
        ImageView selectHolder;
        TextView name;
        TextView nPhotos;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            card_layout = (RelativeLayout) itemView.findViewById(R.id.album_card);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
        }
    }


}



