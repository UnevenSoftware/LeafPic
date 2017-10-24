package org.horaapps.leafpic.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.view.IconicsImageView;
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.fragments.AlbumsFragment;
import org.horaapps.leafpic.fragments.BaseFragment;
import org.horaapps.leafpic.fragments.RvMediaFragment;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.LegacyCompatFileProvider;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends SharedMediaActivity {


    AlbumsFragment albumsFragment = new AlbumsFragment();
    RvMediaFragment rvMediaFragment = RvMediaFragment.make(Album.getEmptyAlbum());

    @BindView(R.id.fab_camera)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator_main_layout)
    CoordinatorLayout mainLayout;

    private boolean pickMode = false;
    private boolean albumsMode = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        pickMode = getIntent().getBooleanExtra(SplashScreen.PICK_MODE, false);

        if (savedInstanceState != null)
            return;

        initUi();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, albumsFragment, "albums")
                .addToBackStack(null)
                .commit();

    }

    private void displayAlbums(boolean hidden) {
        albumsMode = true;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        albumsFragment.displayAlbums(hidden);
    }

    public void displayMedia(Album album) {
        albumsMode = false;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        rvMediaFragment.setListener(new RvMediaFragment.MediaClickListener() {
            @Override
            public void onCreated() {
                rvMediaFragment.loadAlbum(album);
            }

            @Override
            public void onClick(Album album, ArrayList<Media> media, int position) {

                if (!pickMode) {
                    Intent intent = new Intent(getApplicationContext(), SingleMediaActivity.class);
                    intent.putExtra("album", album);
                    try {
                        intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                        intent.putExtra("media", media);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } catch (Exception e) {
                        intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM_LAZY);
                        intent.putExtra("media", media.get(position));
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "dio cane", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Media m = media.get(position);
                    Uri uri = LegacyCompatFileProvider.getUri(getApplicationContext(), m.getFile());
                    Intent intent = new Intent();
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, rvMediaFragment, "media")
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            fab.setVisibility(View.VISIBLE);
            fab.animate().translationY(fab.getHeight() * 2).start();
        } else
            fab.setVisibility(View.GONE);
    }

    public void goBackToAlbums() {
        albumsMode = true;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportFragmentManager().popBackStack();
    }

    private void initUi() {

        setSupportActionBar(toolbar);

        // TODO: 3/25/17 organize better
        /**** DRAWER ****/
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
            }

            public void onDrawerOpened(View drawerView) {
            }
        };


        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        findViewById(R.id.ll_drawer_Donate).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DonateActivity.class)));

        findViewById(R.id.ll_drawer_Setting).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        findViewById(R.id.ll_drawer_About).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));

        findViewById(R.id.ll_drawer_Default).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            displayAlbums(false);
        });

        findViewById(R.id.ll_drawer_all_media).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            displayMedia(Album.getAllMediaAlbum());
        });

        findViewById(R.id.ll_drawer_hidden).setOnClickListener(v -> {
            if (Security.isPasswordOnHidden()) {
                askPassword();
            } else {
                drawer.closeDrawer(GravityCompat.START);
                displayAlbums(true);
            }
        });

        findViewById(R.id.ll_drawer_Wallpapers).setOnClickListener(v -> Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show());

        /**** FAB ***/
        fab.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fab.setOnClickListener(v -> startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)));
    }

    private void askPassword() {

        Security.authenticateUser(MainActivity.this, new Security.AuthCallBack() {
            @Override
            public void onAuthenticated() {
                drawer.closeDrawer(GravityCompat.START);
                displayAlbums(true);
            }

            @Override
            public void onError() {
                Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        new Thread(() -> {
            if (Hawk.get("last_version_code", 0) < BuildConfig.VERSION_CODE) {
                String titleHtml = String.format(Locale.ENGLISH, "<font color='%d'>%s <b>%s</b></font>", getTextColor(), getString(R.string.changelog), BuildConfig.VERSION_NAME),
                        buttonHtml = String.format(Locale.ENGLISH, "<font color='%d'>%s</font>", getAccentColor(), getString(R.string.view).toUpperCase());
                Snackbar snackbar = Snackbar
                        .make(mainLayout, StringUtils.html(titleHtml), Snackbar.LENGTH_LONG)
                        .setAction(StringUtils.html(buttonHtml), view -> AlertDialogsHelper.showChangelogDialog(MainActivity.this));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getBackgroundColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    snackbarView.setElevation(getResources().getDimension(R.dimen.snackbar_elevation));
                snackbar.show();
                Hawk.put("last_version_code", BuildConfig.VERSION_CODE);
            }
        }).start();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        //TODO: MUST BE FIXED
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());

        /**** SWIPE TO REFRESH ****/

        setStatusBarColor();
        setNavBarColor();

        fab.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        fab.setVisibility(Hawk.get(getString(R.string.preference_show_fab), false) ? View.VISIBLE : View.GONE);
        mainLayout.setBackgroundColor(getBackgroundColor());

        setScrollViewColor(findViewById(R.id.drawer_scrollbar));
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));

        findViewById(R.id.Drawer_Header).setBackgroundColor(getPrimaryColor());
        findViewById(R.id.Drawer_Body).setBackgroundColor(getDrawerBackground());
        findViewById(R.id.drawer_scrollbar).setBackgroundColor(getDrawerBackground());
        findViewById(R.id.Drawer_Body_Divider).setBackgroundColor(getIconColor());

        /** TEXT VIEWS **/
        int color = getTextColor();
        ((TextView) findViewById(R.id.Drawer_Default_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_Allmedia_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_Setting_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_Donate_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_wallpapers_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_About_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_hidden_Item)).setTextColor(color);

        /** ICONS **/
        color = getIconColor();
        ((IconicsImageView) findViewById(R.id.Drawer_Default_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_Allmedia_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_Donate_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_Setting_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_wallpapers_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_About_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_hidden_Icon)).setColor(color);

        setRecentApp(getString(R.string.app_name));
    }

    public void updateToolbar(String title, IIcon icon, View.OnClickListener onClickListener) {
        updateToolbar(title, icon);
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    public void updateToolbar(String title, IIcon icon) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(getToolbarIcon(icon));
    }

    public void resetToolbar() {
        updateToolbar(
                getString(R.string.app_name),
                GoogleMaterial.Icon.gmd_menu,
                v -> drawer.openDrawer(GravityCompat.START));
    }

    public void nothingToShow(boolean status) {
        findViewById(R.id.nothing_to_show_placeholder).setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Deprecated
    public void checkNothing(boolean status) {
        //TODO: @jibo come vuo fare qua? o anzi sopra!
        ((TextView) findViewById(R.id.emoji_easter_egg)).setTextColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setTextColor(getSubTextColor());

        if (status && Hawk.get("emoji_easter_egg", 0) == 1) {
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(View.VISIBLE);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(View.GONE);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(View.GONE);
        }
    }

    //region MENU

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            /*
            case R.id.action_move:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.move_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                //TODo
                                //swipeRefreshLayout.setRefreshing(true);
                                /*if (getAlbum().moveSelectedMedia(getApplicationContext(), path) > 0) {
                                    if (getAlbum().getMedia().size() == 0) {
                                        //getAlbums().removeCurrentAlbum();
                                        //albumsAdapter.notifyDataSetChanged();
                                        displayAlbums(false);
                                    }
                                    //oldMediaAdapter.swapDataSet(getAlbum().getMedia());
                                    //finishEditMode();
                                    supportInvalidateOptionsMenu();
                                } else requestSdCardPermissions();

                                //swipeRefreshLayout.setRefreshing(false);
                            }
                        }).show();
                return true;
                */

            /*
            case R.id.action_copy:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.copy_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                boolean success = getAlbum().copySelectedPhotos(getApplicationContext(), path);
                                //finishEditMode();

                                if (!success) // TODO: 11/21/16 handle in other way
                                    requestSdCardPermissions();
                            }
                        }).show();

                return true;

                */


            /*case R.id.rename:
                final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(albumsMode ? firstSelectedAlbum.getName() : getAlbum().getName());

                final AlertDialog insertTextDialog = AlertDialogsHelper.getInsertTextDialog(MainActivity.this, editTextNewName, R.string.rename_album);

                insertTextDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        insertTextDialog.dismiss();
                    }
                });

                insertTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editTextNewName.length() != 0) {
                            swipeRefreshLayout.setRefreshing(true);
                            boolean success;
                            if (albumsMode) {

                                int index = getAlbums().albums.indexOf(firstSelectedAlbum);
                                getAlbums().getAlbum(index).updatePhotos(getApplicationContext());
                                success = getAlbums().getAlbum(index).renameAlbum(getApplicationContext(),
                                        editTextNewName.getText().toString());
                                albumsAdapter.notifyItemChanged(index);
                            } else {
                                success = getAlbum().renameAlbum(getApplicationContext(), editTextNewName.getText().toString());
                                toolbar.setTitle(getAlbum().getName());
                                oldMediaAdapter.notifyDataSetChanged();
                            }
                            insertTextDialog.dismiss();
                            if (!success) requestSdCardPermissions();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            StringUtils.showToast(getApplicationContext(), getString(R.string.insert_something));
                            editTextNewName.requestFocus();
                        }
                    }
                });

                insertTextDialog.show();
                return true;*/


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {

        if (albumsMode) {
            if (!albumsFragment.onBackPressed()) {
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                else finish();
            }
        } else {
            if (!((BaseFragment) getSupportFragmentManager().findFragmentByTag("media")).onBackPressed())
                goBackToAlbums();
        }
    }
}
