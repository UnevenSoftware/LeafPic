package com.leafpic.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.Photo;
import com.leafpic.app.R;

import java.util.ArrayList;

/**
 * Created by dnld on 1/11/16.
 */
public class PhotosPagerAdapter extends android.support.v4.view.PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<Photo> mResources;

    public PhotosPagerAdapter(Context context, ArrayList<Photo> ph) {
        mContext = context;
        mResources = ph;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        final SubsamplingScaleImageView picture = (SubsamplingScaleImageView) itemView.findViewById(R.id.imageView);
        picture.setImage(ImageSource.uri("file://" + mResources.get(position).Path));
        picture.setMaxScale(10);

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}