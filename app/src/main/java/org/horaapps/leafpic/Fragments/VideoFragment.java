package org.horaapps.leafpic.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.ion.Ion;
import org.horaapps.leafpic.Activities.PhotoPagerActivity;

import com.mikepenz.iconics.view.IconicsImageView;

import io.fabric.sdk.android.Fabric;

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
        Fabric.with(getContext(), new Crashlytics());
        path = getArguments().getString("path");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(org.horaapps.leafpic.R.layout.fragment_video, container, false);

        ImageView picture = (ImageView) view.findViewById(org.horaapps.leafpic.R.id.media_view);
        IconicsImageView videoInd = (IconicsImageView) view.findViewById(org.horaapps.leafpic.R.id.icon);
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