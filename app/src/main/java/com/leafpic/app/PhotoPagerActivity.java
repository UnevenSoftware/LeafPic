package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.leafpic.app.Adapters.MediaPagerAdapter;
import com.leafpic.app.Animations.DepthPageTransformer;
import com.leafpic.app.Base.HandlingPhotos;
import com.leafpic.app.Base.Media;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.StringUtils;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * Created by dnld on 18/02/16.
 */
public class PhotoPagerActivity extends ThemedActivity {

    ViewPager mViewPager;
    HandlingPhotos photos;
    MediaPagerAdapter adapter;
    SharedPreferences SP;
    RelativeLayout ActivityBackgorund;

    Toolbar toolbar;
    boolean fullscreenmode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ActivityBackgorund = (RelativeLayout) findViewById(R.id.PhotoPager_Layout);

        initUiTweaks();

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleSystemUI();
                return true;
            }
        });

        try {
            if (getIntent().getData() != null) { /*** Call from android.View */
                photos = new HandlingPhotos(getApplicationContext(), getIntent().getData().getPath());
                photos.setCurrentPhoto(getIntent().getData().getPath());

            } else if (getIntent().getExtras() != null) { /*** Call from PhotosActivity */
                Bundle data = getIntent().getExtras();
                photos = data.getParcelable("album");
                if (photos != null)
                    photos.setContext(getApplicationContext());
            }

            mViewPager = (ViewPager) findViewById(R.id.photos_pager);
            adapter = new MediaPagerAdapter(getSupportFragmentManager(), photos.medias);
            adapter.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(photos.getCurrentPhotoIndex());
            getSupportActionBar().setTitle((photos.getCurrentPhotoIndex() + 1) + " of " + photos.medias.size());
            mViewPager.setPageTransformer(true, new DepthPageTransformer());
            mViewPager.setOffscreenPageLimit(2);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    photos.setCurrentPhotoIndex(position);
                    toolbar.setTitle((position + 1) + " of " + photos.medias.size());
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
    public void onResume() {
        super.onResume();
        initUiTweaks();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(getApplicationContext()).clearMemory();
        Glide.get(getApplicationContext()).trimMemory(TRIM_MEMORY_COMPLETE);
        System.gc();
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
                case SelectAlbumActivity.MOVE_TO_ACTION:
                    int asd = Integer.valueOf(b.getString("photos_indexes"));
                    if (asd >= 0 && asd < photos.medias.size()) {
                        photos.medias.remove(asd);
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((photos.getCurrentPhotoIndex() + 1) + " of " + photos.medias.size());
                        invalidateOptionsMenu();
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri imageUri = UCrop.getOutput(data);
                    if (imageUri != null && imageUri.getScheme().equals("file")) {
                        try {
                            copyFileToDownloads(imageUri);
                            //getFragmentManager().invalidateOptionsMenu();
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("ERROS - uCrop", imageUri.toString(), e);
                        }
                    } else
                        StringUtils.showToast(getApplicationContext(), "errori random");
                    break;

                default:
                    break;
            }
        }
    }

    private void copyFileToDownloads(Uri croppedFileUri) throws Exception {
        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(new File(photos.getCurrentPhoto().Path));
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
        photos.scanFile(new String[]{photos.getCurrentPhoto().Path});
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
                int1.putExtra("photos_indexes", String.valueOf(photos.getCurrentPhotoIndex()));
                startActivityForResult(int1, SelectAlbumActivity.MOVE_TO_ACTION);
                break;
            case R.id.copyAction:
                Intent int2 = new Intent(getApplicationContext(), SelectAlbumActivity.class);
                int2.putExtra("selected_photos", photos.getCurrentPhoto().Path);
                int2.putExtra("request_code", SelectAlbumActivity.COPY_TO_ACTION);
                startActivityForResult(int2, SelectAlbumActivity.COPY_TO_ACTION);
                break;

            case R.id.shareButton:
                String file_path = photos.medias.get(mViewPager.getCurrentItem()).Path;
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
                        photos.deleteCurrentPhoto();
                        if (photos.medias.size() == 0)
                            startActivity(new Intent(PhotoPagerActivity.this, AlbumsActivity.class));
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle(mViewPager.getCurrentItem() + 1 + " of " + photos.medias.size());

                    }
                });
                builder1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder1.show();
                return true;

            case R.id.edit_photo:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(photos.getCurrentPhoto().Path));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(PhotoPagerActivity.this);
                break;

            case R.id.useAsIntent:
                String file_path_use_as = photos.medias.get(mViewPager.getCurrentItem()).Path;
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(Uri.parse("file://" + file_path_use_as), "image/*");
                intent.putExtra("jpg", StringUtils.getMimeType(file_path_use_as));
                startActivity(Intent.createChooser(intent, "Use As"));
                return true;


            case R.id.renamePhoto:
                final AlertDialog.Builder RenameDialog;
                if (isDarkTheme())
                    RenameDialog = new AlertDialog.Builder(PhotoPagerActivity.this, R.style.AlertDialog_Dark);
                else
                    RenameDialog = new AlertDialog.Builder(PhotoPagerActivity.this, R.style.AlertDialog_Light);

                final View Rename_dialogLayout = getLayoutInflater().inflate(R.layout.rename_dialog, null);
                final TextView title = (TextView) Rename_dialogLayout.findViewById(R.id.rename_title);
                final EditText txt_edit = (EditText) Rename_dialogLayout.findViewById(R.id.dialog_txt);
                CardView cv_Rename_Dialog = (CardView) Rename_dialogLayout.findViewById(R.id.rename_card);

                title.setBackgroundColor(getPrimaryColor());
                title.setText("Rename Photo");
                txt_edit.setText(StringUtils.getPhotoNamebyPath(photos.getCurrentPhoto().Path));
                txt_edit.selectAll();
                txt_edit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                if (!isDarkTheme()) {
                    cv_Rename_Dialog.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryLight));
                    txt_edit.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    txt_edit.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    txt_edit.getBackground().mutate().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight), PorterDuff.Mode.SRC_ATOP);
                } else {
                    cv_Rename_Dialog.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryDark));
                    txt_edit.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextDark));
                    txt_edit.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextDark));
                    txt_edit.getBackground().mutate().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextDark), PorterDuff.Mode.SRC_ATOP);
                }

                RenameDialog.setView(Rename_dialogLayout);
                RenameDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                RenameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StringUtils.getPhotoPathRenamed(photos.getCurrentPhoto().Path, txt_edit.getText().toString());
                        if (txt_edit.length() != 0)
                            photos.renamePhoto(photos.getCurrentPhoto().Path, StringUtils.getPhotoRenamed(photos.getCurrentPhoto().Path, txt_edit.getText().toString()));
                        else
                            StringUtils.showToast(getApplicationContext(), "Insert Something!");
                    }
                });
                RenameDialog.show();
                break;

            case R.id.advanced_photo_edit:
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(Uri.parse("file://" + photos.getCurrentPhoto().Path), photos.getCurrentPhoto().MIME);
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, "Edit with"));

                break;
            case R.id.details:
                /****DATA****/
                Media f = photos.getCurrentPhoto();
                String date = "";
                SimpleDateFormat s = new SimpleDateFormat("dd/mm/yyyy HH:MM");
                date = s.format(new Time(Long.valueOf(f.DateTaken)));


                /****** BEAUTIFUL DIALOG ****/
                final AlertDialog.Builder DetailsDialog;
                if (isDarkTheme())
                    DetailsDialog = new AlertDialog.Builder(PhotoPagerActivity.this, R.style.AlertDialog_Dark);
                else
                    DetailsDialog = new AlertDialog.Builder(PhotoPagerActivity.this, R.style.AlertDialog_Light);


                final View Details_DialogLayout = getLayoutInflater().inflate(R.layout.photo_detail_dialog, null);
                //OBJECT INSIDE
                //WRITE
                final TextView Size = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Size);
                final TextView Type = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Type);
                final TextView Resolution = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Resolution);
                final TextView Data = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Date);
                final TextView Path = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Path);
                final ImageView PhotoDetailsPreview = (ImageView) Details_DialogLayout.findViewById(R.id.photo_details_preview);
                CardView cv = (CardView) Details_DialogLayout.findViewById(R.id.photo_details_card);
                //READ
                final TextView txtSize = (TextView) Details_DialogLayout.findViewById(R.id.Size);
                final TextView txtType = (TextView) Details_DialogLayout.findViewById(R.id.Type);
                final TextView txtResolution = (TextView) Details_DialogLayout.findViewById(R.id.Resolution);
                final TextView txtData = (TextView) Details_DialogLayout.findViewById(R.id.Date);
                final TextView txtPath = (TextView) Details_DialogLayout.findViewById(R.id.Path);

                //b PhotoDetailsPreview.setImageURI(medias.getCurrentPhotoIndex());
                Glide.with(this)
                        .load(photos.getCurrentPhoto().Path)
                        .asBitmap()
                        .centerCrop()
                        .priority(Priority.IMMEDIATE)
                        .placeholder(R.drawable.ic_empty)
                        .into(PhotoDetailsPreview);

                Size.setText(f.getHumanReadableSize());
                Resolution.setText(f.getResolution());
                Data.setText(date);
                Type.setText(photos.getCurrentPhoto().MIME);

                Path.setText(photos.getCurrentPhoto().Path);

                if (!isDarkTheme()) {
                    cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryLight));
                    //READ
                    txtData.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    txtPath.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    txtResolution.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    txtType.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    txtSize.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    //WRITE
                    Data.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    Path.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    Resolution.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    Type.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                    Size.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_TextLight));
                } else {
                    cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.cp_PrimaryDark));
                }

                DetailsDialog.setView(Details_DialogLayout);
                DetailsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                DetailsDialog.setNeutralButton("EDIT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                DetailsDialog.show();

                break;

            case R.id.setting:
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                break;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                //return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void initUiTweaks() {

        SP = PreferenceManager.getDefaultSharedPreferences(PhotoPagerActivity.this);
        ActivityBackgorund.setBackgroundColor(getBackgroundColor());
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /****SET THEME ****/
        //TOOLBAR
        toolbar.setBackgroundColor( isApplyThemeOnImgAct()
                ? (getTransparentColor(getPrimaryColor(), getTransparency()))
                : (ContextCompat.getColor(getApplicationContext(),
                isDarkTheme()
                        ? R.color.transparent_dark_gray
                        : R.color.transparent_white_gray )));
        //STATUS & NAV BARS
        setStatusBarColor();
        setNavBarColor();
        /****END THEME****/

        setupSystemUI();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setRecentApp(getString(R.string.app_name));

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                hideSystemUI();
            }
        }, 1500);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showSystemUI();
                        else hideSystemUI();
                    }
                });



        if (SP.getBoolean("set_max_luminosita", false))
            updateBrightness(1.0F);
        else try {
            float brightness = android.provider.Settings.System.getInt(
                    getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            brightness = brightness == 1.0F ? 255.0F : brightness;
            updateBrightness(brightness);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

      /*  Settings.System.putInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION,
                SP.getBoolean("set_picture_orientation", false) ? 1 : 0);*/
        /*if (SP.getBoolean("set_picture_orientation", false)){

        }
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if(SP.getBoolean("set_picture_orientation", false))
            sensorManager.registerListener(new SensorEventListener() {
                int orientation = -1;

                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.values[1] < 6.5 && event.values[1] > -6.5) {
                        if (orientation != 1) {
                            Log.d("Sensor", "Landscape");
                            WindowManager.LayoutParams lp = getWindow().getAttributes();
                            lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            getWindow().setAttributes(lp);
                        }
                        orientation = 1;
                    } else {
                        if (orientation != 0) {
                            Log.d("Sensor", "Portrait");
                            WindowManager.LayoutParams lp = getWindow().getAttributes();
                            lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            getWindow().setAttributes(lp);
                        }
                        orientation = 0;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub

                }
            }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    */
    }
    //SensorManager sensorManager;

    private void updateBrightness(float level) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = level;
        getWindow().setAttributes(lp);
    }

    public UCrop.Options getUcropOptions() {

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //fullSizeOptions.set
        options.setCompressionQuality(90);
        options.setActiveWidgetColor(getAccentColor());
        options.setToolbarColor(getPrimaryColor());
        if(isTraslucentStatusBar())
            options.setStatusBarColor(getOscuredColor(getPrimaryColor()));
        else
            options.setStatusBarColor(getPrimaryColor());
        options.setCropFrameColor(getAccentColor());
        //fullSizeOptions.se

        // fullSizeOptions.setDimmedLayerColor(Color.CYAN);
       /*
        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧
        fullSizeOptions.setMaxScaleMultiplier(5);
        fullSizeOptions.setImageToCropBoundsAnimDuration(666);
        fullSizeOptions.setDimmedLayerColor(Color.CYAN);
        fullSizeOptions.setOvalDimmedLayer(true);
        fullSizeOptions.setShowCropFrame(false);
        fullSizeOptions.setCropGridStrokeWidth(20);
        fullSizeOptions.setCropGridColor(Color.GREEN);
        fullSizeOptions.setCropGridColumnCount(2);
        fullSizeOptions.setCropGridRowCount(1);
        // Color palette
        fullSizeOptions.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        fullSizeOptions.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        fullSizeOptions.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
		fullSizeOptions.setToolbarTitleTextColor(ContextCompat.getColor(this, R.color.your_color_res));
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
                        .setDuration(200).start();
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);

                fullscreenmode = true;
                ChangeBackGroundColor();
            }
        });
    }

    private void setupSystemUI() {
        toolbar.animate().translationY(getStatusBarHeight()).setInterpolator(new DecelerateInterpolator())
                .setDuration(0).start();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                toolbar.animate().translationY(getStatusBarHeight()).setInterpolator(new DecelerateInterpolator())
                        .setDuration(240).start();
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                fullscreenmode = false;
                ChangeBackGroundColor();
            }
        });
    }

    public void ChangeBackGroundColor(){
        int colorTo;
        int colorFrom;
        if(fullscreenmode) {
            colorFrom = getBackgroundColor();
            colorTo = (ContextCompat.getColor(PhotoPagerActivity.this ,R.color.md_black_1000));
        } else {
            colorFrom = (ContextCompat.getColor(PhotoPagerActivity.this ,R.color.md_black_1000));
            colorTo = getBackgroundColor();
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(240);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ActivityBackgorund.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
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

