package com.leafpic.app.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.R;

/**
 * Created by dnld on 18/02/16.
 */
public class ImageFragment extends Fragment {
    // Store instance variables

    private String path;
    private int width;
    private int height;
    private View.OnTouchListener onTouchListener;

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

    public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width = getArguments().getInt("width", 300);
        height = getArguments().getInt("height", 300);
        path = getArguments().getString("path");
    }
    int times=0;
    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        final SubsamplingScaleImageView picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);
       // if (picture!=null)
         //   Glide.clear(picture);


        Glide.with(container.getContext())
                .load(path)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .override(width, height)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!isFromMemoryCache) {
                            Log.e("ViewerPager", "Image not from cache:" + model + " " + target.toString());
                        } else {
                            Log.e("ViewerPager", "Image from cache:" + model + " " + target.toString());
                        }
                        return false;
                    }
                })
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        picture.setImage(ImageSource.bitmap(bitmap));
                    }
                });
        times++;

        //TODO load full image size when starting zooming
        picture.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {

            }

            @Override
            public void onImageLoaded() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (times == 1)
                        Glide.with(getContext())
                                .load(path)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.IMMEDIATE)
                                .dontAnimate()
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                                        picture.setImage(ImageSource.bitmap(bitmap));
                                    }
                                });
                        times++;
                    }
                }, 250);
            }

            @Override
            public void onPreviewLoadError(Exception e) {

            }

            @Override
            public void onImageLoadError(Exception e) {

            }

            @Override
            public void onTileLoadError(Exception e) {

            }
        });
        picture.setOnTouchListener(onTouchListener);
        picture.setMaxScale(10);
        return view;
    }
}