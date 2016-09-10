package org.horaapps.leafpic.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.ion.Ion;

import org.horaapps.leafpic.Activities.SingleMediaActivity;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by dnld on 18/02/16.
 */
public class GifFragment extends MediaFragment {


    // newInstance constructor for creating fragment with arguments
    public static GifFragment newInstance() {
        return new GifFragment();
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PhotoView photoView = new PhotoView(container.getContext());

        Ion.with(getContext())
                .load(getMedia().getPath())
                .intoImageView(photoView);

        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                ((SingleMediaActivity) getActivity()).toggleSystemUI();
            }

            @Override
            public void onOutsidePhotoTap() {
                ((SingleMediaActivity) getActivity()).toggleSystemUI();
            }
        });

        return photoView;
    }
}