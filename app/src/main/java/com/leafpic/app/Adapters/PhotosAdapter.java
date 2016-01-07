package com.leafpic.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.leafpic.app.Photo;
import com.leafpic.app.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotosAdapter extends ArrayAdapter<Photo> {

    ArrayList<Photo> photos;
    private Context localContext;
    private int layout_ID;
    //Bitmap photoPlaceholder = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_launcher);

    public PhotosAdapter (Context ct,int Layout_ID, ArrayList<Photo> a){
        super(ct, Layout_ID, a);
        this.localContext = ct;
        this.layout_ID = Layout_ID;
        photos = a;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ImageView picture;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) localContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(this.layout_ID, parent, false);
            v.setTag(R.id.pic, v.findViewById(R.id.pic));
        } else
            v = convertView;

        picture = (ImageView) v.getTag(R.id.pic);
        picture.setTag(photos.get(position).Path);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_empty)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();


        ImageLoader.getInstance().displayImage("file://"+photos.get(position).Path, picture, defaultOptions);


        return v;
    }
}