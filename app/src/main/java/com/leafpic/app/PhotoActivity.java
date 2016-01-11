package com.leafpic.app;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
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

    boolean hidetoolbar=false;
    //Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);

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
            //ImageView picture = (ImageView) findViewById(R.id.current_picture);
            final SubsamplingScaleImageView picture = (SubsamplingScaleImageView)findViewById(R.id.imageView);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_empty)
                    .imageScaleType(ImageScaleType.NONE)
                    .cacheInMemory(true)
                    .build();

            //ImageLoader.getInstance().displayImage("file://" + f.Path, picture, defaultOptions);
            picture.setImage(ImageSource.uri("file://" + f.Path));
            picture.setMaxScale(10);

            //On Single Tap
            final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (picture.isReady()) {
                        OneTapImage();
                    }
                    return true;
                }
            });
            picture.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });
            //END TAP
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OneTapImage(){
        //Toast.makeText(PhotoActivity.this, "Toast", Toast.LENGTH_SHORT).show();
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        if(hidetoolbar==false) {
            toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        }
        else {
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        }
        hidetoolbar=!hidetoolbar;
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
        getSupportActionBar().setShowHideAnimationEnabled(true);
        toolbar.setBackgroundColor(getColor(R.color.trasparent_toolbar));
        //.setDisplayHomeAsUpEnabled(true);

    }

}
