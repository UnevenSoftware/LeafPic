package com.leafpic.app.Fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.R;

import java.util.concurrent.ExecutionException;

/**
 * Created by dnld on 18/02/16.
 */

public class ImageFragment extends Fragment {

    SubsamplingScaleImageView picture;
    ImageView preview_picture;
    Bitmap mThumbnailBitmap;
    private String path;
    private int width;
    private int height;
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
        width = getArguments().getInt("width", 500);
        height = getArguments().getInt("height", 500);
        path = getArguments().getString("path");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(picture!=null) {
            picture.recycle();
            mThumbnailBitmap.recycle();
            picture.setOnTouchListener(null);
        }
    }

    public void update() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                new DownloadFilesTask().execute();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);
        picture.recycle();
        preview_picture = (ImageView) view.findViewById(R.id.media_preview_view);
        final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.loading);
        spinner.setVisibility(View.VISIBLE);
        picture.setVisibility(View.INVISIBLE);
        preview_picture.setVisibility(View.INVISIBLE);
        update();
        picture.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);

       /* Glide.with(this)
                .load(path)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipMemoryCache(true)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                        //.override(width, height)
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
                        //preview_picture.setVisibility(View.VISIBLE);

                        //preview_picture.setImageBitmap(resource);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //picture.setImage(ImageSource.uri(path).dimensions(width, height), ImageSource.cachedBitmap(resource));
                                //picture.setImage(ImageSource.bitmap(resource));
                                picture.setImage(ImageSource.bitmap(resource));
                                picture.setVisibility(View.VISIBLE);
                                spinner.setVisibility(View.GONE);
                            }
                        });




                    }
                });*/



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

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mThumbnailBitmap = Glide.with(getContext())
                        .load(path)
                        .asBitmap()
                        .centerCrop()
                        .into(width, height)
                        .get();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        picture.setImage(ImageSource.bitmap(mThumbnailBitmap));
                    }
                });


            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {

        }
    }
}