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
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.orhanobut.hawk.Hawk;
import com.yalantis.ucrop.UCrop;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.adapters.MediaPagerAdapter;
import org.horaapps.leafpic.animations.DepthPageTransformer;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.AlbumSettings;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.MediaHelper;
import org.horaapps.leafpic.data.StorageHelper;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.fragments.ImageFragment;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.LegacyCompatFileProvider;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.file.DeleteException;
import org.horaapps.leafpic.views.HackyViewPager;
import org.horaapps.liz.ColorPalette;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 18/02/16.
 */
@SuppressWarnings("ResourceAsColor")
public class SingleMediaActivity extends SharedMediaActivity {

    private static final String TAG = SingleMediaActivity.class.getSimpleName();

    private static final int SLIDE_SHOW_INTERVAL = 5000;
    private static final String ISLOCKED_ARG = "isLocked";
    public static final String ACTION_OPEN_ALBUM = "org.horaapps.leafpic.intent.VIEW_ALBUM";
    public static final String ACTION_OPEN_ALBUM_LAZY = "org.horaapps.leafpic.intent.VIEW_ALBUM_LAZY";
    private static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";


    @BindView(R.id.photos_pager)
    HackyViewPager mViewPager;

    @BindView(R.id.PhotoPager_Layout)
    RelativeLayout activityBackground;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean fullScreenMode, customUri = false;
    int position;

