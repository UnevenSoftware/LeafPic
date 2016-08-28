package org.horaapps.leafpic.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class GifFragment extends Fragment {

    private String path;

    // newInstance constructor for creating fragment with arguments
    public static GifFragment newInstance(String path) {
        GifFragment fragmentFirst = new GifFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getArguments().getString("path");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PhotoView photoView = new PhotoView(container.getContext());

        Ion.with(getContext())
                .load(path)
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