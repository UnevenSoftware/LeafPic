package com.leafpic.app.Fragments;

import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leafpic.app.R;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Created by dnld on 18/02/16.
 */
public class ImageFragment extends Fragment {
    // Store instance variables

    private String path;
    private int width;
    private int height;

    SubsamplingScaleImageView picture;
    private View.OnTouchListener onTouchListener;

    // newInstance constructor for creating fragment with arguments
    public static ImageFragment newInstance(String path,int width,int height) {
        ImageFragment fragmentFirst = new ImageFragment();

        Bundle args = new Bundle();
        args.putInt("width", width);
        args.putInt("height", height);
        args.putString("path", path);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    public void setOnTouchListener(View.OnTouchListener l){onTouchListener = l;}

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width = getArguments().getInt("width", 300);
        height = getArguments().getInt("height", 300);
        path = getArguments().getString("path");
    }
    @Override
    public void onDestroy(){
        picture.recycle();
        super.onDestroy();
    }
   // int times=0;
    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_pager_item, container, false);
        picture = (SubsamplingScaleImageView) view.findViewById(R.id.media_view);

        Glide.with(container.getContext())
                .load(path)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .override(width, height)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        picture.setImage(ImageSource.bitmap(bitmap));
                    }
                });
       // times++;
        //picture.setImage(ImageSource.bitmap(BitmapFactory.decodeFile(path)));

        FutureTarget<File> future = Glide.with(getContext())
                .load(path)
                .downloadOnly(500, 500);

        /*try {

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //final BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inJustDecodeBounds = true;
                Log.wtf("asdasd", path);
                // picture.setImage(ImageSource.uri(Uri.fromFile(new File(path)).toString()).tilingEnabled());
                //Log.wtf("asdasdasdasdas","fulllllllll");
                // Glide.get(getActivity()).clearDiskCache();

/*try {
    FutureTarget<File> future = Glide.with(getContext())
            .load(path)
            .downloadOnly(500, 500);

    picture.setImage(ImageSource.uri(Uri.fromFile(future.get())));
    //future.get()

}catch (InterruptedException e){e.printStackTrace();}
catch (ExecutionException e){e.printStackTrace();}*/

                //File cacheFile = future.get();
              /*Glide.with(getContext())
                        .load(path)
                        .asBitmap()
                        .fitCenter()
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                                picture.setImage(ImageSource.bitmap(bitmap).tilingEnabled());

                            }
                        });*/
                Glide.get(getContext()).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
                //picture.setImage(ImageSource.uri("file://"+path));
                System.gc();

                //Glide.get(getActivity()).trimMemory(40);
            }
        });


        picture.setOnTouchListener(onTouchListener);
        picture.setMaxScale(10);
        return view;
    }
}