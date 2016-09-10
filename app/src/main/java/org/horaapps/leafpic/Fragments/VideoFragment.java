package org.horaapps.leafpic.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.Activities.SingleMediaActivity;

/**
 * Created by dnld on 18/02/16.
 */

public class VideoFragment extends MediaFragment {

    private View.OnClickListener onClickListener;

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(org.horaapps.leafpic.R.layout.fragment_video, container, false);

        ImageView picture = (ImageView) view.findViewById(org.horaapps.leafpic.R.id.media_view);
        IconicsImageView videoInd = (IconicsImageView) view.findViewById(org.horaapps.leafpic.R.id.icon);
        videoInd.setOnClickListener(onClickListener);

        Ion.with(getContext())
                .load(getMedia().getPath())
                .withBitmap()
                .intoImageView(picture);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SingleMediaActivity) getActivity()).toggleSystemUI();
            }
        });
        return view;
    }
}