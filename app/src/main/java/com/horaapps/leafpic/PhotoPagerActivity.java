package com.horaapps.leafpic;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.horaapps.leafpic.Adapters.MediaPagerAdapter;
import com.horaapps.leafpic.Animations.DepthPageTransformer;
import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.AlbumSettings;
import com.horaapps.leafpic.Fragments.ImageFragment;
import com.horaapps.leafpic.Views.HackyViewPager;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.AlertDialogsHelper;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.Measure;
import com.horaapps.leafpic.utils.SecurityHelper;
import com.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by dnld on 18/02/16.
 */
public class PhotoPagerActivity extends ThemedActivity {

    private static final String ISLOCKED_ARG = "isLocked";
    static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    private HackyViewPager mViewPager;
    private MediaPagerAdapter adapter;
    private SharedPreferences SP;
    private RelativeLayout ActivityBackgorund;
    private Album album;
    private SelectAlbumBottomSheet bottomSheetDialogFragment;
    private SecurityHelper securityObj;
    private Toolbar toolbar;
    private boolean fullscreenmode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        SP = PreferenceManager.getDefaultSharedPreferences(PhotoPagerActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (HackyViewPager) findViewById(R.id.photos_pager);
        securityObj= new SecurityHelper(PhotoPagerActivity.this);

        if (savedInstanceState != null)
            mViewPager.setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));
        try
        {
            if (getIntent().getAction().equals(ACTION_OPEN_ALBUM))
                album = ((MyApplication) getApplicationContext()).getCurrentAlbum();
            else if ((getIntent().getAction().equals(Intent.ACTION_VIEW) || getIntent().getAction().equals(ACTION_REVIEW))
                            && getIntent().getData() != null) {
                File folderPath = new  File( ContentHelper.getPath(getApplicationContext(),
                        getIntent().getData()));

                if (folderPath.isFile())
                    //the image is stored in the storage
                    album = new Album(getApplicationContext(), folderPath);
                else
                    //try to show with Uri
                    album = new Album(getApplicationContext(), getIntent().getData());

            }

            initUI();
            setupUI();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initUI() {

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

    private void setupUI() {

        /**** Theme ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(
                isApplyThemeOnImgAct()
                        ? ColorPalette.getTransparentColor (getPrimaryColor(), getTransparency())
                        : ColorPalette.getTransparentColor(getDefaultThemeToolbarColor3th(), 175));

        toolbar.setPopupTheme(getPopupToolbarStyle());

        ActivityBackgorund = (RelativeLayout) findViewById(R.id.PhotoPager_Layout);
        ActivityBackgorund.setBackgroundColor(getBackgroundColor());

        setStatusBarColor();
        setNavBarColor();

        securityObj.updateSecuritySetting();

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

        menu.findItem(R.id.action_delete).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        menu.findItem(R.id.action_share).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.action_rotate).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_right));
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

    private void displayAlbums(boolean reload) {
        Intent i = new Intent(PhotoPagerActivity.this, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt(SplashScreen.CONTENT, SplashScreen.ALBUMS_PREFETCHED);
        if (!reload) i.putExtras(b);
        startActivity(i);
        finish();
    }

    private void deleteCurrentMedia() {
        album.deleteCurrentMedia(getApplicationContext());
        if (album.media.size() == 0) {
            ((MyApplication) getApplicationContext()).removeCurrentAlbum();
            displayAlbums(false);
        }
        adapter.notifyDataSetChanged();
        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + album.media.size());
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

            case R.id.action_copy:

                bottomSheetDialogFragment = new SelectAlbumBottomSheet();
                bottomSheetDialogFragment.setCurrentPath(album.getPath());
                bottomSheetDialogFragment.setTitle(getString(R.string.copy_to));
                bottomSheetDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = v.findViewById(R.id.title_bottom_sheet_item).getTag().toString();
                        album.copyPhoto(getApplicationContext(), album.getCurrentMedia().getPath(), path);
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                break;

            case R.id.name_sort_action:
                album.setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_NAME);
                album.sortPhotos();
                adapter.swapDataSet(album.media);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_action:
                album.setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_DATE);
                album.sortPhotos();
                adapter.swapDataSet(album.media);
                item.setChecked(true);
                return true;

            case R.id.size_sort_action:
                album.setDefaultSortingMode(getApplicationContext(),AlbumSettings.SORT_BY_SIZE);
                album.sortPhotos();
                adapter.swapDataSet(album.media);
                item.setChecked(true);
                return true;

            case R.id.type_sort_action:
                album.setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_TYPE);
                album.sortPhotos();
                adapter.swapDataSet(album.media);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_action:
                album.setDefaultSortingAscending(getApplicationContext(), !item.isChecked());
                album.sortPhotos();
                adapter.swapDataSet(album.media);

                item.setChecked(!item.isChecked());
                return true;


            case R.id.action_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(album.getCurrentMedia().getMIME());
                share.putExtra(Intent.EXTRA_STREAM, album.getCurrentMedia().getUri());
                startActivity(Intent.createChooser(share, getString(R.string.send_to)));
                return true;

            case R.id.action_edit:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(album.getCurrentMedia().getPath()));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(PhotoPagerActivity.this);
                break;

            case R.id.action_use_as:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(
                       album.getCurrentMedia().getUri(), album.getCurrentMedia().getMIME());
                startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
                return true;

            case R.id.action_open_with:
                Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                intentopenWith.setDataAndType(
                        album.getCurrentMedia().getUri(), album.getCurrentMedia().getMIME());
                startActivity(Intent.createChooser(intentopenWith, getString(R.string.open_with)));
                break;

            case R.id.action_delete:
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());

                AlertDialogsHelper.getTextDialog(PhotoPagerActivity.this,deleteDialog,
                        getString(R.string.delete), getString(R.string.delete_photo_message));

                deleteDialog.setNegativeButton(this.getString(R.string.cancel), null);
                deleteDialog.setPositiveButton(this.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (securityObj.isActiveSecurity()&&securityObj.isPasswordOnDelete()) {

                            final AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());
                            final EditText editTextPassword = securityObj.getInsertPasswordDialog
                                    (PhotoPagerActivity.this, passwordDialogBuilder);

                            passwordDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (securityObj.checkPassword(editTextPassword.getText().toString())) {
                                        album.deleteCurrentMedia(getApplicationContext());
                                        if (album.media.size() == 0) {
                                            ((MyApplication) getApplicationContext()).removeCurrentAlbum();
                                            displayAlbums(false);
                                        }
                                        adapter.notifyDataSetChanged();
                                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + album.media.size());
                                    } else
                                        Toast.makeText(passwordDialogBuilder.getContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();

                                }
                            });
                            passwordDialogBuilder.setNegativeButton(getString(R.string.cancel), null);
                            final AlertDialog passwordDialog = passwordDialogBuilder.create();
                            passwordDialog.show();
                            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                                    .OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (securityObj.checkPassword(editTextPassword.getText().toString())){
                                        deleteCurrentMedia();
                                        passwordDialog.dismiss();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                        editTextPassword.getText().clear();
                                        editTextPassword.requestFocus();
                                    }
                                }
                            });
                        } else
                            deleteCurrentMedia();
                    }
                });
                deleteDialog.show();
                return true;

            case R.id.action_move:
                bottomSheetDialogFragment = new SelectAlbumBottomSheet();
                bottomSheetDialogFragment.setCurrentPath(album.getPath());
                bottomSheetDialogFragment.setTitle(getString(R.string.move_to));
                bottomSheetDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = v.findViewById(R.id.title_bottom_sheet_item).getTag().toString();
                        album.moveCurrentPhoto(getApplicationContext(), path);

                        if (album.media.size() == 0) {
                            ((MyApplication) getApplicationContext()).removeCurrentAlbum();
                            displayAlbums(false);
                        }
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + album.media.size());
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                return true;

            case R.id.action_rename:
                AlertDialog.Builder renameDialogBuilder = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());
                final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(StringUtils.getPhotoNamebyPath(album.getCurrentMedia().getPath()));

                AlertDialog renameDialog =
                        AlertDialogsHelper.getInsertTextDialog(
                        this,renameDialogBuilder, editTextNewName, getString(R.string.rename_photo_action));

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editTextNewName.length() != 0)
                            album.renameCurrentMedia(getApplicationContext(), editTextNewName.getText().toString());
                         else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.nothing_changed));
                    }});
                renameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { } });
                renameDialog.show();
                break;

            case R.id.action_edit_with:
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(album.getCurrentMedia().getUri(), album.getCurrentMedia().getMIME());
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, "Edit with"));
                break;

            case R.id.action_details:
                AlertDialog.Builder detailsDialogBuilder = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());
                AlertDialog detailsDialog =
                         AlertDialogsHelper.getDetailsDialog(this, detailsDialogBuilder,album.getCurrentMedia());

                detailsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                        .ok_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }});
                detailsDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.fix_date), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!album.getCurrentMedia().fixDate())
                            Toast.makeText(PhotoPagerActivity.this, R.string.unable_to_fix_date, Toast.LENGTH_SHORT).show();
                    }
                });
                detailsDialog.show();
                break;

            case R.id.action_settings:
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

    private UCrop.Options getUcropOptions() {

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);
        options.setActiveWidgetColor(getAccentColor());
        options.setToolbarColor(getPrimaryColor());
        options.setStatusBarColor(isTranslucentStatusBar() ? ColorPalette.getOscuredColor(getPrimaryColor()) : getPrimaryColor());
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
                if (isTranslucentStatusBar() && isTransparencyZero())
                    getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
                else
                    getWindow().setStatusBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
            else
                getWindow().setStatusBarColor(ColorPalette.getTransparentColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 175));
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
                changeBackGroundColor();
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
                changeBackGroundColor();
            }
        });
    }

    private void changeBackGroundColor() {
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

