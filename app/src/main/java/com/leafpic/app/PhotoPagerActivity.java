package com.leafpic.app;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.MenuItem;
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
import com.leafpic.app.Fragments.ImageFragment;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.ColorPalette;
import com.leafpic.app.utils.Measure;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.Time;
import java.text.DateFormat;
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
        setContentView(R.layout.activity_pager);

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

           initUI();
           setupUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initUI(){
        SP = PreferenceManager.getDefaultSharedPreferences(PhotoPagerActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (ViewPager) findViewById(R.id.photos_pager);
        ActivityBackgorund = (RelativeLayout) findViewById(R.id.PhotoPager_Layout);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_arrow_back)
                .color(Color.WHITE)
                .sizeDp(18));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRecentApp(getString(R.string.app_name));
        setupSystemUI();

        new Handler().postDelayed(new Runnable() {
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
        adapter = new MediaPagerAdapter(getSupportFragmentManager(), photos.medias);

        adapter.setVideoOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mpdIntent = new Intent(PhotoPagerActivity.this, PlayerActivity.class)
                        .setData(photos.getCurrentPhoto().getUri());
                startActivity(mpdIntent);
            }
        });

        getSupportActionBar().setTitle((photos.getCurrentPhotoIndex() + 1) + " " + getString(R.string.of) + " " + photos.medias.size());

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(photos.getCurrentPhotoIndex());
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                photos.setCurrentPhotoIndex(position);
                toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + photos.medias.size());
                if (!fullscreenmode) new Handler().postDelayed(new Runnable() {
                    public void run() {
                        hideSystemUI();
                    }
                }, 1200);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    public void setupUI(){

        /**** Theme ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(isApplyThemeOnImgAct()
                ? (ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()))
                : (ContextCompat.getColor(getApplicationContext(),
                isDarkTheme()
                        ? R.color.transparent_dark_gray
                        : R.color.transparent_white_gray)));

        ActivityBackgorund.setBackgroundColor(getBackgroundColor());

        if(!isDarkTheme())
            toolbar.setPopupTheme(R.style.LightActionBarMenu);

        setStatusBarColor();
        setNavBarColor();


        /**** Settings ****/

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
    }


    @Override
    public void onResume() {
        super.onResume();
        setupUI();
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
                case PickAlbumActivity.MOVE_TO_ACTION:
                    int asd = Integer.valueOf(b.getString("photos_indexes"));
                    if (asd >= 0 && asd < photos.medias.size()) {
                        photos.medias.remove(asd);
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((photos.getCurrentPhotoIndex() + 1) + this.getString(R.string.of) + photos.medias.size());
                        invalidateOptionsMenu();
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri imageUri = UCrop.getOutput(data);
                    if (imageUri != null && imageUri.getScheme().equals("file")) {
                        try {
                            copyFileToDownloads(imageUri);
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

            case R.id.rotate_180:
                ((ImageFragment) adapter.getItem(photos.getCurrentPhotoIndex())).rotatePicture(180);
                break;

            case R.id.rotate_right_90:
                ((ImageFragment) adapter.getItem(photos.getCurrentPhotoIndex())).rotatePicture(90);
                break;

            case R.id.rotate_left_90:
                ((ImageFragment) adapter.getItem(photos.getCurrentPhotoIndex())).rotatePicture(-90);
                break;

            case R.id.moveAction:
                Intent int1 = new Intent(getApplicationContext(), PickAlbumActivity.class);
                int1.putExtra("selected_photos", photos.getCurrentPhoto().Path);
                int1.putExtra("request_code", PickAlbumActivity.MOVE_TO_ACTION);
                int1.putExtra("photos_indexes", String.valueOf(photos.getCurrentPhotoIndex()));
                startActivityForResult(int1, PickAlbumActivity.MOVE_TO_ACTION);
                break;
            case R.id.copyAction:
                Intent int2 = new Intent(getApplicationContext(), PickAlbumActivity.class);
                int2.putExtra("selected_photos", photos.getCurrentPhoto().Path);
                int2.putExtra("request_code", PickAlbumActivity.COPY_TO_ACTION);
                startActivityForResult(int2, PickAlbumActivity.COPY_TO_ACTION);
                break;

            case R.id.shareButton:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(photos.medias.get(mViewPager.getCurrentItem()).MIME);
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + photos.medias.get(mViewPager.getCurrentItem()).Path));
                startActivity(Intent.createChooser(share, getString(R.string.send_to)));
                return true;

            case R.id.edit_photo:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(photos.getCurrentPhoto().Path));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(PhotoPagerActivity.this);
                break;

            case R.id.useAsIntent:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(
                        Uri.parse("file://" + photos.medias.get(mViewPager.getCurrentItem()).Path),
                        photos.medias.get(mViewPager.getCurrentItem()).MIME);
                startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
                return true;

            case R.id.open_with:
                Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                intentopenWith.setDataAndType(
                        Uri.parse("file://" + photos.medias.get(mViewPager.getCurrentItem()).Path),
                        photos.medias.get(mViewPager.getCurrentItem()).MIME);
                startActivity(Intent.createChooser(intentopenWith, getString(R.string.open_with)));
                break;

            case R.id.deletePhoto:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(new ContextThemeWrapper(PhotoPagerActivity.this, android.R.style.Theme_Dialog));
                builder1.setMessage(R.string.delete_album_message);
                builder1.setPositiveButton(this.getString(R.string.delete_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        photos.deleteCurrentPhoto();
                        if (photos.medias.size() == 0)
                            startActivity(new Intent(PhotoPagerActivity.this, AlbumsActivity.class));
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((mViewPager.getCurrentItem()+1) + getString(R.string.of) + photos.medias.size());

                    }
                });
                builder1.setNegativeButton(this.getString(R.string.cancel_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder1.show();
                break;

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
                title.setText(this.getString(R.string.rename_photo_action));
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
                RenameDialog.setNeutralButton(this.getString(R.string.cancel_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                RenameDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StringUtils.getPhotoPathRenamed(photos.getCurrentPhoto().Path, txt_edit.getText().toString());
                        if (txt_edit.length() != 0)
                            photos.renamePhoto(photos.getCurrentPhoto().Path, StringUtils.getPhotoRenamed(photos.getCurrentPhoto().Path, txt_edit.getText().toString()));
                        else
                            StringUtils.showToast(getApplicationContext(), PhotoPagerActivity.this.getString(R.string.insert_a_name));
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
                DateFormat as = SimpleDateFormat.getDateTimeInstance();
                String date = as.format(new Time(f.DateTaken));

                /****** BEAUTIFUL DIALOG ****/
                final AlertDialog.Builder DetailsDialog = new AlertDialog.Builder(PhotoPagerActivity.this,
                        isDarkTheme() ? R.style.AlertDialog_Dark : R.style.AlertDialog_Light);

                final View Details_DialogLayout = getLayoutInflater().inflate(R.layout.photo_detail_dialog, null);

                final TextView Size = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Size);
                final TextView Type = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Type);
                final TextView Resolution = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Resolution);
                final TextView Data = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Date);
                final TextView Path = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Path);
                final ImageView PhotoDetailsPreview = (ImageView) Details_DialogLayout.findViewById(R.id.photo_details_preview);
                final TextView txtSize = (TextView) Details_DialogLayout.findViewById(R.id.Size);
                final TextView txtType = (TextView) Details_DialogLayout.findViewById(R.id.Type);
                final TextView txtResolution = (TextView) Details_DialogLayout.findViewById(R.id.Resolution);
                final TextView txtData = (TextView) Details_DialogLayout.findViewById(R.id.Date);
                final TextView txtPath = (TextView) Details_DialogLayout.findViewById(R.id.Path);

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

                int color = ContextCompat.getColor(getApplicationContext(),
                        !isDarkTheme()
                        ? R.color.cp_TextLight
                        : R.color.cp_TextDark);

                txtData.setTextColor(color);
                txtPath.setTextColor(color);
                txtResolution.setTextColor(color);
                txtType.setTextColor(color);
                txtSize.setTextColor(color);
                Data.setTextColor(color);
                Path.setTextColor(color);
                Resolution.setTextColor(color);
                Type.setTextColor(color);
                Size.setTextColor(color);

                CardView cv = (CardView) Details_DialogLayout.findViewById(R.id.photo_details_card);
                cv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                        !isDarkTheme()
                        ? R.color.cp_PrimaryLight
                        : R.color.cp_PrimaryDark));

                DetailsDialog.setView(Details_DialogLayout);
                DetailsDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
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


    private void updateBrightness(float level) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = level;
        getWindow().setAttributes(lp);
    }

    public UCrop.Options getUcropOptions() {

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);
        options.setActiveWidgetColor(getAccentColor());
        options.setToolbarColor(getPrimaryColor());
        options.setStatusBarColor
                (isTraslucentStatusBar() ? ColorPalette.getOscuredColor(getPrimaryColor()) : getPrimaryColor());
        options.setCropFrameColor(getAccentColor());

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
        toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
                .setDuration(0).start();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showSystemUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
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
}

