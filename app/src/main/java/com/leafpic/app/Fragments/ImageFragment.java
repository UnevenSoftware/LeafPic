package com.leafpic.app.Fragments;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.MainActivity;
import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

/**
 * Created by dnld on 18/02/16.
 */
public class ImageFragment extends Fragment {
    // Store instance variables

    private String path;
    private int width;
    private int height;

    // newInstance constructor for creating fragment with arguments
    public static ImageFragment newInstance(String path,int width,int height) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putInt("width", width);
        args.putInt("height", height);
        args.putString("path", path);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width = getArguments().getInt("width", 300);
        height = getArguments().getInt("height", 300);
        path = getArguments().getString("path");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        final SubsamplingScaleImageView picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);

         picture.recycle();

        Glide.with(container.getContext())
                .load(path)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        picture.setImage(ImageSource.bitmap(bitmap));
                    }
                });
       /* picture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });*/
        picture.setMaxScale(10);
        return view;
    }
}