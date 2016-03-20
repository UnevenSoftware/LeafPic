package com.leafpic.app.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.leafpic.app.Base.Media;
import com.leafpic.app.Fragments.GifFragment;
import com.leafpic.app.Fragments.ImageFragment;
import com.leafpic.app.Fragments.VideoFragment;

import java.util.ArrayList;

/**
 * Created by dnld on 18/02/16.
 */

public class MediaPagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Media> medias;
    View.OnTouchListener listener;
    View.OnClickListener videoOnClickListener;

    public MediaPagerAdapter(FragmentManager fm, ArrayList<Media> medias) {
        super(fm);
        this.medias = medias;
    }

    public void setVideoOnClickListener(View.OnClickListener videoOnClickListener) {
        this.videoOnClickListener = videoOnClickListener;
    }

    public void setOnTouchListener(View.OnTouchListener l) {
        listener = l;
    }

    @Override
    public Fragment getItem(int pos) {
        Media p = medias.get(pos);
        if (p.isImage()) {
            if (p.isGif()) {
                GifFragment fragment = GifFragment.newInstance(p.Path);
                fragment.setOnTouchListener(listener);
                return fragment;
            } else {
                ImageFragment fragment = ImageFragment.newInstance(p.Path, p.DateModified, p.orientation, p.MIME);
                fragment.setOnTouchListener(listener);
                return fragment;
            }
        } else {
            VideoFragment fragment = VideoFragment.newInstance(p.Path);
            fragment.setOnTouchListener(listener);
            fragment.setOnClickListener(videoOnClickListener);
            return fragment;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public int getCount() {
        return medias.size();
    }
}