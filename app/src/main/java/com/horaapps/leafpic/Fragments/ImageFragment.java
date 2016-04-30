package com.horaapps.leafpic.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.horaapps.leafpic.Base.Media;
import com.koushikdutta.ion.Ion;
import com.horaapps.leafpic.PhotoPagerActivity;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

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

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
        PhotoView photoView = new PhotoView(getContext());

        if (SP.getBoolean("set_delay_full_image", true)) {
            Ion.with(getContext())
                    .load(img.getPath())
                    .withBitmap()
                    .deepZoom()
                    .intoImageView(photoView);
        } else {
            Glide.with(getContext())
                    .load(img.getPath())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .skipMemoryCache(true)
                    .priority(Priority.HIGH)
                    .signature(new MediaStoreSignature(img.getMIME(), img.getDateModified(), img.getOrientation()))
                            //.centerCrop()
                    .into(photoView);
        }
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
        photoView.setScaleLevels(1.0F, 3.5F, 6.0F);
        return photoView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //StringUtils.showToast(getContext(),"resume");
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
        PhotoView photoView = (PhotoView) getView();
        if (photoView!=null){
            photoView.setRotationBy(rotation);
            //photoView.refreshDrawableState();
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }
}