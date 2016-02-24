package com.leafpic.app;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.leafpic.app.Adapters.MediaPagerAdapter;
import com.leafpic.app.Animations.DepthPageTransformer;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Base.HandlingPhotos;
import com.leafpic.app.Base.Photo;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.StringUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by dnld on 18/02/16.
 */
public class PhotoPagerActivity extends ThemedActivity{

    ViewPager mViewPager;
    HandlingPhotos photos;
    MediaPagerAdapter adapter;

    Toolbar toolbar;
    boolean fullscreenmode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        initUiTweaks();

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleSystemUI();
                return true;
            }
        });

        try {

            Bundle data = getIntent().getExtras();
            photos = data.getParcelable("album");
            if(photos!=null)
                photos.setContext(getApplicationContext());

            mViewPager = (ViewPager) findViewById(R.id.photos_pager);
            adapter = new MediaPagerAdapter(getSupportFragmentManager(),photos.photos);
            adapter.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(photos.getCurrentPhotoIndex());
            mViewPager.setPageTransformer(true, new DepthPageTransformer());
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    photos.setCurrentPhotoIndex(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

        }catch (Exception e){e.printStackTrace();}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null && resultCode == RESULT_OK) {
            final Bundle b = data.getExtras();
            switch (requestCode) {
                case SelectAlbumActivity.COPY_TO_ACTION:
                    StringUtils.showToast(getApplicationContext(), "copied ok");
                break;
                case SelectAlbumActivity.MOVE_TO_ACTION:
                    String asd = b.getString("photos_indexes");
                    if (asd != null) {
                        StringUtils.showToast(getApplicationContext(), "moved ok");
                        //Log.wtf("asdasdasdas", photos.photos.size() + "");
                        //photos.removePhoto(Integer.valueOf(asd));
                        // TODO remove photo moved from older album [porco dio]
                        //Log.wtf("asdasdasdas", photos.photos.size() + "");
                        //adapter.removeItemAt(Integer.valueOf(asd));
                        //mRecyclerView.removeViewAt(Integer.parseInt(asd));
                        //photos.photos.remove(Integer.parseInt(asd));
                        //mRecyclerView.removeViewAt(Integer.valueOf(asd));
                        //adapter.notifyItemRemoved(Integer.parseInt(asd));
                        //adapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }
                break;
                case UCrop.REQUEST_CROP:
                    final Uri imageUri = UCrop.getOutput(data);
                    if (imageUri != null && imageUri.getScheme().equals("file")) {
                        try {
                            copyFileToDownloads(imageUri);
                            mViewPager.getFocusedChild().invalidate();
                        } catch (Exception e) {
                            Log.e("ERROS - uCrop", imageUri.toString(), e);
                        }
                    } else
                        StringUtils.showToast(getApplicationContext(),"errori random");
                    break;

                default:
                    break;
            }
        }
    }

    private void copyFileToDownloads(Uri croppedFileUri) throws Exception {
        StringUtils.showToast(getApplicationContext(), croppedFileUri.getPath());
        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(new File(photos.getCurrentPhoto().Path));
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
        photos.scanFile(new String[]{photos.getCurrentPhoto().Path});
        StringUtils.showToast(getApplicationContext(), "ok");

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.moveAction:
                Intent int1 = new Intent(getApplicationContext(), SelectAlbumActivity.class);
                int1.putExtra("selected_photos", photos.getCurrentPhoto().Path);
                int1.putExtra("request_code", SelectAlbumActivity.MOVE_TO_ACTION);
                int1.putExtra("photos_indexes", photos.getCurrentPhotoIndex());
                startActivityForResult(int1, SelectAlbumActivity.MOVE_TO_ACTION);
                break;
            case R.id.copyAction:
                Intent int2 = new Intent(getApplicationContext(), SelectAlbumActivity.class);
                int2.putExtra("selected_photos", photos.getCurrentPhoto().Path);
                int2.putExtra("request_code", SelectAlbumActivity.COPY_TO_ACTION);
                startActivityForResult(int2, SelectAlbumActivity.COPY_TO_ACTION);
                break;

            case R.id.shareButton:
                String file_path = photos.photos.get(mViewPager.getCurrentItem()).Path;
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(StringUtils.getMimeType(file_path));
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file_path));
                startActivity(Intent.createChooser(share, "Share Image"));
                return true;

            case R.id.deletePhoto:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(new ContextThemeWrapper(PhotoPagerActivity.this, android.R.style.Theme_Dialog));
                builder1.setMessage(R.string.delete_album_message);
                builder1.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StringUtils.showToast(getApplicationContext(),"doesn't work properly");
                        //int index = mViewPager.getCurrentItem();
                        //mViewPager.removeView(mViewPager.getChildAt(index));
                        //TODO improve delete single photo
                        //photos.deleteCurrentPhoto();
                        //adapter.notifyDataSetChanged();
                        //mViewPager.destroyDrawingCache();
                        //mViewPager.setCurrentItem(index + 1);
                    }
                });
                builder1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {}});
                builder1.show();

                return true;
            case R.id.edit_photo:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(photos.getCurrentPhoto().Path));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                //uCrop = uCrop.useSourceImageAspectRatio();
                uCrop.withOptions(getUcropOptions());

                //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                //uCrop = basisConfig(uCrop);
                //uCrop = advancedConfig(uCrop);


                uCrop.start(PhotoPagerActivity.this);

               /* UCrop.of(Uri.parse("file/" + curPath), Uri.parse("file/" + curPath))
                        .start(PhotoPagerActivity.this);*/
                break;

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

               /* new MaterialDialog.Builder(this)
                        .title("Rename Photo")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, StringUtils.getPhotoNamebyPath(photos.getCurrentPhoto().Path), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                photos.renamePhoto(
                                        photos.getCurrentPhoto().Path,
                                        input + StringUtils.getPhotoExtensionbyPath(photos.getCurrentPhoto().Path));
                            }
                        }).show();*/

                break;
            case R.id.details:
                /****DATA****/
                Photo f = photos.getCurrentPhoto();
                String date = "", size = "", resolution = "";
                SimpleDateFormat s = new SimpleDateFormat("dd/mm/yyyy HH:MM");
                date = s.format(new Time(Long.valueOf(f.DateTaken)));

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

                /**DIALOG**/
               /* new MaterialDialog.Builder(this)
                        .title("Photo Details")
                        .content("Path: \t" + photos.getCurrentPhoto().Path
                                + "\nSize: \t" + size
                                + "\nResolution: \t" + resolution
                                + "\nType: \t" + photos.getCurrentPhoto().MIME
                                + "\nDate: \t" + date)
                        .positiveText("DONE")
                        .show();*/
                break;

            case R.id.setting:
                Intent intent2= new Intent(getApplicationContext(), SettingsActivity.class);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getColor(R.color.transparent_gray));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /**** Status Bar *****/
        getWindow().setStatusBarColor(getColor(R.color.transparent_gray));
        /**** Navigation Bar */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getColor(R.color.transparent_gray));
            BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), drawable.getBitmap(), getPrimaryColor()));
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                hideSystemUI();
            }
        }, 1000);


    }

    public UCrop.Options getUcropOptions(){

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.set
        options.setCompressionQuality(90);
        options.setActiveWidgetColor(getAccentColor());
        options.setToolbarColor(getPrimaryColor());
        options.setStatusBarColor(getPrimaryColor());
    //options.se

       // options.setDimmedLayerColor(Color.CYAN);
       /*
        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧
        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setOvalDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
		options.setToolbarTitleTextColor(ContextCompat.getColor(this, R.color.your_color_res));
       */

        return options;
    }

    public void toggleSystemUI() {
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

