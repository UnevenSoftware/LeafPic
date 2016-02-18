package com.leafpic.app.Adapters;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;
import com.leafpic.app.Base.Photo;
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
        Glide.clear(holder.imageView);//fix corruption

        if (f.MIME.equals("image/gif")) {
            Ion.with(holder.imageView.getContext())
                    .load(f.Path)
                    .intoImageView(holder.imageView);
            holder.gifIcon.setVisibility(View.VISIBLE);
        } else {
            holder.gifIcon.setVisibility(View.INVISIBLE);


            Glide.with(holder.imageView.getContext())
                    .load(f.Path)
                    .asBitmap()
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
        return photos.size();
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void removeItemAt(int pos) {
        //Log.wtf("asdasd",getItemCount()+"");
        photos.remove(pos);
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



