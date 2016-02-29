package com.leafpic.app.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.leafpic.app.Base.Media;
import com.leafpic.app.Fragments.GifFragment;
import com.leafpic.app.Fragments.ImageFragment;

import java.util.ArrayList;

/**
 * Created by dnld on 18/02/16.
 */

public class MediaPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Media> medias;
    View.OnTouchListener listener;

    public void  setOnTouchListener(View.OnTouchListener l){ listener = l; }

    public MediaPagerAdapter(FragmentManager fm, ArrayList<Media> medias) {
        super(fm);
        this.medias = medias;
    }

    @Override
    public Fragment getItem(int pos) {
        Media p = medias.get(pos);

        if (p.isGif()){
            GifFragment fragment = GifFragment.newInstance(p.Path);
            fragment.setOnTouchListener(listener);
            return fragment;
        } else {
            ImageFragment fragment = ImageFragment.newInstance(p.Path, 300, 300);
            fragment.setOnTouchListener(listener);
            return fragment;
        }
    }

    public void removeFragmentat(int index){
        medias.remove(index);
        //getItem(index).onDestroy();

        notifyDataSetChanged();
        //medias.remove(index)
    }

    public void rotatepictureAt(int index){
        ImageFragment asd = (ImageFragment) getItem(index);
        asd.rotatePicture(0);
        //getItem(index).re
    }

    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public int getCount() {
        return medias.size();
    }
}