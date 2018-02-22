package org.horaapps.leafpic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.SingleMediaActivity;
import org.horaapps.leafpic.data.Media;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by dnld on 18/02/16.
 */

@SuppressWarnings("ResourceType")
public class ImageFragment extends Fragment {

    View view;
    private Media img;
    private Unbinder unbinder;

    @BindView(R.id.subsampling_view)
    SubsamplingScaleImageView subsampling;

    public static ImageFragment newInstance(Media media) {
        ImageFragment imageFragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelable("image", media);
        imageFragment.setArguments(args);
        return imageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        img = getArguments().getParcelable("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_photo, container, false);
        unbinder = ButterKnife.bind(this, view);

        subsampling.setOrientation(SubsamplingScaleImageView.ORIENTATION_0);
        subsampling.setImage(ImageSource.uri(img.getUri()));
        subsampling.setOnClickListener(view -> ((SingleMediaActivity) getActivity()).toggleSystemUI());

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subsampling.recycle();
        unbinder.unbind();
    }

    /* private void rotateLoop() { //april fools
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                photoView.setRotationBy(1);
                rotateLoop();
            }
        }, 5);
    }*/

    public void rotatePicture(int rotation) {
        if (rotation == -90 && subsampling.getOrientation() == 0)
            subsampling.setOrientation(SubsamplingScaleImageView.ORIENTATION_270);
        else
            subsampling.setOrientation((subsampling.getOrientation() + rotation) % 360);
    }
}