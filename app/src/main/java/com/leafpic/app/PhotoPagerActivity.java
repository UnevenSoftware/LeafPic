package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.leafpic.app.Adapters.MediaPagerAdapter;
import com.leafpic.app.Animations.DepthPageTransformer;
import com.leafpic.app.Base.HandlingPhotos;
import com.leafpic.app.Base.Media;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
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
public class PhotoPagerActivity extends ThemedActivity{

    ViewPager mViewPager;
    HandlingPhotos photos;
    MediaPagerAdapter adapter;
    SharedPreferences SP;

    Toolbar toolbar;
    boolean fullscreenmode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        //startSystemUI();

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
            adapter = new MediaPagerAdapter(getSupportFragmentManager(),photos.medias);
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
    public void  onLowMemory(){
        super.onLowMemory();
        ImageLoader.getInstance().clearMemoryCache();
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
                        //Log.wtf("asdasdasdas", medias.medias.size() + "");
                        //medias.removePhoto(Integer.valueOf(asd));
                        // TODO remove photo moved from older album [porco dio]
                        //Log.wtf("asdasdasdas", medias.medias.size() + "");
                        //adapter.removeItemAt(Integer.valueOf(asd));
                        //mRecyclerView.removeViewAt(Integer.parseInt(asd));
                        //medias.medias.remove(Integer.parseInt(asd));
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
                            DiskCacheUtils.removeFromCache("file://"+photos.getCurrentPhoto().Path, ImageLoader.getInstance().getDiskCache());
                            adapter.notifyDataSetChanged();
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
                        StringUtils.showToast(getApplicationContext(),"doesn't work properly");
                        //int index = mViewPager.getCurrentItem();
                        //mViewPager.removeView(mViewPager.getChildAt(index));
                        //TODO improve delete single photo
                        //medias.deleteCurrentPhoto();
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

                //fullSizeOptions.setCompressionFormat(Bitmap.CompressFormat.PNG);
                //uCrop = basisConfig(uCrop);
                //uCrop = advancedConfig(uCrop);


                uCrop.start(PhotoPagerActivity.this);

               /* UCrop.of(Uri.parse("file/" + curPath), Uri.parse("file/" + curPath))
                        .start(PhotoPagerActivity.this);*/
                break;

            case R.id.useAsIntent:
                String file_path_use_as = photos.medias.get(mViewPager.getCurrentItem()).Path;
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(Uri.parse("file://" + file_path_use_as), "image/*");
                intent.putExtra("jpg", StringUtils.getMimeType(file_path_use_as));
                startActivity(Intent.createChooser(intent, "Use As"));
                return true;


            case R.id.renamePhoto:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Rename Photo");

                final EditText input = new EditText(this);
                input.setHint(StringUtils.getPhotoNamebyPath(photos.getCurrentPhoto().Path));

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setPadding(40,40,40,40);

                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String Type = photos.getCurrentPhoto().MIME;
                        Type = Type.replace("image/","");
                        photos.renamePhoto(photos.getCurrentPhoto().Path, input.getText().toString() +"."+ Type);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
               /* new MaterialDialog.Builder(this)
                        .title("Rename Media")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, StringUtils.getPhotoNamebyPath(medias.getCurrentPhoto().Path), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                medias.renamePhoto(
                                        medias.getCurrentPhoto().Path,
                                        input + StringUtils.getPhotoExtensionbyPath(medias.getCurrentPhoto().Path));
                            }
                        }).show();*/

                break;
            case R.id.details:
                /****DATA****/
                Media f = photos.getCurrentPhoto();
                String date = "";
                SimpleDateFormat s = new SimpleDateFormat("dd/mm/yyyy HH:MM");
                date = s.format(new Time(Long.valueOf(f.DateTaken)));



                /****** BEAUTIFUL DIALOG ****/
                final AlertDialog.Builder DetailsDialog;
                SP = PreferenceManager.getDefaultSharedPreferences(PhotoPagerActivity.this);
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
                }
                else {
                    cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.cp_PrimaryDark));
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
                Intent intent2= new Intent(getApplicationContext(), SettingsActivity.class);

                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

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


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent_gray));
        setupSystemUI();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent_gray));

        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent_gray));

        setRecentApp(getString(R.string.app_name));

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                hideSystemUI();
            }
        }, 1500);


    }

    public UCrop.Options getUcropOptions(){

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //fullSizeOptions.set
        options.setCompressionQuality(90);
        options.setActiveWidgetColor(getAccentColor());
        options.setToolbarColor(getPrimaryColor());
        options.setStatusBarColor(getPrimaryColor());
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
            }
        });
    }

    private void setupSystemUI(){
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

