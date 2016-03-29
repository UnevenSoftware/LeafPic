package com.leafpic.app.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.ion.Ion;
import com.leafpic.app.PhotoPagerActivity;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by dnld on 18/02/16.
 */

public class ImageFragment extends Fragment {

    private String path;
    private long DataModified;
    private int orientation;
    private String MIME;

    public static ImageFragment newInstance(String path, long dateModified, int orientation, String mime) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putInt("orientation", orientation);
        args.putString("path", path);
        args.putLong("dateModified", dateModified);
        args.putString("mime", mime);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    //public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientation = getArguments().getInt("orientation", 0);
        DataModified = getArguments().getLong("dateModified", 0);
        path = getArguments().getString("path");
        MIME = getArguments().getString("mime");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PhotoView photoView = new PhotoView(container.getContext());

        Ion.with(getContext())
                .load(path)
                .withBitmap()
                .deepZoom()
                .intoImageView(photoView);

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
        //photoView.setMinimumScale(0.85F);
        photoView.setMaximumScale(4.0F);

        return photoView;
    }

    public void rotatePicture(int rotation) {

    }
}