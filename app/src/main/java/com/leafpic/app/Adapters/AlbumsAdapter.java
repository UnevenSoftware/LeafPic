package com.leafpic.app.Adapters;

import android.content.SharedPreferences;
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
        //holder.picture.setTag(a.getPathCoverAlbum());


        //ImageLoader.getInstance().displayImage(a.getPathCoverAlbum(), holder.picture, defaultOptions);
        Glide.with(holder.picture.getContext())
                .load(a.getPathCoverAlbum())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(holder.picture);

        holder.name.setText(a.DisplayName);

        SharedPreferences SP;
        SP = PreferenceManager.getDefaultSharedPreferences(holder.picture.getContext());
        String SColor = SP.getString("PrefColor", "#03A9F4");

        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + SColor + "'>" + a.getImagesCount() + "</font></b>" + "<font " +
                "color='#FFFFFF'> Photos</font>"));
        holder.name.setTag(a.Path);

        if (a.isSelected()) {
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.selected_album));
        } else {
            holder.card_layout.setBackgroundColor(holder.card_layout.getContext().getColor(R.color.unselected_album));
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
        TextView name;
        TextView nPhotos;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.picture);
            card_layout = (RelativeLayout) itemView.findViewById(R.id.layout_card_id);
            name = (TextView) itemView.findViewById(R.id.picturetext);
            nPhotos = (TextView) itemView.findViewById(R.id.image_number_text);
        }
    }
}



