package com.leafpic.app.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.ion.Ion;
import com.leafpic.app.PhotoPagerActivity;
import com.leafpic.app.R;
import com.mikepenz.iconics.view.IconicsImageView;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by dnld on 18/02/16.
 */

public class VideoFragment extends Fragment {

    private String path;
    private View.OnClickListener onClickListener;

    public static VideoFragment newInstance(String path) {
        VideoFragment fragmentFirst = new VideoFragment();

        Bundle args = new Bundle();
        args.putString("path", path);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getArguments().getString("path");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.video_pager_layout, container, false);

        PhotoView picture = (PhotoView) view.findViewById(R.id.media_view);
        IconicsImageView videoInd = (IconicsImageView) view.findViewById(R.id.video_indicator);

        Ion.with(getContext())
                .load(path)
                .withBitmap()
                .intoImageView(picture);
       // picture.setZoomable(false);

        picture.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                ((PhotoPagerActivity) getActivity()).toggleSystemUI();
            }

            @Override
            public void onOutsidePhotoTap() {
                ((PhotoPagerActivity) getActivity()).toggleSystemUI();
            }
        });

        //Log.wtf("asd",picture.getOnPhotoTapListener().toString());
        videoInd.setOnClickListener(onClickListener);

        //TODO optimize zoom disabled
        picture.setMinimumScale(1.0F);
        picture.setMediumScale(1.0000001F);
        picture.setMaximumScale(1.00001F);
       // picture.setZoomable(false);
        return view;
    }
}