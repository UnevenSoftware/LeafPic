package com.leafpic.app.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.leafpic.app.PhotoPagerActivity;
import com.leafpic.app.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by dnld on 18/02/16.
 */

public class ImageFragment extends Fragment {

    private String path;

    PhotoView photoView;
    //PhotoViewAttacher mAttacher;

    public static ImageFragment newInstance(String path) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putString("path", path);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    //public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getArguments().getString("path");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Need to call clean-up

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //photoView = new PhotoView(container.getContext());
        View view = inflater.inflate(R.layout.image_fragment, container, false);

        photoView = (PhotoView) view.findViewById(R.id.media_view);

        Ion.with(getContext())
                .load(path)
                .withBitmap()
                .deepZoom()
                .intoImageView(photoView);
        //mAttacher = new PhotoViewAttacher(photoView,true);
        //photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                ((PhotoPagerActivity) getActivity()).toggleSystemUI();
            }

            @Override
            public void onOutsidePhotoTap() {
                ((PhotoPagerActivity) getActivity()).toggleSystemUI();
            }
        });
        photoView.setZoomTransitionDuration(375);
        photoView.setMaximumScale(6.0F);//first set maximum
        photoView.setMinimumScale(1.0F);
        photoView.setMediumScale(3.5F);

        return view;
    }

    private void rotateLoop() { //april fools
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                photoView.setRotationBy(1);
                rotateLoop();
            }
        }, 5);
    }

    public void rotatePicture(int rotation) {
        if (photoView!=null)
            photoView.setRotationBy(rotation);
        else Log.d("asdasdas", "rotatePicture: nulll");
    }
}