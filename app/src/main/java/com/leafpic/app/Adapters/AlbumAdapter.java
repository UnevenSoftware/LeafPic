package com.leafpic.app.Adapters;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.leafpic.app.Album;
import com.leafpic.app.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

/**
 * Created by dnld on 12/12/15.
 */

public class AlbumAdapter extends ArrayAdapter<Album> {

    ArrayList<Album> albums;
    Bitmap photoPlaceHolder = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_launcher);
    private Context localContext;
    private int layout_ID;

     public AlbumAdapter (Context ct,int Layout_ID, ArrayList<Album> a){
         super(ct, Layout_ID, a);
         this.localContext = ct;
         this.layout_ID = Layout_ID;
         albums = a;
     }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout card_layout;
        View v;
        ImageView picture;
        TextView name;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) localContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(this.layout_ID, parent, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.picturetext, v.findViewById(R.id.picturetext));
            v.setTag(R.id.layout_card_id, v.findViewById(R.id.layout_card_id));


        } else
            v = convertView;

        card_layout = (RelativeLayout) v.getTag(R.id.layout_card_id);
        picture = (ImageView) v.getTag(R.id.picture);
        name = (TextView) v.getTag(R.id.picturetext);

        Album a = albums.get(position);
        //non so se serve
        picture.setTag(a.getPathCoverAlbum());

        //impostazioni libreria immagini
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_empty)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        //Setta immagine
        ImageLoader.getInstance().displayImage(a.getPathCoverAlbum(), picture, defaultOptions);
        //setta titolo
        name.setText(a.DisplayName);
        //setta numero immaghini
        //a.getImagesCount();

        if (a.isSelected()) {
            //name.setBackgroundColor(localContext.getColor(R.color.selected_album));
            card_layout.setBackgroundColor(localContext.getColor(R.color.selected_album));
        } else {
            //name.setBackgroundColor(localContext.getColor(R.color.unselected_album));
            card_layout.setBackgroundColor(localContext.getColor(R.color.unselected_album));
        }
        return v;
    }

}