package com.leafpic.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.leafpic.app.Adapters.PhotosPagerAdapter;
import com.leafpic.app.Animations.DepthPageTransformer;
import com.leafpic.app.Base.HandlingPhotos;
import com.leafpic.app.Base.Photo;
import com.leafpic.app.utils.StringUtils;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotoActivity extends AppCompatActivity {

    PhotosPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;
    HandlingPhotos photos;
    Toolbar toolbar;
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
            mCustomPagerAdapter = new PhotosPagerAdapter(this, photos.photos);
            mCustomPagerAdapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSystemUI();
                }
            });
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mCustomPagerAdapter);
            mViewPager.setCurrentItem(photos.getCurrentPhotoIndex());

            Photo f = photos.getCurrentPhoto();

            String[] projection = new String[]{
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE
            };

            Log.wtf("asdasdasd", f.Path);
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Media.DATA + " = ?",
                    new String[]{f.Path}, "");

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
                StringUtils.showToast(getApplicationContext(), cursor.getString(columnIndex));
            }
            cursor.close();

            mViewPager.setPageTransformer(true, new DepthPageTransformer());
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {photos.setCurrentPhotoIndex(position);}

                @Override
                public void onPageScrollStateChanged(int state) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        //DA FIXXARE
        hideSystemUI();
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
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(StringUtils.getMimeType(file_path));
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file_path));
                startActivity(Intent.createChooser(share, "Share Image"));
                return true;

            case R.id.deletePhoto:
                new MaterialDialog.Builder(this)
                        .content(R.string.delete_photo_message)
                        .positiveText("DELETE")
                        .negativeText("CANCEL")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int index = mViewPager.getCurrentItem();
                                mViewPager.removeView(mViewPager.getChildAt(index));
                                //TODO improve delete single photo
                                photos.deleteCurrentPhoto();
                                mCustomPagerAdapter.notifyDataSetChanged();
                                mViewPager.destroyDrawingCache();
                                mViewPager.setCurrentItem(index + 1);
                            }
                        })
                        .show();
                return true;

            case R.id.useAsIntent:
                String file_path_use_as = photos.photos.get(mViewPager.getCurrentItem()).Path;
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(Uri.parse("file://" + file_path_use_as), "image/*");
                intent.putExtra("jpg", StringUtils.getMimeType(file_path_use_as));
                startActivity(Intent.createChooser(intent, "Use As"));
                return true;

            case R.id.rotateSX:
                return true;

            case R.id.rotateDX:
                return true;

            case R.id.rotate180:
                return true;

            case R.id.renamePhoto:
                /*
                new MaterialDialog.Builder(this)
                        .title("Rename Photo")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, StringUtils.getPhotoNamebyPath(photos.getCurrentPhoto().Path), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                    albums.renameAlbum(photos.FolderPath, input.toString());
                                //onBackPressed();
                                    //finish();

                                StringUtils.showToast(getApplicationContext(), "I have to fix this!");
                            }
                        }).show();
                */
                break;
            case R.id.Modify:
                break;
            case R.id.details:
                /****DATA****/
                Calendar cl = Calendar.getInstance();
                cl.setTimeInMillis(Long.parseLong(StringUtils.getPhotoNamebyPath(photos.getCurrentPhoto().DateTaken)));  //here your time in miliseconds
                String date = "", size = "", resolution = "";

                SimpleDateFormat s = new SimpleDateFormat("dd/mm/yyyy HH:MM");// //new DateFormat();
                date = s.format(new Time(Long.valueOf(photos.getCurrentPhoto().DateTaken)));

                Photo f = photos.getCurrentPhoto();

                String[] projection = new String[]{
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.HEIGHT,
                        MediaStore.Images.Media.WIDTH
                };

                Cursor cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        MediaStore.Images.Media.DATA + " = ?",
                        new String[]{f.Path}, "");

                if (cursor.moveToFirst()) {
                    size = StringUtils.humanReadableByteCount(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)), true);
                    resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                    resolution += "x" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
                }
                cursor.close();

                /**GET COLOR**/
                /*

                SharedPreferences SP;
                SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String SColor = SP.getString("PrefColor", "#03A9F4");
                int color = Color.parseColor(SColor);
                //Html.fromHtml("<b><font color='" + SColor + "'>" + "Dimensionedddd: " + "</font></b>" + "Ddddda Implementare");
                */
                /**DIALOG**/
                new MaterialDialog.Builder(this)
                        .title("Photo Details")
                    //.titleColor(color)
                        .content("Album: " + StringUtils.getPhotoNamebyPath(photos.FolderPath)
                                + "\nName: " + StringUtils.getPhotoNamebyPath(photos.getCurrentPhoto().Path)
                                + "\nDimensione: " + size
                                + "\nRisoluzione: " + resolution
                                + "\nFormato: " + photos.getCurrentPhoto().MIME
                                + "\nData: " + date)
                        .positiveText("OK")
                        .show();
                break;

            case R.id.setting:
                Intent intent2= new Intent(PhotoActivity.this, SettingsActivity.class);
                startActivity(intent2);
                break;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                //return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void initUiTweaks() {
        /**** ToolBar ********/
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setBackgroundColor(getColor(R.color.transparent_gray));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /**** Status Bar *****/
        getWindow().setStatusBarColor(getColor(R.color.transparent_gray));
        /**** Navigation Bar */
        getWindow().setNavigationBarColor(getColor(R.color.transparent_gray));
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                hideSystemUI();
            }
        }, 150);
    }

    private void toggleSystemUI() {
        if (fullscreenmode)
            showSystemUI();
        else hideSystemUI();
    }

    private void hideSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator())
                        .start();
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);

                fullscreenmode = true;
            }
        });
    }

    private void showSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                toolbar.animate().translationY(getStatusBarHeight()).setInterpolator(new DecelerateInterpolator())
                        .start();
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                fullscreenmode = false;
            }
        });
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
