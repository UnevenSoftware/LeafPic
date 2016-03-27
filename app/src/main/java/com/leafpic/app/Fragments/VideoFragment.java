package com.leafpic.app.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.leafpic.app.PhotoPagerActivity;
import com.leafpic.app.R;
import com.mikepenz.iconics.view.IconicsImageView;

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


        View view = inflater.inflate(R.layout.video_fragment, container, false);

        ImageView picture = (ImageView) view.findViewById(R.id.media_view);
        IconicsImageView videoInd = (IconicsImageView) view.findViewById(R.id.video_indicator);
        videoInd.setOnClickListener(onClickListener);

        Ion.with(getContext())
                .load(path)
                .withBitmap()
                .intoImageView(picture);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PhotoPagerActivity) getActivity()).toggleSystemUI();
            }
        });
        return view;
    }
}