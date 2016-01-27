package com.leafpic.app.Adapters;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.leafpic.app.Photo;
import com.leafpic.app.R;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    ArrayList<Photo> photos;
    private int layout_ID;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public PhotosAdapter(ArrayList<Photo> ph, int id) {
        photos = ph;
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
    public void onBindViewHolder(PhotosAdapter.ViewHolder holder, int position) {
        Photo f = photos.get(position);


        if (f.MIME.equals("image/gif")) {
            Glide.with(holder.imageView.getContext())
                    .load(f.Path)
                    .asGif()
                    .centerCrop()
                    .placeholder(R.drawable.ic_empty)
                    //.crossFade()
                    .into(holder.imageView);
            holder.gifIcon.setVisibility(View.VISIBLE);
        } else {
            holder.gifIcon.setVisibility(View.INVISIBLE);
            Glide.with(holder.imageView.getContext())
                    .load(f.Path)
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.ic_empty)
                    //.crossFade()
                    .into(holder.imageView);
        }

        holder.path.setTag(f.Path);
        if (f.isSelected()) {
            holder.selectHolder.setVisibility(View.VISIBLE);
            holder.imageView.setPadding(15, 15, 15, 15);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.selectHolder.setVisibility(View.INVISIBLE);
            holder.imageView.setPadding(0, 0, 0, 0);
            holder.imageView.clearColorFilter();
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
        return photos.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, selectHolder, gifIcon;
        TextView path;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.pic);
            selectHolder = (ImageView) itemView.findViewById(R.id.selectedPicIcon);
            gifIcon = (ImageView) itemView.findViewById(R.id.gifIcon);
            path = (TextView) itemView.findViewById(R.id.path);
        }
    }
}



