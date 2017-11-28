package org.horaapps.leafpic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.horaapps.leafpic.activities.SingleMediaActivity;
import org.horaapps.leafpic.data.Media;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by dnld on 18/02/16.
 */
public class GifFragment extends Fragment {

    private Media gif;

    public static GifFragment newInstance(Media media) {
        GifFragment gifFragment = new GifFragment();

        Bundle args = new Bundle();
        args.putParcelable("gif", media);
        gifFragment.setArguments(args);

        return gifFragment;

    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gif = getArguments().getParcelable("gif");
    }


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GifImageView photoView = new GifImageView(container.getContext());
        photoView.setImageURI(gif.getUri());
        photoView.setOnClickListener(view -> ((SingleMediaActivity) getActivity()).toggleSystemUI());
        return photoView;
    }
}