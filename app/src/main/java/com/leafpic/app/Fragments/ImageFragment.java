package com.leafpic.app.Fragments;

import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by dnld on 18/02/16.
 */
public class ImageFragment extends Fragment {

    private String path;
    private int width;
    private int height;
    private Bitmap mThumbnailBitmap;
    SubsamplingScaleImageView picture;
    private View.OnTouchListener onTouchListener;

    public static ImageFragment newInstance(String path,int width,int height) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putInt("width", width);
        args.putInt("height", height);
        args.putString("path", path);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width = getArguments().getInt("width", 300);
        height = getArguments().getInt("height", 300);
        path = getArguments().getString("path");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(picture!=null) {
            picture.recycle();
            picture.setOnTouchListener(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);

        Glide.with(container.getContext())
                .load(path)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .override(width, height)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        mThumbnailBitmap = bitmap;
                        //loadFullImage();
                        picture.setImage(ImageSource.bitmap(bitmap));
                    }
                });


        picture.setDebug(true);
        //picture.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        picture.setOnTouchListener(onTouchListener);
        picture.setMaxScale(10);
        return view;
    }
    public void loadFullImage(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                  picture.setImage(ImageSource.uri(path).dimensions(500, 500), ImageSource.cachedBitmap(mThumbnailBitmap));
              }
          });
    }
}