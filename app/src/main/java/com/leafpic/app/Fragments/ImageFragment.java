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
            picture.setOnTouchListener(null);
        }
        if (mThumbnailBitmap != null)
            mThumbnailBitmap.recycle();
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
        //spinner.setVisibility(View.VISIBLE);
        spinner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.accent));
        picture.setVisibility(View.INVISIBLE);
        update();
        picture.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);

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
                                //.signature(new MediaStoreSignature(f.MIME, Long.parseLong(f.DateModified), f.orientation))
                        .asBitmap()
                        .centerCrop()
                                //.diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .skipMemoryCache(true)
                        .priority(Priority.IMMEDIATE)
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