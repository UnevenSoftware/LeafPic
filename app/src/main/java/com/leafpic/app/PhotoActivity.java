package com.leafpic.app;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotoActivity extends AppCompatActivity {

    Album album;
    Photo f;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        initUiTweaks();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(size.x, size.y)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().destroy();
        ImageLoader.getInstance().init(config);
        initUiTweaks();

        Bundle data = getIntent().getExtras();
        f = data.getParcelable("photo");
        album = data.getParcelable("album");

        try {
            ImageView picture = (ImageView) findViewById(R.id.current_picture);
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_empty)
                    .imageScaleType(ImageScaleType.NONE)
                    .cacheInMemory(true)
                    .build();


            ImageLoader.getInstance().displayImage("file://" + f.Path, picture, defaultOptions);


            // setTitle(album.DisplayName);


            //photos = new HandlingPhotos(this, album.Path, album.isHidden());

            /*adapter = new PhotosAdapter(this, R.layout.photo_card, album.photos);
            photosgrid = (GridView) findViewById(R.id.gridPhotos);
            photosgrid.setAdapter(adapter);
            photosgrid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Photo f = (Photo) parent.getItemAtPosition(position);
                    string.showToast(PhotoActivity.this,f.Path);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            hidden = album.isHidden();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case android.R.id.home:
                finish();
                //NavUtils.navigateUpFromSameTask(this);
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    public void initUiTweaks() {

        /**** Status Bar */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.status_bar));


        /**** ToolBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //.setDisplayHomeAsUpEnabled(true);

    }
}
