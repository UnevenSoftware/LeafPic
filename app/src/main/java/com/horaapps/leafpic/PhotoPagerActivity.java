package com.horaapps.leafpic;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.horaapps.leafpic.Adapters.MediaPagerAdapter;
import com.horaapps.leafpic.Animations.DepthPageTransformer;
import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.Fragments.ImageFragment;
import com.horaapps.leafpic.Views.HackyViewPager;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.Measure;
import com.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by dnld on 18/02/16.
 */
public class PhotoPagerActivity extends ThemedActivity {

    private static final String ISLOCKED_ARG = "isLocked";
    public static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    HackyViewPager mViewPager;
    MediaPagerAdapter adapter;
    SharedPreferences SP;
    RelativeLayout ActivityBackgorund;
    Album album;
    Toolbar toolbar;
    boolean fullscreenmode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        SP = PreferenceManager.getDefaultSharedPreferences(PhotoPagerActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (HackyViewPager) findViewById(R.id.photos_pager);

        if (savedInstanceState != null)
            mViewPager.setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));
        try
        {
            if (getIntent().getAction().equals(ACTION_OPEN_ALBUM))
                album = ((MyApplication) getApplicationContext()).getCurrentAlbum();
            else if (getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null)
                album = new Album(getIntent().getData().getPath());
            else if (getIntent().getAction().equals(ACTION_REVIEW) && getIntent().getData() != null)
                album = new Album(getRealPathFromURI(this, getIntent().getData()));

            initUI();
            setupUI();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index;
            if (cursor != null) column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            else return null;
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally { if (cursor != null) cursor.close(); }
    }

    public void initUI() {

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
        setRecentApp(getString(R.string.app_name));
        setupSystemUI();

        /*
        new Handler().postDelayed(new Runnable() {
            public void run() {
                hideSystemUI();
            }
        }, 1500);
        */

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showSystemUI();
                        else hideSystemUI();
                    }
                });
        adapter = new MediaPagerAdapter(getSupportFragmentManager(), album.media);

        adapter.setVideoOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SP.getBoolean("set_internal_player", false)) {
                    Intent mpdIntent = new Intent(PhotoPagerActivity.this, PlayerActivity.class)
                            .setData(album.getCurrentMedia().getUri());
                    startActivity(mpdIntent);
                } else {
                    Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                    intentopenWith.setDataAndType(
                            album.media.get(mViewPager.getCurrentItem()).getUri(),
                            album.media.get(mViewPager.getCurrentItem()).getMIME());
                    startActivity(intentopenWith);
                }
            }
        });

        getSupportActionBar().setTitle((album.getCurrentMediaIndex() + 1) + " " + getString(R.string.of) + " " + album.media.size());

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(album.getCurrentMediaIndex());
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                album.setCurrentPhotoIndex(position);
                toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + album.media.size());
                if (!fullscreenmode) new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //hideSystemUI();
                    }
                }, 1200);
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Display aa = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (aa.getRotation() == Surface.ROTATION_90) {//1
            Configuration configuration = new Configuration();
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
            onConfigurationChanged(configuration);
        }

    }



    public void setupUI() {

        /**** Theme ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (!isApplyThemeOnImgAct())
            toolbar.setBackgroundColor(ColorPalette.getTransparentColor(getDefaultThemeToolbarColor3th(), 175));
        else
            toolbar.setBackgroundColor((ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency())));

        ActivityBackgorund = (RelativeLayout) findViewById(R.id.PhotoPager_Layout);
        ActivityBackgorund.setBackgroundColor(getBackgroundColor());

        switch (getBasicTheme()){
            case 1: toolbar.setPopupTheme(R.style.LightActionBarMenu);break;
            //case 2: toolbar.setPopupTheme(R.style.DarkActionBarMenu);break;
            //case 3: toolbar.setPopupTheme(R.style.AmoledDarkActionBarMenu);break;
            //default: toolbar.setPopupTheme(R.style.LightActionBarMenu);break;
        }

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

        if (SP.getBoolean("set_picture_orientation", false))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
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

        menu.findItem(R.id.deletePhoto).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        menu.findItem(R.id.shareButton).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.rotatePhoto).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_right));
        menu.findItem(R.id.rotate_right_90).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_right));
        menu.findItem(R.id.rotate_left_90).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_left));
        menu.findItem(R.id.rotate_180).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_replay));

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
           params.setMargins(0,0,Measure.getNavigationBarSize(PhotoPagerActivity.this).x,0);
        else
            params.setMargins(0,0,0,0);

        toolbar.setLayoutParams(params);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {

        menu.setGroupVisible(R.id.only_photos_otions, !album.getCurrentMedia().isVideo());
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
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
        FileOutputStream outStream = new FileOutputStream(new File(album.getCurrentMedia().getPath()));
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
        album.scanFile(getApplicationContext(), new String[]{album.getCurrentMedia().getPath()});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.rotate_180:
                ((ImageFragment) adapter.getRegisteredFragment(album.getCurrentMediaIndex())).rotatePicture(180);
                break;

            case R.id.rotate_right_90:
                ((ImageFragment) adapter.getRegisteredFragment(album.getCurrentMediaIndex())).rotatePicture(90);
                break;

            case R.id.rotate_left_90:
                ((ImageFragment) adapter.getRegisteredFragment(album.getCurrentMediaIndex())).rotatePicture(-90);
                break;

            case R.id.copyAction:
                final HandlingAlbums albums = ((MyApplication) getApplicationContext()).getAlbums();
                final SelectAlbumBottomSheet bottomSheetDialogFragment = new SelectAlbumBottomSheet();
                bottomSheetDialogFragment.setAlbumArrayList(albums.dispAlbums);
                bottomSheetDialogFragment.setTitle(getString(R.string.copy_to));
                bottomSheetDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = Integer.parseInt(v.findViewById(R.id.Bottom_Sheet_Title_Item).getTag().toString());
                        album.copyPhoto(getApplicationContext(), album.getCurrentMedia().getPath(), albums.getAlbum(index).getPath());
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                break;


            case R.id.shareButton:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(album.getCurrentMedia().getMIME());
                share.putExtra(Intent.EXTRA_STREAM, album.getCurrentMedia().getUri());
                startActivity(Intent.createChooser(share, getString(R.string.send_to)));
                return true;

            case R.id.edit_photo:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(album.getCurrentMedia().getPath()));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(PhotoPagerActivity.this);
                break;

            case R.id.useAsIntent:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(
                       album.getCurrentMedia().getUri(), album.getCurrentMedia().getMIME());
                startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
                return true;

            case R.id.open_with:
                Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                intentopenWith.setDataAndType(
                        album.getCurrentMedia().getUri(), album.getCurrentMedia().getMIME());
                startActivity(Intent.createChooser(intentopenWith, getString(R.string.open_with)));
                break;

            case R.id.deletePhoto:
                final AlertDialog.Builder DeleteDialog = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());

                final View Delete_dialogLayout = getLayoutInflater().inflate(R.layout.text_dialog, null);
                final TextView txt_Delete_title = (TextView) Delete_dialogLayout.findViewById(R.id.text_dialog_title);
                final TextView txt_Delete_message = (TextView) Delete_dialogLayout.findViewById(R.id.text_dialog_message);
                CardView cv_Delete_Dialog = (CardView) Delete_dialogLayout.findViewById(R.id.message_card);

                cv_Delete_Dialog.setBackgroundColor(getCardBackgroundColor());
                txt_Delete_title.setBackgroundColor(getPrimaryColor());
                txt_Delete_title.setText(getString(R.string.delete));
                txt_Delete_message.setText(R.string.delete_album_message);
                txt_Delete_message.setTextColor(getTextColor());
                DeleteDialog.setView(Delete_dialogLayout);

                DeleteDialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                DeleteDialog.setPositiveButton(this.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*album.deleteCurrentPhoto();
                        if (album.medias.size() == 0) {
                            startActivity(new Intent(PhotoPagerActivity.this, MainActivity.class));
                            finish();
                        }
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + album.medias.size());
                    */
                    }
                });
                DeleteDialog.show();
                break;

            case R.id.moveAction:
               /*
                final HandlingAlbums albumsa = ((MyApplication) getApplicationContext()).getAlbums();
                final SelectAlbumBottomSheet sheetMove = new SelectAlbumBottomSheet();
                sheetMove.setAlbumArrayList(albumsa.dispAlbums);
                sheetMove.setTitle(getString(R.string.move_to));
                sheetMove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = Integer.parseInt(v.findViewById(R.id.Bottom_Sheet_Title_Item).getTag().toString());
                        album.moveCurentPhoto(albumsa.getAlbum(index).getPath());
                        album.media.remove(album.getCurrentMediaIndex());
                        if (album.media.size() == 0) {
                            startActivity(new Intent(PhotoPagerActivity.this, MainActivity.class));
                            finish();
                        }
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + album.medias.size());
                        sheetMove.dismiss();
                    }
                });
                sheetMove.show(getSupportFragmentManager(), sheetMove.getTag());
                */
                break;

            case R.id.renamePhoto:
                final AlertDialog.Builder RenameDialog = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());

                final View Rename_dialogLayout = getLayoutInflater().inflate(R.layout.rename_dialog, null);
                final TextView title = (TextView) Rename_dialogLayout.findViewById(R.id.rename_title);
                final EditText txt_edit = (EditText) Rename_dialogLayout.findViewById(R.id.dialog_txt);
                CardView cv_Rename_Dialog = (CardView) Rename_dialogLayout.findViewById(R.id.rename_card);

                cv_Rename_Dialog.setBackgroundColor(getCardBackgroundColor());
                title.setBackgroundColor(getPrimaryColor());
                title.setText(getString(R.string.rename_photo_action));
                txt_edit.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
                txt_edit.setTextColor(getTextColor());
                txt_edit.setText(StringUtils.getPhotoNamebyPath(album.getCurrentMedia().getPath()));

                RenameDialog.setView(Rename_dialogLayout);
                RenameDialog.setNeutralButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                RenameDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /*if (txt_edit.length() != 0) {
                            try {
                                File from = new File(album.getCurrentPhoto().Path);
                                File to = new File(StringUtils.getPhotoPathRenamed(album.getCurrentPhoto().Path, txt_edit.getText().toString()));
                                if (from.renameTo(to)) {
                                    MediaScannerConnection.scanFile(
                                            getApplicationContext(),
                                            new String[]{to.getAbsolutePath()}, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                @Override
                                                public void onScanCompleted(String path, Uri uri) {
                                                    getContentResolver().delete(album.getCurrentPhoto().getUri(), null, null);
                                                    album.getCurrentPhoto().ID = StringUtils.getID(uri + "");
                                                    album.getCurrentPhoto().Path = path;
                                                }
                                            });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.nothing_changed));
                            */
                    }
                });
                RenameDialog.show();
                break;

            case R.id.advanced_photo_edit:
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(album.getCurrentMedia().getUri(), album.getCurrentMedia().getMIME());
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, "Edit with"));
                break;

            case R.id.details:
                /****DATA****/
                final Media f = album.getCurrentMedia();
                DateFormat as = SimpleDateFormat.getDateTimeInstance();
                String date = as.format(new Time(f.getDateModified()));

                /****** BEAUTIFUL DIALOG ****/
                final AlertDialog.Builder DetailsDialog = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());

                final View Details_DialogLayout = getLayoutInflater().inflate(R.layout.photo_detail_dialog, null);
                final TextView Size = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Size);
                final TextView Type = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Type);
                final TextView Resolution = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Resolution);
                final TextView Data = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Date);
                final TextView Path = (TextView) Details_DialogLayout.findViewById(R.id.Photo_Path);
                //final ImageView PhotoDetailsPreview = (ImageView) Details_DialogLayout.findViewById(R.id.photo_details_preview);
                final TextView txtTitle = (TextView) Details_DialogLayout.findViewById(R.id.media_details_title);
                final TextView txtSize = (TextView) Details_DialogLayout.findViewById(R.id.Size);
                final TextView txtType = (TextView) Details_DialogLayout.findViewById(R.id.Type);
                final TextView txtResolution = (TextView) Details_DialogLayout.findViewById(R.id.Resolution);
                final TextView txtData = (TextView) Details_DialogLayout.findViewById(R.id.Date);
                final TextView txtPath = (TextView) Details_DialogLayout.findViewById(R.id.Path);

                //EXIF
                final TextView txtDevice = (TextView) Details_DialogLayout.findViewById(R.id.Device);
                final TextView Device = (TextView) Details_DialogLayout.findViewById(R.id.Device_item);
                final TextView txtEXIF = (TextView) Details_DialogLayout.findViewById(R.id.EXIF);
                final TextView EXIF = (TextView) Details_DialogLayout.findViewById(R.id.EXIF_item);
                final TextView txtLocation = (TextView) Details_DialogLayout.findViewById(R.id.Location);
                final TextView Location = (TextView) Details_DialogLayout.findViewById(R.id.Location_item);
                //MAP
                final LinearLayout llMap = (LinearLayout) Details_DialogLayout.findViewById(R.id.ll_map);
                final ImageView imgMap = (ImageView) Details_DialogLayout.findViewById(R.id.img_Map);
                final LinearLayout llLocation =(LinearLayout) Details_DialogLayout.findViewById(R.id.ll_location);
                txtTitle.setBackgroundColor(getPrimaryColor());

                Size.setText(f.getHumanReadableSize());
                Resolution.setText(f.getResolution());
                Data.setText(date);
                Type.setText(f.getMIME());
                Path.setText(f.getPath());

                txtData.setTextColor(getTextColor());
                txtPath.setTextColor(getTextColor());
                txtResolution.setTextColor(getTextColor());
                txtType.setTextColor(getTextColor());
                txtSize.setTextColor(getTextColor());
                txtDevice.setTextColor(getTextColor());
                txtEXIF.setTextColor(getTextColor());
                txtLocation.setTextColor(getTextColor());

                Data.setTextColor(getSubTextColor());
                Path.setTextColor(getSubTextColor());
                Resolution.setTextColor(getSubTextColor());
                Type.setTextColor(getSubTextColor());
                Size.setTextColor(getSubTextColor());
                Device.setTextColor(getSubTextColor());
                EXIF.setTextColor(getSubTextColor());
                Location.setTextColor(getSubTextColor());

                final LinearLayout ll = (LinearLayout) Details_DialogLayout.findViewById(R.id.ll_detail_dialog_EXIF);

                try {
                    ExifInterface exif = new ExifInterface(f.getPath());
                    if (exif.getAttribute(ExifInterface.TAG_MAKE) != null) {
                        Device.setText(String.format("%s %s",
                                exif.getAttribute(ExifInterface.TAG_MAKE),
                                exif.getAttribute(ExifInterface.TAG_MODEL)));

                        EXIF.setText(String.format("f/%s ISO-%s %ss",
                                exif.getAttribute(ExifInterface.TAG_APERTURE),
                                exif.getAttribute(ExifInterface.TAG_ISO),
                                exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)));

                        final float[] output= new float[2];
                        exif.getLatLong(output);

                        if(output[0] != 0 && output[1] != 0) {
                            String url = "http://maps.google.com/maps/api/staticmap?center=" + output[0] + "," + output[1] + "&zoom=15&size="+400+"x"+400+"&scale=2&sensor=false&&markers=color:red%7Clabel:C%7C"+output[0]+","+output[1];
                            Glide.with(this)
                                    .load(url)
                                    .asBitmap()
                                    .centerCrop()
                                    .animate(R.anim.fade_in)
                                    .into(imgMap);
                            imgMap.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f", output[0], output[1]);
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                                }
                            });
                            Location.setText(String.format(Locale.getDefault(),"%f, %f",
                                    output[0], output[1]));
                            llLocation.setVisibility(View.VISIBLE);
                            llMap.setVisibility(View.VISIBLE);

                        }
                        ll.setVisibility(View.VISIBLE);
                    }
                }
                catch (IOException e){ e.printStackTrace(); }

                CardView cv = (CardView) Details_DialogLayout.findViewById(R.id.photo_details_card);
                cv.setCardBackgroundColor(getCardBackgroundColor());
                DetailsDialog.setView(Details_DialogLayout);

                DetailsDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                DetailsDialog.setNeutralButton(this.getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                        UCrop uCrop = UCrop.of(f.getUri(), mDestinationUri);
                        uCrop.withOptions(getUcropOptions());
                        uCrop.start(PhotoPagerActivity.this);
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
        options.setStatusBarColor(isTraslucentStatusBar() ? ColorPalette.getOscuredColor(getPrimaryColor()) : getPrimaryColor());
        options.setCropFrameColor(getAccentColor());
        options.setFreeStyleCropEnabled(true);

        return options;
    }


    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isApplyThemeOnImgAct())
                if (isNavigationBarColored())
                    getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
                else
                    getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), getTransparency()));
            else
                getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 175));//MUST BE SETTED BETTER
        }
    }


    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isApplyThemeOnImgAct())
                if (isTraslucentStatusBar() && isTransparencyZero())
                    getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                else
                    getWindow().setStatusBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
            else
                getWindow().setStatusBarColor(ColorPalette.getTransparentColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 175));//TODO ;UST BE BETER FIXXED
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (mViewPager != null) {
            outState.putBoolean(ISLOCKED_ARG, mViewPager.isLocked());
        }
        super.onSaveInstanceState(outState);
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

    public void ChangeBackGroundColor() {
        int colorTo;
        int colorFrom;
        if (fullscreenmode) {
            colorFrom = getBackgroundColor();
            colorTo = (ContextCompat.getColor(PhotoPagerActivity.this, R.color.md_black_1000));
        } else {
            colorFrom = (ContextCompat.getColor(PhotoPagerActivity.this, R.color.md_black_1000));
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

