package com.leafpic.app.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.koushikdutta.ion.Ion;
import com.leafpic.app.R;

/**
 * Created by dnld on 18/02/16.
 */
public class GifFragment extends Fragment {
    // Store instance variables

    private String path;
    private View.OnTouchListener onTouchListener;
    ImageView picture;

    // newInstance constructor for creating fragment with arguments
    public static GifFragment newInstance(String path) {
        GifFragment fragmentFirst = new GifFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    public void setOnTouchListener(View.OnTouchListener l){ onTouchListener = l;}

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getArguments().getString("path");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        picture.setOnTouchListener(null);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gif_pager_layout, container, false);
        picture = (ImageView) view.findViewById(R.id.media_view);
        Ion.with(getContext())
                .load(path)
                .intoImageView(picture);

        picture.setOnTouchListener(onTouchListener);
        return view;
    }
}