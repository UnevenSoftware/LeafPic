package com.leafpic.app.Adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.leafpic.app.Base.Photo;
import com.leafpic.app.Fragments.ImageFragment;
import com.leafpic.app.R;

import java.util.ArrayList;

/**
 * Created by dnld on 18/02/16.
 */

public class MediaPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Photo> photos;
    View.OnTouchListener listener;
    public void  setOnTouchListener(View.OnTouchListener l){ listener = l; }

    public MediaPagerAdapter(FragmentManager fm, ArrayList<Photo> photos) {
        super(fm);
        this.photos=photos;
    }

    @Override
    public Fragment getItem(int pos) {
        Photo p = photos.get(pos);

        ImageFragment fragment = ImageFragment.newInstance(p.Path,300,300);
        fragment.setOnTouchListener(listener);
        return fragment;
    }

    @Override
    public int getCount() {
        return photos.size();
    }
}