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
import com.leafpic.app.Animations.DepthPageTransformer;
import com.leafpic.app.utils.string;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotoActivity extends AppCompatActivity {


    HandlingPhotos photos;

    Toolbar toolbar;
    PhotosPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;
    boolean fullscreenmode;


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
                    toggleSystemUI();
                    return true;
                }
            });

            mCustomPagerAdapter = new PhotosPagerAdapter(this, photos.photos);

            mCustomPagerAdapter.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mCustomPagerAdapter);
            mViewPager.setCurrentItem(photos.getCurrentPhotoIndex());
            mViewPager.setPageTransformer(true, new DepthPageTransformer());
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    photos.setCurrentPhotoIndex(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

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

                mCustomPagerAdapter.destroyItem(mViewPager,
                        photos.getCurrentPhotoIndex(),
                        mViewPager.getFocusedChild());
                mViewPager.removeView(mViewPager.getFocusedChild());

                photos.deleteCurrentPhoto();
                mCustomPagerAdapter.notifyDataSetChanged();
                return true;
            case R.id.rotatePhoto:

                return true;

            case R.id.useAsIntent:

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void initUiTweaks() {

        /**** ToolBar*/
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setBackgroundColor(getColor(android.R.color.transparent));
        hideSystemUI();// TODO hide navigation bar [PORCODIO]
        //toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new  AccelerateInterpolator()).start();
        // getSupportActionBar().hide();

    }

    private void toggleSystemUI() {
        if (fullscreenmode)
            showSystemUI();
        else hideSystemUI();

    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.

        //getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();

        fullscreenmode = true;
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        toolbar.animate().translationY(getStatusBarHeight()).setInterpolator(new DecelerateInterpolator()).start();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //getSupportActionBar().show();
        //getWindow().getst
        fullscreenmode = false;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
