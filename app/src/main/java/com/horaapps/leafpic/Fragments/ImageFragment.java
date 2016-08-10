package com.horaapps.leafpic.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.horaapps.leafpic.Data.Media;
import com.horaapps.leafpic.PhotoPagerActivity;
import com.horaapps.leafpic.utils.Measure;
import com.horaapps.leafpic.utils.PreferenceUtil;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by dnld on 18/02/16.
 */

@SuppressWarnings("ResourceType")
public class ImageFragment extends Fragment {

    private Media img;

    public static ImageFragment newInstance(Media asd) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putParcelable("image", asd);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    //public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        img = getArguments().getParcelable("image");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Need to call clean-up

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final SubsamplingScaleImageView imageView =  new SubsamplingScaleImageView(getContext());
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        displayMedia(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PhotoPagerActivity) getActivity()).toggleSystemUI();
            }
        });

        return imageView;
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

    public void displayMedia() {
        displayMedia(((SubsamplingScaleImageView) getView()));
    }

    private void displayMedia(SubsamplingScaleImageView imageView) {
        PreferenceUtil SP = PreferenceUtil.getInstance(getContext());

        if (SP.getBoolean("set_delay_full_image", true)) {
            /*Ion.with(getContext())
                    .load(img.getPath())
                    .withBitmap()
                    .deepZoom()
                    .intoImageView(photoView);

            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    ((PhotoPagerActivity) getActivity()).toggleSystemUI();
                }

                @Override
                public void onOutsidePhotoTap() {
                    ((PhotoPagerActivity) getActivity()).toggleSystemUI();
                }
            });
            photoView.setZoomTransitionDuration(375);
            photoView.setScaleLevels(1.0F, 4.5F, 10.0F);//TODO improve


            return photoView;*/
            imageView.setImage(ImageSource.uri(img.getUri()));
            imageView.setMaxScale(5);
        } else {
            imageView.setImage(ImageSource.uri(img.getUri()).tilingEnabled());
            imageView.setMaxScale(5);
        }


    }

    public void rotatePicture(int rotation) {
        View view = getView();
        if (view.getClass().equals(SubsamplingScaleImageView.class)) {
            int orientation = Measure.rotateBy(img.getOrientation(), rotation);
            if(img.setOrientation(orientation))
                ((SubsamplingScaleImageView) view).setOrientation(orientation);
        } else {
                int orienatation = Measure.rotateBy(img.getOrientation(),rotation);
            //((ImageView) view).setRotation(orienatation);
               ((PhotoView) view).setRotationBy(rotation);
                if (orienatation==0)
                    ((PhotoView) view).setMinimumScale(1.0F);
                else
                    ((PhotoView) view).setMinimumScale(0.65F);
                //photoView.setRotationBy(rotation);
                //photoView.refreshDrawableState();
                //photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }
}