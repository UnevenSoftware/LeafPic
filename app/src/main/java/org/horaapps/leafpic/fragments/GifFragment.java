package org.horaapps.leafpic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.koushikdutta.ion.Ion;

import org.horaapps.leafpic.activities.SingleMediaActivity;
import org.horaapps.leafpic.data.Media;

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
        PhotoView photoView = new PhotoView(container.getContext());

        Ion.with(getContext())
                .load(gif.getPath())
                .intoImageView(photoView);

        photoView.setOnClickListener(view -> ((SingleMediaActivity) getActivity()).toggleSystemUI());
        return photoView;
    }
}