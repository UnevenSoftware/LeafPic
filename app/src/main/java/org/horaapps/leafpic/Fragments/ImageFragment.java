package org.horaapps.leafpic.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import org.horaapps.leafpic.Activities.SingleMediaActivity;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;

import java.util.Date;

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
        PhotoView photoView = new PhotoView(getContext());
        displayMedia(photoView, true);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                ((SingleMediaActivity) getActivity()).toggleSystemUI();
            }

            @Override
            public void onOutsidePhotoTap() {
                ((SingleMediaActivity) getActivity()).toggleSystemUI();
            }
        });
        //photoView.setMaximumScale(10.0F);
        return photoView;
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

    public void displayMedia(boolean changed) {
        displayMedia(((PhotoView) getView()), !changed);
    }

    private void displayMedia(PhotoView photoView, boolean useCache) {
        //PreferenceUtil SP = PreferenceUtil.getInstance(getContext());

        Glide.with(getContext())
                .load(img.getUri())
                .asBitmap()
                .signature(useCache ? img.getSignature(): new StringSignature(new Date().getTime()+""))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(0.5f)
                .animate(R.anim.fade_in)
                .into(photoView);

        /*if (SP.getBoolean("set_delay_full_image", true)) {
            Ion.with(getContext())
                    .load(img.getPath())
                    .withBitmap()
                    .deepZoom()
                    .intoImageView(photoView);
        } else {
            Glide.with(getContext())
                    .load(img.getUri())
                    .asBitmap()
                    .signature(img.getSignature())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .animate(R.anim.fade_in)
                    .into(photoView);
        }*/


    }

    public boolean rotatePicture(int rotation) {
        // TODO: 28/08/16 not working yet
        /*PhotoView photoView = (PhotoView) getView();
        int orientation = Measure.rotateBy(img.getOrientation(), rotation);

        if(photoView != null && img.setOrientation(orientation)) {
            Glide.clear(photoView);
            Glide.with(getContext())
                    .load(img.getUri())
                    .asBitmap()
                    //.signature(img.getSignature())
                    //.diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    //.thumbnail(0.5f)
                    .transform(new RotateTransformation(getContext(), orientation))
                    //.animate(R.anim.fade_in)
                    .into(photoView);

            return true;
        }*/
        return false;
    }
}