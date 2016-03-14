package com.leafpic.app.Fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

/**
 * Created by dnld on 18/02/16.
 */

public class ImageFragment extends Fragment {

    SubsamplingScaleImageView picture;
    ImageView preview_picture;
    Bitmap mThumbnailBitmap;
    private String path;
    private long DataModified;
    private int orientation;
    private String MIME;
    private View.OnTouchListener onTouchListener;

    public static ImageFragment newInstance(String path, String dateModified, int orientation, String mime) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putInt("orientation", orientation);
        args.putString("path", path);
        args.putString("dateModified", dateModified);
        args.putString("mime", mime);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientation = getArguments().getInt("orientation", 0);
        DataModified = Long.valueOf(getArguments().getString("dateModified", "0"));
        path = getArguments().getString("path");
        MIME = getArguments().getString("mime");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
      /*  if(picture!=null) {
            picture.recycle();
            picture.setOnTouchListener(null);
        }
        if (mThumbnailBitmap != null) {
            mThumbnailBitmap.recycle();
            Log.wtf("asd", "recycled: ");
        }*/
    }

    public void updatePhoto() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                System.gc();
                try {
                    Glide.with(getContext())
                            .load(path)
                            .asBitmap()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    //.signature(new MediaStoreSignature(MIME, DataModified, orientation))
                            .skipMemoryCache(true)
                            .priority(Priority.IMMEDIATE)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                    picture.setImage(ImageSource.bitmap(bitmap));
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);
        if (picture != null) picture.recycle();
        preview_picture = (ImageView) view.findViewById(R.id.media_preview_view);
        final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.loading);
        spinner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.accent));
        picture.setVisibility(View.INVISIBLE);
        updatePhoto();
        picture.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);

        picture.setOnTouchListener(onTouchListener);
        picture.setMaxScale(10);
        return view;
    }

    public void rotatePicture(int rotation) {
        Log.wtf("asdf" , picture.getOrientation()+"");
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                System.gc();
                Glide.with(getContext())
                        .load(path)
                                //.signature(new MediaStoreSignature(f.MIME, Long.parseLong(f.DateModified), f.orientation))
                        .asBitmap()
                        .centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.IMMEDIATE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                picture.setImage(ImageSource.bitmap(bitmap));
                            }
                        });


            } catch (OutOfMemoryError e) {
                StringUtils.showToast(getContext(), "Out of Memory!");
                e.printStackTrace();

            }
            return null;
        }

        protected void onPostExecute(Void result) {

        }
    }
}