    private Album album;
    private ArrayList<Media> media;
    private MediaPagerAdapter adapter;
    private boolean isSlideShowOn = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_media);

        ButterKnife.bind(this);

        String action = getIntent().getAction();

        if (action != null) {
            switch (action) {
                case ACTION_OPEN_ALBUM:
                    loadAlbum(getIntent());
                    break;
                case ACTION_OPEN_ALBUM_LAZY:
                    loadAlbumsLazy(getIntent());
                    break;
                default:
                    loadUri(getIntent().getData());
                    break;

            }
        }

        /*if (action != null && action.equals(ACTION_OPEN_ALBUM)) {
            loadAlbum(getIntent());
        } else if (getIntent().getData() != null) {

        }*/

        if (savedInstanceState != null) {
            mViewPager.setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));
        }

        adapter = new MediaPagerAdapter(getSupportFragmentManager(), media);
        initUi();
    }

    private void loadAlbum(Intent intent) {
        album = intent.getParcelableExtra("album");
        position = intent.getIntExtra("position", 0);
        media = intent.getParcelableArrayListExtra("media");
    }

    private void loadAlbumsLazy(Intent intent) {
        album = intent.getParcelableExtra("album");
        //position = intent.getIntExtra("position", 0);
        Media m = intent.getParcelableExtra("media");
        media = new ArrayList<>();
        media.add(m);
        position = 0;
    }

    private void loadUri(Uri uri) {
        album = new Album(uri.toString(), uri.getPath());
        album.settings = AlbumSettings.getDefaults();

        /*
        String path = StorageHelper.getMediaPath(getApplicationContext(), getIntent().getData());
                Album album = null;

                if (path != null) {
                    album = ContentProviderHelper.getAlbumFromMedia(getApplicationContext(), path);
                    if (album != null) {
                        //album.updatePhotos(getApplicationContext());
                        album.setCurrentMedia(path);
                    }
                }
        */

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) inputStream.close();
        } catch (Exception ex) {
            //TODO: EMOJI EASTER EGG - THERE'S NOTHING TO SHOW
            ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setText(R.string.error_occured_open_media);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(Hawk.get("emoji_easter_egg", 0) == 0 ? View.VISIBLE : View.GONE);
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(Hawk.get("emoji_easter_egg", 0) == 1 ? View.VISIBLE : View.GONE);
        }

        media = new ArrayList<>(Collections.singletonList(new Media(getApplicationContext(), uri)));
        position = 0;
        customUri = true;
    }


    private void initUi() {

        setSupportActionBar(toolbar);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupSystemUI();

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showSystemUI();
                    else hideSystemUI();
                });

        updatePageTitle(position);

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(position);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                SingleMediaActivity.this.position = position;

                updatePageTitle(position);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation() == Surface.ROTATION_90) {
            Configuration configuration = new Configuration();
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
            onConfigurationChanged(configuration);
        }
    }

    Handler handler = new Handler();
    Runnable slideShowRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mViewPager.setCurrentItem((mViewPager.getCurrentItem() + 1) % album.getCount());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                handler.postDelayed(this, SLIDE_SHOW_INTERVAL);
            }
        }
    };

    @CallSuper
    public void updateUiElements() {
        super.updateUiElements();
        /**** Theme ****/
        toolbar.setBackgroundColor(
                themeOnSingleImgAct()
                        ? ColorPalette.getTransparentColor(getPrimaryColor(), 255 - Hawk.get(getString(R.string.preference_transparency), 0))
                        : ColorPalette.getTransparentColor(getDefaultThemeToolbarColor3th(), 175));

        toolbar.setPopupTheme(getPopupToolbarStyle());


        activityBackground.setBackgroundColor(getBackgroundColor());

        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.app_name));

        //TODO: EMOJI EASTER EGG - THERE'S NOTHING TO SHOW
        ((TextView) findViewById(R.id.emoji_easter_egg)).setTextColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setTextColor(getSubTextColor());


        /**** SETTINGS ****/

        if (Hawk.get("set_max_luminosity", false))
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

        if (Hawk.get("set_picture_orientation", false))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

    }

    private void updatePageTitle(int position) {
        getSupportActionBar().setTitle(getString(R.string.of, position + 1, adapter.getCount()));
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
        if (isSlideShowOn) {
            getMenuInflater().inflate(R.menu.menu_view_page_slide_on, menu);
            menu.findItem(R.id.slide_show).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_stop_circle_outline));
        } else {
            getMenuInflater().inflate(R.menu.menu_view_pager, menu);

            menu.findItem(R.id.action_delete).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_delete));
            menu.findItem(R.id.action_share).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
            menu.findItem(R.id.action_rotate).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_rotate_right));
            menu.findItem(R.id.rotate_right_90).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_rotate_right).color(getIconColor()));
            menu.findItem(R.id.rotate_left_90).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_rotate_left).color(getIconColor()));
            menu.findItem(R.id.rotate_180).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_replay).color(getIconColor()));
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            params.setMargins(0, 0, Measure.getNavigationBarSize(SingleMediaActivity.this).x, 0);
        else
            params.setMargins(0, 0, 0, 0);

        toolbar.setLayoutParams(params);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (!isSlideShowOn) {
            menu.setGroupVisible(R.id.only_photos_options, !getCurrentMedia().isVideo());

            if (customUri) {
                menu.setGroupVisible(R.id.on_internal_storage, false);
                menu.setGroupVisible(R.id.only_photos_options, false);
                menu.findItem(R.id.sort_action).setVisible(false);
            }
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
                            if (StorageHelper.copyFile(getApplicationContext(), new File(imageUri.getPath()), new File(this.album.getPath()))) {
                                //((ImageFragment) adapter.getRegisteredFragment(this.album.getCurrentMediaIndex())).displayMedia(true);
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


    private void displayAlbums() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void deleteCurrentMedia() {
        Media currentMedia = getCurrentMedia();

        MediaHelper.deleteMedia(getApplicationContext(), currentMedia)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleted -> {
                            media.remove(deleted);
                            if (media.size() == 0) {
                                displayAlbums();
                            }
                        },
                        err -> {
                            if (err instanceof DeleteException)
                                Toast.makeText(this, R.string.delete_error, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
                        },
                        () -> {
                            adapter.notifyDataSetChanged();
                            updatePageTitle(mViewPager.getCurrentItem());
                        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.rotate_180:
                if (!((ImageFragment) adapter.getRegisteredFragment(position)).rotatePicture(180)) {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.rotate_right_90:
                if (!((ImageFragment) adapter.getRegisteredFragment(position)).rotatePicture(90)) {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.rotate_left_90:
                if (!((ImageFragment) adapter.getRegisteredFragment(position)).rotatePicture(-90)) {
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.action_copy:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.copy_to))
                        .onFolderSelected(path -> {

                            Media currentMedia = getCurrentMedia();
                            boolean b = MediaHelper.copyMedia(getApplicationContext(), currentMedia, path);
                            if (!b)
                                Toast.makeText(getApplicationContext(), R.string.copy_error, Toast.LENGTH_SHORT).show();
                        }).show();
                break;

            case R.id.name_sort_mode:
                this.album.setDefaultSortingMode(getApplicationContext(), SortingMode.NAME);
                this.album.sortPhotos();
                adapter.swapDataSet(media);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                this.album.setDefaultSortingMode(getApplicationContext(), SortingMode.DATE);
                this.album.sortPhotos();
                adapter.swapDataSet(media);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                this.album.setDefaultSortingMode(getApplicationContext(), SortingMode.SIZE);
                this.album.sortPhotos();
                adapter.swapDataSet(media);
                item.setChecked(true);
                return true;

            case R.id.type_sort_action:
                this.album.setDefaultSortingMode(getApplicationContext(), SortingMode.TYPE);
                this.album.sortPhotos();
                adapter.swapDataSet(media);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                this.album.setDefaultSortingMode(getApplicationContext(), SortingMode.NUMERIC);
                this.album.sortPhotos();
                adapter.swapDataSet(media);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_order:
                this.album.setDefaultSortingAscending(getApplicationContext(), !item.isChecked() ? SortingOrder.ASCENDING : SortingOrder.DESCENDING);
                this.album.sortPhotos();
                adapter.swapDataSet(media);

                item.setChecked(!item.isChecked());
                return true;


            case R.id.action_share:
                // TODO: 16/10/17 check if it works everywhere
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(getCurrentMedia().getMimeType());
                Uri uri1 = LegacyCompatFileProvider.getUri(this, getCurrentMedia().getFile());
                share.putExtra(Intent.EXTRA_STREAM, uri1);
                share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(share, getString(R.string.send_to)));
                return true;

            case R.id.action_edit:
                // TODO: 16/10/17 redo
                Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.png"));
                Uri uri = Uri.fromFile(new File(getCurrentMedia().getPath()));
                UCrop uCrop = UCrop.of(uri, mDestinationUri);
                uCrop.withOptions(getUcropOptions());
                uCrop.start(SingleMediaActivity.this);
                break;

            case R.id.action_use_as:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(LegacyCompatFileProvider.getUri(this,
                        getCurrentMedia().getFile()), getCurrentMedia().getMimeType());
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, getString(R.string.use_as)));
                return true;

            case R.id.action_open_with:
                Intent intentopenWith = new Intent(Intent.ACTION_VIEW);
                intentopenWith.setDataAndType(LegacyCompatFileProvider.getUri(this,
                        getCurrentMedia().getFile()), getCurrentMedia().getMimeType());
                intentopenWith.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intentopenWith, getString(R.string.open_with)));
                break;

            case R.id.action_delete:
                final AlertDialog textDialog = AlertDialogsHelper.getTextDialog(SingleMediaActivity.this, R.string.delete, R.string.delete_photo_message);
                textDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        textDialog.dismiss();
                    }
                });
                textDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.delete).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Security.isPasswordOnDelete()) {

                            Security.authenticateUser(SingleMediaActivity.this, new Security.AuthCallBack() {
                                @Override
                                public void onAuthenticated() {
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
                        .exploreMode(true)
                        .force(true)
                        .onFolderSelected(path -> {

                            Media currentMedia = getCurrentMedia();

                            boolean success = MediaHelper.moveMedia(getApplicationContext(), currentMedia, path);

                            if (success) {
                                media.remove(currentMedia);

                                if (media.size() == 0) {
                                    displayAlbums();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.move_error, Toast.LENGTH_SHORT).show();
                            }
                            adapter.notifyDataSetChanged();
                            updatePageTitle(mViewPager.getCurrentItem());
                        }).show();

                return true;

            case R.id.action_rename:
                final EditText editTextNewName = new EditText(this);
                editTextNewName.setText(StringUtils.getPhotoNameByPath(getCurrentMedia().getPath()));

                AlertDialog renameDialog = AlertDialogsHelper.getInsertTextDialog(this, editTextNewName, R.string.rename_photo_action);

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editTextNewName.length() != 0) {
                            Media currentMedia = getCurrentMedia();
                            boolean b = MediaHelper.renameMedia(getApplicationContext(), currentMedia, editTextNewName.getText().toString());
                            if (!b) {
                                StringUtils.showToast(getApplicationContext(), getString(R.string.rename_error));
                                //adapter.notifyDataSetChanged();
                            }
                        } else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.nothing_changed));
                    }
                });
                renameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), (dialog, which) -> dialog.dismiss());
                renameDialog.show();
                break;

            case R.id.action_edit_with:
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(LegacyCompatFileProvider.getUri(this,
                        getCurrentMedia().getFile()), getCurrentMedia().getMimeType());
                editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(editIntent, getString(R.string.edit_with)));
                break;

            case R.id.action_details:

                final AlertDialog detailsDialog = AlertDialogsHelper.getDetailsDialog(this, getCurrentMedia());

                detailsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                        .ok_action).toUpperCase(), (dialog, which) -> dialog.dismiss());

                detailsDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.fix_date).toUpperCase(), (dialog, which) -> {
                    // todo
                    //if (!getCurrentMedia().fixDate())
                    Toast.makeText(SingleMediaActivity.this, R.string.unable_to_fix_date, Toast.LENGTH_SHORT).show();
                });

                detailsDialog.show();
                break;

            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;

            case R.id.action_palette:
                Intent paletteIntent = new Intent(getApplicationContext(), PaletteActivity.class);
                paletteIntent.setData(LegacyCompatFileProvider.getUri(this,
                        getCurrentMedia().getFile()));
                paletteIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(paletteIntent);
                break;

            case R.id.slide_show:
                isSlideShowOn = !isSlideShowOn;
                if (isSlideShowOn) {
                    handler.postDelayed(slideShowRunnable, SLIDE_SHOW_INTERVAL);
                    hideSystemUI();
                } else handler.removeCallbacks(slideShowRunnable);
                supportInvalidateOptionsMenu();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                //return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public Media getCurrentMedia() {
        return media.get(position);
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
                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Log.wtf(TAG, "ui changed: " + visibility);
                    }
                });
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
                activityBackground.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(slideShowRunnable);
        handler = null;
    }
}

