package org.horaapps.leafpic.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.horaapps.leafpic.data.Media;
import pl.droidsonroids.gif.GifImageView;

/**
 * Media Fragment for showing an Image (static)
 */
public class GifFragment extends BaseMediaFragment {

    @NonNull
    public static GifFragment newInstance(@NonNull Media media) {
        return BaseMediaFragment.newInstance(new GifFragment(), media);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GifImageView photoView = new GifImageView(getContext());
        photoView.setImageURI(media.getUri());
        setTapListener(photoView);
        return photoView;
    }
}
