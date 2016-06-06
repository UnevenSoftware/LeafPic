package com.horaapps.leafpic.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.Fragments.GifFragment;
import com.horaapps.leafpic.Fragments.ImageFragment;
import com.horaapps.leafpic.Fragments.VideoFragment;

import java.util.ArrayList;

/**
 * Created by dnld on 18/02/16.
 */

public class MediaPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Media> medias;
    private View.OnClickListener videoOnClickListener;
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


    public MediaPagerAdapter(FragmentManager fm, ArrayList<Media> medias) {
        super(fm);
        this.medias = medias;
    }

    public void setVideoOnClickListener(View.OnClickListener videoOnClickListener) {
        this.videoOnClickListener = videoOnClickListener;
    }

    @Override
    public Fragment getItem(int pos) {
        Media p = medias.get(pos);
        if (p.isImage()) {
            if (p.isGif()) return GifFragment.newInstance(p.getPath());
            else return ImageFragment.newInstance(p);
        } else {
            VideoFragment fragment = VideoFragment.newInstance(p.getPath());
            fragment.setOnClickListener(videoOnClickListener);
            return fragment;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
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