package com.leafpic.app.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.leafpic.app.utils.ImageLoaderUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by dnld on 18/02/16.
 */
public class ImageFragment extends Fragment {

    private String path;
    private int width;
    private int height;
    SubsamplingScaleImageView picture;
    ImageView preview_picture;
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
    Bitmap mThumbnailBitmap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);
        preview_picture = (ImageView) view.findViewById(R.id.media_preview_view);
        final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.loading);
        preview_picture.setVisibility(View.GONE);

        Glide.with(this)
                .load(path)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
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
                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        // final ViewerActivity activity = (ViewerActivity) getActivity();

                        //recycleFullImageShowThumbnail();

                        //mThumbnailBitmap = resource;
                        preview_picture.setVisibility(View.VISIBLE);

                        preview_picture.setImageBitmap(resource);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                picture.setImage(ImageSource.uri(path).dimensions(width, height), ImageSource.cachedBitmap(resource));
                                //picture.setImage(ImageSource.bitmap(resource));
                                preview_picture.setVisibility(View.GONE);
                                picture.setVisibility(View.VISIBLE);
                            }
                        });




                    }
                });



       /* ImageLoader.getInstance().displayImage("file://"+path, new ImageViewAware(preview_picture), ImageLoaderUtils.fullSizeOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Input/Output error";
                        break;
                    case DECODING_ERROR:
                        message = "Image can't be decoded";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                picture.setImage(ImageSource.bitmap(loadedImage));
                picture.setVisibility(View.VISIBLE);
                preview_picture.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
            }
        });*/

        picture.setOnTouchListener(onTouchListener);
        picture.setMaxScale(10);
        return view;
    }

    private void recycleFullImageShowThumbnail() {
        if (picture != null) {
            picture.setOnTouchListener(null);
            picture.recycle();
            picture.setVisibility(View.INVISIBLE);
        }


        if (preview_picture != null) {
            preview_picture.setVisibility(View.VISIBLE);
        }
    }


    public void rotatePicture(int rotation) {
        Log.wtf("asdf" , picture.getOrientation()+"");
    }
}