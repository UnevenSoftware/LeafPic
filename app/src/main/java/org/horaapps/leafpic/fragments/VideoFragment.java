package org.horaapps.leafpic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.activities.SingleMediaActivity;
import org.horaapps.leafpic.data.ContentHelper;
import org.horaapps.leafpic.data.Media;

/**
 * Created by dnld on 18/02/16.
 */

public class VideoFragment extends Fragment {

    private Media video;

    public static VideoFragment newInstance(Media media) {
        VideoFragment videoFragment = new VideoFragment();

        Bundle args = new Bundle();
        args.putParcelable("video", media);
        videoFragment.setArguments(args);

        return videoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        video = getArguments().getParcelable("video");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(org.horaapps.leafpic.R.layout.fragment_video, container, false);

        ImageView picture = (ImageView) view.findViewById(org.horaapps.leafpic.R.id.media_view);
        IconicsImageView videoInd = (IconicsImageView) view.findViewById(org.horaapps.leafpic.R.id.icon);
        videoInd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(
                        new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(ContentHelper.getUriForFile(getContext(), video.getFile()),
                                        video.getMimeType()));
            }
        });

        RequestOptions options = new RequestOptions()
                .signature(video.getSignature())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);


        Glide.with(getContext())
                .load(video.getUri())
                .apply(options)
                .into(picture);

        picture.setOnClickListener(v -> ((SingleMediaActivity) getActivity()).toggleSystemUI());
        return view;
    }
}