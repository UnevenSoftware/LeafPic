package com.leafpic.app.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.leafpic.app.R;
import com.mikepenz.iconics.view.IconicsImageView;

/**
 * Created by dnld on 18/02/16.
 */

public class VideoFragment extends Fragment {

    ImageView picture;
    IconicsImageView videoInd;
    private String path;
    private View.OnTouchListener onTouchListener;
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

    public void setOnTouchListener(View.OnTouchListener l) {
        onTouchListener = l;
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
        picture = (ImageView) view.findViewById(R.id.media_view);
        videoInd = (IconicsImageView) view.findViewById(R.id.video_indicator);

        Ion.with(getContext())
                .load(path)
                .withBitmap()
                .intoImageView(picture);

        videoInd.setOnClickListener(onClickListener);
        picture.setOnTouchListener(onTouchListener);
        return view;
    }
}