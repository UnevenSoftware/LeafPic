package org.horaapps.leafpic.activities;

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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.yalantis.ucrop.UCrop;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.adapters.MediaPagerAdapter;
import org.horaapps.leafpic.animations.DepthPageTransformer;
import org.horaapps.leafpic.fragments.ImageFragment;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.AlbumSettings;
import org.horaapps.leafpic.model.ContentProviderHelper;
import org.horaapps.leafpic.model.Media;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.views.HackyViewPager;

import java.io.File;
import java.io.InputStream;

/**
 * Created by dnld on 18/02/16.
 */
@SuppressWarnings("ResourceAsColor")
public class SingleMediaActivity extends SharedMediaActivity {

    private static final String ISLOCKED_ARG = "isLocked";
    static final String ACTION_OPEN_ALBUM = "android.intent.action.pagerAlbumMedia";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";

    private HackyViewPager mViewPager;
    private MediaPagerAdapter adapter;
    private PreferenceUtil SP;
    private RelativeLayout ActivityBackground;

    private Toolbar toolbar;
    private boolean fullScreenMode, customUri = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_media);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (HackyViewPager) findViewById(R.id.photos_pager);

        SP = PreferenceUtil.getInstance(getApplicationContext());

        if (savedInstanceState != null)
            mViewPager.setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));

            if ((getIntent().getAction().equals(Intent.ACTION_VIEW) || getIntent().getAction().equals(ACTION_REVIEW)) && getIntent().getData() != null) {

                String path = ContentHelper.getMediaPath(getApplicationContext(), getIntent().getData());
                Album album = null;

                if (path != null) {
                    album = ContentProviderHelper.getAlbumFromMedia(getApplicationContext(), path);
                    if (album != null) {
                        album.updatePhotos(getApplicationContext());
                        album.setCurrentMedia(path);
                    }
                }

                if (album == null || album.getCount() == 0) {
                    Uri mediaUri = getIntent().getData();
                    Media media;
                    album = new Album(mediaUri.toString(), mediaUri.getPath());
                    album.settings = AlbumSettings.getDefaults();

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(mediaUri);
                        if (inputStream != null) inputStream.close();
                    } catch (Exception ex) {
                        ((TextView) findViewById(R.id.nothing_to_show)).setText(R.string.error_occured_open_media);
                        findViewById(R.id.ll_nothing_to_show).setVisibility(View.VISIBLE);
                    }

                    media = new Media(getApplicationContext(), mediaUri);
                    if (album.addMedia(media)) album.setCount(1);
                    customUri = true;
                }
                getAlbums().addAlbum(0, album);
            }

        adapter = new MediaPagerAdapter(getSupportFragmentManager(), getAlbum().getMedia());
        initUi();
    }

    private void initUi() {

        setSupportActionBar(toolbar);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setupSystemUI();

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showSystemUI();
                        else hideSystemUI();
                    }
                });

        updatePageTitle(getAlbum().getCurrentMediaIndex());

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(getAlbum().getCurrentMediaIndex());
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {    }

            @Override public void onPageSelected(int position) {
                getAlbum().setCurrentMedia(position);
                updatePageTitle(position);
                supportInvalidateOptionsMenu();
            }

            @Override public void onPageScrollStateChanged(int state) {    }
        });

        Display aa = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (aa.getRotation() == Surface.ROTATION_90) {
            Configuration configuration = new Configuration();
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
            onConfigurationChanged(configuration);
        }
    }

    public void updateUiElements() {
        /**** Theme ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(
                themeOnSingleImgAct()
                        ? ColorPalette.getTransparentColor (getPrimaryColor(), getTransparency())
                        : ColorPalette.getTransparentColor(getDefaultThemeToolbarColor3th(), 175));

        toolbar.setPopupTheme(getPopupToolbarStyle());

        ActivityBackground = (RelativeLayout) findViewById(R.id.PhotoPager_Layout);
        ActivityBackground.setBackgroundColor(getBackgroundColor());

        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.app_name));

        ((IconicsImageView) findViewById(R.id.nothing_to_show_icon)).setColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show)).setTextColor(getSubTextColor());


        /**** SETTINGS ****/

        if (SP.getBoolean("set_max_luminosity", false))
            updateBrightness(1.0F);
        else try {
            // TODO: 12/4/16 redo
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

    private void updatePageTitle(int position) {
        getSupportActionBar().setTitle((position + 1) + " " + getString(R.string.of) + " " + adapter.getCount());
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

        menu.findItem(R.id.action_delete).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_delete));
        menu.findItem(R.id.action_share).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.action_rotate).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_rotate_right));
        menu.findItem(R.id.rotate_right_90).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_rotate_right).color(getIconColor()));
        menu.findItem(R.id.rotate_left_90).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_rotate_left).color(getIconColor()));
        menu.findItem(R.id.rotate_180).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_replay).color(getIconColor()));

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            params.setMargins(0,0,Measure.getNavigationBarSize(SingleMediaActivity.this).x,0);
        else
            params.setMargins(0,0,0,0);

        toolbar.setLayoutParams(params);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {

        menu.setGroupVisible(R.id.only_photos_options, !getAlbum().getCurrentMedia().isVideo());

        if (customUri) {
            menu.setGroupVisible(R.id.on_internal_storage, false);
            menu.setGroupVisible(R.id.only_photos_options, false);
            menu.findItem(R.id.sort_action).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case UCrop.REQUEST_CROP:
                    final Uri imageUri = UCrop.getOutput(data);
                    if (imageUri != null && imageUri.getScheme().equals("file")) {
                        try {
                            //copyFileToDownloads(imageUri);
                            // TODO: 21/08/16 handle this better
                            if(ContentHelper.copyFile(getApplicationContext(), new File(imageUri.getPath()), new File(getAlbum().getPath()))) {
                                //((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).displayMedia(true);
                                Toast.makeText(this, R.string.new_file_created, Toast.LENGTH_SHORT).show();
                            }
                            //adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("ERROS - uCrop", imageUri.toString(), e);
                        }
                    } else
                        StringUtils.showToast(getApplicationContext(), "errori random");
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }


    private void displayAlbums(boolean reload) {
        Intent i = new Intent(SingleMediaActivity.this, MainActivity.class);
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
        updatePageTitle(mViewPager.getCurrentItem());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.rotate_180:
                if (!((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).rotatePicture(180)) {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.rotate_right_90:
                if (!((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).rotatePicture(90)) {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.rotate_left_90:
                if (!((ImageFragment) adapter.getRegisteredFragment(getAlbum().getCurrentMediaIndex())).rotatePicture(-90)) {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.action_copy:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.copy_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                getAlbum().copyPhoto(getApplicationContext(), getAlbum().getCurrentMedia().getPath(), path);
                            }
                        }).show();
                break;

            case R.id.name_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.NAME);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.DATE);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.size_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.SIZE);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.type_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.TYPE);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_action:
                getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.NUMERIC);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_action:
                getAlbum().setDefaultSortingAscending(getApplicationContext(), !item.isChecked() ? SortingOrder.ASCENDING : SortingOrder.DESCENDING);
                getAlbum().sortPhotos();
                adapter.swapDataSet(getAlbum().getMedia());

                item.setChecked(!item.isChecked());
                return true;


            case R.id.action_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(getAlbum().getCurrentMedia().getMimeType());
                share.putExtra(Intent.EXTRA_STREAM, getAlbum().getCurrentMedia().getUri());
                startActivity(Intent.createChooser(share, getString(R.string.send_to)));
                return true;

            case R.id.action_edit:
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(getAlbum().getCurrentMedia().getPath()));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(SingleMediaActivity.this);
                break;

            case R.id.action_use_as:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(
                        getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMimeType());
                startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
                return true;

            case R.id.action_open_with:
                Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                intentopenWith.setDataAndType(
                        getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMimeType());
                startActivity(Intent.createChooser(intentopenWith, getString(R.string.open_with)));
                break;

            case R.id.action_delete:
                final AlertDialog textDialog = AlertDialogsHelper.getTextDialog(SingleMediaActivity.this, R.string.delete, R.string.delete_photo_message);
                textDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        textDialog.dismiss();
                    }
                });
                textDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.delete).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Security.isPasswordOnDelete(getApplicationContext())) {

                            Security.askPassword(SingleMediaActivity.this, new Security.PasswordInterface() {
                                @Override
                                public void onSuccess() {
                                    deleteCurrentMedia();
                                }

                                @Override
                                public void onError() {
                                    Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else
                            deleteCurrentMedia();
                    }
                });
                textDialog.show();
                return true;

            case R.id.action_move:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.move_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                getAlbum().moveCurrentMedia(getApplicationContext(), path);

                                if (getAlbum().getMedia().size() == 0) {
                                    if (customUri) finish();
                                    else {
                                        getAlbums().removeCurrentAlbum();
                                        displayAlbums(false);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                                updatePageTitle(mViewPager.getCurrentItem());
                            }
                        }).show();

                return true;

            case R.id.action_rename:
                final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(StringUtils.getPhotoNameByPath(getAlbum().getCurrentMedia().getPath()));

                AlertDialog renameDialog = AlertDialogsHelper.getInsertTextDialog(this, editTextNewName, R.string.rename_photo_action);

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editTextNewName.length() != 0)
                            getAlbum().renameCurrentMedia(getApplicationContext(), editTextNewName.getText().toString());
                        else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.nothing_changed));
                    }});
                renameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); } });
                renameDialog.show();
                break;

            case R.id.action_edit_with:
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(getAlbum().getCurrentMedia().getUri(), getAlbum().getCurrentMedia().getMimeType());
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, getString(R.string.edit_with)));
                break;

            case R.id.action_details:

                final AlertDialog detailsDialog = AlertDialogsHelper.getDetailsDialog(this, getAlbum().getCurrentMedia());

                detailsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                        .ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }});

                detailsDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.fix_date).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!getAlbum().getCurrentMedia().fixDate())
                            Toast.makeText(SingleMediaActivity.this, R.string.unable_to_fix_date, Toast.LENGTH_SHORT).show();
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

    @SuppressWarnings("ResourceAsColor")
    private UCrop.Options getUcropOptions() {

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(90);
        options.setActiveWidgetColor(getAccentColor());
        options.setToolbarColor(getPrimaryColor());
        options.setStatusBarColor(isTranslucentStatusBar() ? ColorPalette.getObscuredColor(getPrimaryColor()) : getPrimaryColor());
        options.setCropFrameColor(getAccentColor());
        options.setFreeStyleCropEnabled(true);

        return options;
    }

    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (themeOnSingleImgAct())
                if (isNavigationBarColored())
                    getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
                else
                    getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), getTransparency()));
            else
                getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 175));
        }
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (themeOnSingleImgAct())
                if (isTranslucentStatusBar() && isTransparencyZero())
                    getWindow().setStatusBarColor(ColorPalette.getObscuredColor(getPrimaryColor()));
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
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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
            colorTo = (ContextCompat.getColor(SingleMediaActivity.this, R.color.md_black_1000));
        } else {
            colorFrom = (ContextCompat.getColor(SingleMediaActivity.this, R.color.md_black_1000));
            colorTo = getBackgroundColor();
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(240);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ActivityBackground.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }
}

