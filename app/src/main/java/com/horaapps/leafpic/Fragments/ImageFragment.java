package com.horaapps.leafpic.Fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.utils.Measure;
import com.koushikdutta.async.future.Cancellable;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.horaapps.leafpic.PhotoPagerActivity;
import com.koushikdutta.ion.future.ImageViewFuture;

import java.util.concurrent.ExecutionException;

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
        final SubsamplingScaleImageView imageView =  new SubsamplingScaleImageView(getContext());
        final ImageView imag =  new ImageView(getContext());

        if (SP.getBoolean("set_delay_full_image", true)) {
            Ion.with(getContext())
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

            Ion.with(getContext())
                    .load(img.getPath())
                    .withBitmap()
                    .deepZoom()
                    .intoImageView(photoView);

            return photoView;

        } else {

            imageView.setImage(ImageSource.uri(img.getUri()).tilingEnabled());
            imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PhotoPagerActivity) getActivity()).toggleSystemUI();
                }
            });
            return imageView;
        }
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
        View view = getView();
        if (view.getClass().equals(SubsamplingScaleImageView.class)) {
            int orienatation = Measure.rotateBy(img.getOrientation(),rotation);
            if(img.setOrientation(orienatation))
                ((SubsamplingScaleImageView) view).setOrientation(orienatation);
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