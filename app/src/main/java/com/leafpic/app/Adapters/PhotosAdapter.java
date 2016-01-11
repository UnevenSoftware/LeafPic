package com.leafpic.app.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.leafpic.app.Photo;
import com.leafpic.app.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    ArrayList<Photo> photos;
    private int layout_ID;
    private View.OnClickListener mOnClickListener;

    public PhotosAdapter(ArrayList<Photo> ph, int id) {
        photos = ph;
        layout_ID = id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout_ID, parent, false);
        v.setOnClickListener(mOnClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PhotosAdapter.ViewHolder holder, int position) {

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_empty)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheInMemory(true)

                .build();

        ImageLoader.getInstance().displayImage("file://" + photos.get(position).Path,
                holder.imageView, defaultOptions);
        holder.imageView.setTag(photos.get(position).Path);
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }


    @Override
    public int getItemCount() {
        return photos.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.pic);
        }
    }
}



