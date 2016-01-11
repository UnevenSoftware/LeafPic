package com.leafpic.app;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.leafpic.app.Adapters.PhotosPagerAdapter;


/**
 * Created by dnld on 12/12/15.
 */
public class PhotoActivity extends AppCompatActivity {

    Album album;
    Photo f;

    boolean toolbar_hidden = false;
    Toolbar toolbar;
    PhotosPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        initUiTweaks();

        Bundle data = getIntent().getExtras();
        f = data.getParcelable("photo");
        album = data.getParcelable("album");

        try {
            final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    //if (picture.isReady()) {
                    if (!toolbar_hidden) {
                        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                    } else {
                        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                    }
                    toolbar_hidden = !toolbar_hidden;

                    return true;
                }
            });

            mCustomPagerAdapter = new PhotosPagerAdapter(this, album.photos);
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mCustomPagerAdapter);

           /* mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });*/





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
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(true);
        toolbar.setBackgroundColor(getColor(R.color.trasparent_toolbar));
    }

}
