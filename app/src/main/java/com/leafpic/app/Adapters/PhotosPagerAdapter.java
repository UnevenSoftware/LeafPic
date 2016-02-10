package com.leafpic.app.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.koushikdutta.ion.Ion;
import com.leafpic.app.Base.Photo;
import com.leafpic.app.R;

import java.util.ArrayList;

/**
 * Created by dnld on 1/11/16.
 */
public class PhotosPagerAdapter extends android.support.v4.view.PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<Photo> mResources;

    private View.OnClickListener onClickListener;

    public PhotosPagerAdapter(Context context, ArrayList<Photo> ph) {
        mContext = context;
        mResources = ph;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnClickListener(View.OnClickListener ls) {
        onClickListener = ls;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Photo f = mResources.get(position);
        View itemView;

        if (f.MIME.equals("image/gif")) {
            itemView = mLayoutInflater.inflate(R.layout.gif_pager_layout, container, false);

            ImageView picture = (ImageView) itemView.findViewById(R.id.media_view);
            picture.setOnClickListener(onClickListener);
            Ion.with(container.getContext())
                    .load(f.Path)
                    .intoImageView(picture);
        } else {
            itemView = mLayoutInflater.inflate(R.layout.image_pager_item, container, false);
            final SubsamplingScaleImageView picture = (SubsamplingScaleImageView) itemView.findViewById(R.id.media_view);

            Glide.with(container.getContext())
                    .load(mResources.get(position).Path)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            picture.setImage(ImageSource.bitmap(bitmap));
                        }
                    });
            picture.setOnClickListener(onClickListener);
            picture.setMaxScale(10);
        }



        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}