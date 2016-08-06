package com.horaapps.leafpic;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.horaapps.leafpic.Data.Album;
import com.horaapps.leafpic.Data.AlbumSettings;
import com.horaapps.leafpic.Fragments.ImageFragment;
import com.horaapps.leafpic.Views.HackyViewPager;
import com.horaapps.leafpic.Views.SharedMediaActivity;
import com.horaapps.leafpic.utils.AlertDialogsHelper;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.Measure;
import com.horaapps.leafpic.utils.PreferenceUtil;
import com.horaapps.leafpic.utils.SecurityHelper;
import com.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by dnld on 18/02/16.
 */
public class PhotoPagerActivity extends SharedMediaActivity {

    private static final String ISLOCKED_ARG = "isLocked";
    static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    private HackyViewPager mViewPager;
    private MediaPagerAdapter adapter;
    private PreferenceUtil SP;
    private RelativeLayout ActivityBackgorund;
    //private Album album;
    private SelectAlbumBottomSheet bottomSheetDialogFragment;
    private SecurityHelper securityObj;
    private Toolbar toolbar;
    private boolean fullScreenMode, customUri = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        SP = PreferenceUtil.getInstance(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (HackyViewPager) findViewById(R.id.photos_pager);
        securityObj= new SecurityHelper(PhotoPagerActivity.this);

        if (savedInstanceState != null)
            mViewPager.setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));
        try
        {
            Album album;
            if ((getIntent().getAction().equals(Intent.ACTION_VIEW) || getIntent().getAction().equals(ACTION_REVIEW)) && getIntent().getData() != null) {

                String path = ContentHelper.getMediaPath(getApplicationContext(), getIntent().getData());

                File file = null;
                if (path != null)
                    file = new File(path);

                if (file != null && file.isFile())
                    //the image is stored in the storage
                    album = new Album(getApplicationContext(), file);
                else {
                    //try to show with Uri
                    album = new Album(getApplicationContext(), getIntent().getData());
                    customUri = true;
                }
                getAlbums().addAlbum(0, album);
            }
            initUI();
            setupUI();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initUI() {

        setSupportActionBar(toolbar);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
        adapter = new MediaPagerAdapter(getSupportFragmentManager(), getAlbum().getMedia());

        adapter.setVideoOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SP.getBoolean("set_internal_player", false)) {
                    Intent mpdIntent = new Intent(PhotoPagerActivity.this, PlayerActivity.class)
                                               .setData(getAlbum().getCurrentMedia().getUri());
                    startActivity(mpdIntent);
                } else {
                    Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                    intentopenWith.setDataAndType(
                            getAlbum().getMedia().get(mViewPager.getCurrentItem()).getUri(),
                            getAlbum().getMedia().get(mViewPager.getCurrentItem()).getMIME());
                    startActivity(intentopenWith);
                }
            }
        });

        getSupportActionBar().setTitle((getAlbum().getCurrentMediaIndex() + 1) + " " + getString(R.string.of) + " " + getAlbum().getMedia().size());

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(getAlbum().getCurrentMediaIndex());
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                getAlbum().setCurrentPhotoIndex(position);
                toolbar.setTitle((position + 1) + " " + getString(R.string.of) + " " + getAlbum().getMedia().size());
                if (!fullScreenMode) new Handler().postDelayed(new Runnable() {
                    public void run() {
                        hideSystemUI();
                    }
                }, 1200);
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Display aa = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (aa.getRotation() == Surface.ROTATION_90) {
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

        /**** SETTINGS ****/

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
        getMenuInflater().inflate(R.menu.menu_view_pager, menu);

        menu.findItem(R.id.action_delete).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        menu.findItem(R.id.action_share).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.action_rotate).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_right));
        menu.findItem(R.id.rotate_right_90).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_right).color(getIconColor()));
        menu.findItem(R.id.rotate_left_90).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_rotate_left).color(getIconColor()));
        menu.findItem(R.id.rotate_180).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_replay).color(getIconColor()));

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

        menu.setGroupVisible(R.id.only_photos_otions, !getAlbum().getCurrentMedia().isVideo());

        if (customUri) {
            menu.setGroupVisible(R.id.on_internal_storage, false);
            menu.setGroupVisible(R.id.only_photos_otions, false);
            menu.findItem(R.id.sort_action).setVisible(false);
        }
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
                            //copyFileToDownloads(imageUri);
                            if(ContentHelper.copyFile(getApplicationContext(), new File(imageUri.getPath()), new File(getAlbum().getCurrentMedia().getPath()))) {
                                ((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).displayMedia();
                            }
                            //adapter.notifyDataSetChanged();
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


    private void displayAlbums(boolean reload) {
        Intent i = new Intent(PhotoPagerActivity.this, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt(SplashScreen.CONTENT, SplashScreen.ALBUMS_PREFETCHED);
        if (!reload) i.putExtras(b);
        startActivity(i);
        finish();
    }

    private void deleteCurrentMedia() {
        getAlbum().deleteCurrentMedia(getApplicationContext());
        if (getAlbum().getMedia().size() == 0) {
            if (customUri) finish();
            else {
                getAlbums().removeCurrentAlbum();
                displayAlbums(false);
            }
        }
        adapter.notifyDataSetChanged();
        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + getAlbum().getMedia().size());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.rotate_180:
                ((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).rotatePicture(180);
                break;

            case R.id.rotate_right_90:
                ((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).rotatePicture(90);
                break;

            case R.id.rotate_left_90:
                ((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).rotatePicture(-90);
                break;

            case R.id.action_copy:
                bottomSheetDialogFragment = new SelectAlbumBottomSheet();
                bottomSheetDialogFragment.setTitle(getString(R.string.copy_to));
                bottomSheetDialogFragment.setSelectAlbumInterface(new SelectAlbumBottomSheet.SelectAlbumInterface() {
                    @Override
                    public void folderSelected(String path) {
                        getAlbum().copyPhoto(getApplicationContext(), getAlbum().getCurrentMedia().getPath(), path);
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                break;

            case R.id.name_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_NAME);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_DATE);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.size_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(),AlbumSettings.SORT_BY_SIZE);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.type_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_TYPE);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_action:
                getAlbum().setDefaultSortingAscending(getApplicationContext(), !item.isChecked());
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());

                item.setChecked(!item.isChecked());
                return true;


            case R.id.action_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(getAlbum().getCurrentMedia().getMIME());
                share.putExtra(Intent.EXTRA_STREAM, getAlbum().getCurrentMedia().getUri());
                startActivity(Intent.createChooser(share, getString(R.string.send_to)));
                return true;

            case R.id.action_edit:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(getAlbum().getCurrentMedia().getPath()));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(PhotoPagerActivity.this);
                break;

            case R.id.action_use_as:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(
                        getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMIME());
                startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
                return true;

            case R.id.action_open_with:
                Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                intentopenWith.setDataAndType(
                        getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMIME());
                startActivity(Intent.createChooser(intentopenWith, getString(R.string.open_with)));
                break;

            case R.id.action_delete:
                final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());

                AlertDialogsHelper.getTextDialog(PhotoPagerActivity.this,deleteDialog,
                        R.string.delete, R.string.delete_photo_message);

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
                                        deleteCurrentMedia();
                                        adapter.notifyDataSetChanged();
                                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + getAlbum().getMedia().size());
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
                bottomSheetDialogFragment.setTitle(getString(R.string.move_to));
                bottomSheetDialogFragment.setSelectAlbumInterface(new SelectAlbumBottomSheet.SelectAlbumInterface() {
                    @Override
                    public void folderSelected(String path) {
                        getAlbum().moveCurrentMedia(getApplicationContext(), path);

                        if (getAlbum().getMedia().size() == 0) {
                            if (customUri) finish();
                            else {
                                getAlbums().removeCurrentAlbum();
                                //((MyApplication) getApplicationContext()).removeCurrentAlbum();
                                displayAlbums(false);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        toolbar.setTitle((mViewPager.getCurrentItem() + 1) + " " + getString(R.string.of) + " " + getAlbum().getCount());
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                return true;

            case R.id.action_rename:
                AlertDialog.Builder renameDialogBuilder = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());
                final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(StringUtils.getPhotoNamebyPath(getAlbum().getCurrentMedia().getPath()));

                AlertDialog renameDialog =
                        AlertDialogsHelper.getInsertTextDialog(
                                this,renameDialogBuilder, editTextNewName, R.string.rename_photo_action);

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editTextNewName.length() != 0)
                            getAlbum().renameCurrentMedia(getApplicationContext(), editTextNewName.getText().toString());
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
                editIntent.setDataAndType(getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMIME());
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, "Edit with"));
                break;

            case R.id.action_details:
                AlertDialog.Builder detailsDialogBuilder = new AlertDialog.Builder(PhotoPagerActivity.this, getDialogStyle());
                AlertDialog detailsDialog =
                        AlertDialogsHelper.getDetailsDialog(this, detailsDialogBuilder,getAlbum().getCurrentMedia());

                detailsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                                                                                           .ok_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }});
                detailsDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.fix_date), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!getAlbum().getCurrentMedia().fixDate())
                            Toast.makeText(PhotoPagerActivity.this, R.string.unable_to_fix_date, Toast.LENGTH_SHORT).show();
                    }
                });
                detailsDialog.show();
                break;

            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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
        if (fullScreenMode)
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

                fullScreenMode = true;
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
                fullScreenMode = false;
                changeBackGroundColor();
            }
        });
    }

    private void changeBackGroundColor() {
        int colorTo;
        int colorFrom;
        if (fullScreenMode) {
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

