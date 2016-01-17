package com.leafpic.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.leafpic.app.Adapters.PhotosPagerAdapter;
import com.leafpic.app.utils.string;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotoActivity extends AppCompatActivity {


    HandlingPhotos photos;

    boolean toolbar_hidden = false;
    Toolbar toolbar;
    PhotosPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        initUiTweaks();



        try {
            Bundle data = getIntent().getExtras();

            photos = data.getParcelable("album");
            photos.setContext(PhotoActivity.this);
            final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    //if (picture.isReady()) {
                    string.showToast(getApplicationContext(), "click");
                    if (!toolbar_hidden) {
                        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                    } else {
                        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                    }
                    toolbar_hidden = !toolbar_hidden;

                    return true;
                }
            });

            mCustomPagerAdapter = new PhotosPagerAdapter(this, photos.photos);
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mCustomPagerAdapter);
            mViewPager.setCurrentItem(photos.getCurrentPhotoIndex());

            /*mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
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
            case R.id.shareButton:
                String file_path = photos.photos.get(mViewPager.getCurrentItem()).Path;
                string.showToast(this, file_path);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(string.getMimeType(file_path));
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file_path));
                startActivity(Intent.createChooser(share, "Share Image"));
                return true;
            case R.id.deletePhoto:
                photos.deletePhoto(photos.photos.get(mViewPager.getCurrentItem()).Path);
                mCustomPagerAdapter.notifyDataSetChanged();
                return true;
            case R.id.rotatePhoto:
                return true;
            case R.id.setWallpaper:

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void initUiTweaks() {
        /**** Navigation Bar*/
        /*
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.toolbar));
        }
        */
        /**** Status Bar */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.toolbar));

        /**** ToolBar*/
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(true);
        toolbar.setBackgroundColor(getColor(R.color.trasparent_toolbar));
    }

}
