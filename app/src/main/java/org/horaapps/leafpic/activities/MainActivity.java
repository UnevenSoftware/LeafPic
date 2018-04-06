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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.about.AboutActivity;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.fragments.AlbumsFragment;
import org.horaapps.leafpic.fragments.BaseFragment;
import org.horaapps.leafpic.fragments.EditModeListener;
import org.horaapps.leafpic.fragments.NothingToShowListener;
import org.horaapps.leafpic.fragments.RvMediaFragment;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.LegacyCompatFileProvider;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.ItemListener;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ABOUT;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_ALBUMS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_MEDIA;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_DONATE;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_HIDDEN_FOLDERS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_SETTINGS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_WALLPAPERS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NavigationItem;

/**
 * The Main Activity used to display Albums / Media.
 */
public class MainActivity extends SharedMediaActivity implements
        RvMediaFragment.MediaClickListener, AlbumsFragment.AlbumClickListener,
        NothingToShowListener, EditModeListener, ItemListener {

    public static final String ARGS_PICK_MODE = "pick_mode";

    private static final String SAVE_ALBUM_MODE = "album_mode";

    @BindView(R.id.fab_camera) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout navigationDrawer;
    @BindView(R.id.home_navigation_drawer) NavigationDrawer navigationDrawerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.coordinator_main_layout) CoordinatorLayout mainLayout;

    private AlbumsFragment albumsFragment;
    private RvMediaFragment rvMediaFragment;

    private boolean pickMode = false;
    private boolean albumsMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initUi();
        pickMode = getIntent().getBooleanExtra(ARGS_PICK_MODE, false);

        if (savedInstanceState == null) {
            // Add AlbumsFragment to UI
            albumsMode = true;

            albumsFragment = new AlbumsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, albumsFragment, AlbumsFragment.TAG)
                    .addToBackStack(null)
                    .commit();

            albumsFragment.setListener(this);
            albumsFragment.setEditModeListener(this);
            albumsFragment.setNothingToShowListener(this);

            return;
        }

        // We have some instance state
        restoreState(savedInstanceState);

        if (!albumsMode) {
            rvMediaFragment = (RvMediaFragment) getSupportFragmentManager().findFragmentByTag(RvMediaFragment.TAG);
            rvMediaFragment.setListener(this);
            rvMediaFragment.setEditModeListener(this);
            rvMediaFragment.setNothingToShowListener(this);
        }

        albumsFragment = (AlbumsFragment) getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
        albumsFragment.setListener(this);
        albumsFragment.setEditModeListener(this);
        albumsFragment.setNothingToShowListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_ALBUM_MODE, albumsMode);
        super.onSaveInstanceState(outState);
    }

    private void restoreState(@NonNull Bundle savedInstance) {
        albumsMode = savedInstance.getBoolean(SAVE_ALBUM_MODE, true);
    }

    private void displayAlbums(boolean hidden) {
        albumsMode = true;
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        albumsFragment.displayAlbums(hidden);
    }

    public void displayMedia(Album album) {
        rvMediaFragment = RvMediaFragment.make(album);

        albumsMode = false;
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        rvMediaFragment.setListener(this);
        rvMediaFragment.setEditModeListener(this);
        rvMediaFragment.setNothingToShowListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, rvMediaFragment, RvMediaFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onMediaClick(Album album, ArrayList<Media> media, int position) {

        if (!pickMode) {
            Intent intent = new Intent(getApplicationContext(), SingleMediaActivity.class);
            intent.putExtra(SingleMediaActivity.EXTRA_ARGS_ALBUM, album);
            try {
                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_POSITION, position);
                startActivity(intent);
            } catch (Exception e) { // Putting too much data into the Bundle
                // TODO: Find a better way to pass data between the activities - possibly a key to
                // access a HashMap or a unique value of a singleton Data Repository of some sort.
                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM_LAZY);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media.get(position));
                startActivity(intent);
            }

        } else {

            Media m = media.get(position);
            Uri uri = LegacyCompatFileProvider.getUri(getApplicationContext(), m.getFile());
            Intent res = new Intent();
            res.setData(uri);
            res.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(RESULT_OK, res);
            finish();
        }
    }

    @Override
    public void changedNothingToShow(boolean nothingToShow) {
        enableNothingToSHowPlaceHolder(nothingToShow);
    }

    @Override
    public void changedEditMode(boolean editMode, int selected, int total, @javax.annotation.Nullable View.OnClickListener listener, @javax.annotation.Nullable String title) {
        if (editMode) {
            updateToolbar(
                    getString(R.string.toolbar_selection_count, selected, total),
                    GoogleMaterial.Icon.gmd_check, listener);
        } else {
            if (albumsMode) resetToolbar();
            else updateToolbar(title, GoogleMaterial.Icon.gmd_arrow_back, v -> goBackToAlbums());
        }
    }

    @Override
    public void onItemsSelected(int count, int total) {
        toolbar.setTitle(getString(R.string.toolbar_selection_count, count, total));
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
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportFragmentManager().popBackStack();
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        setupNavigationDrawer();
        setupFAB();
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle
                (this, navigationDrawer, toolbar,
                        R.string.drawer_open, R.string.drawer_close);

        navigationDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationDrawerView.setListener(this);
        navigationDrawerView.setAppVersion(BuildConfig.VERSION_NAME);
    }

    private void setupFAB() {
        fab.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fab.setOnClickListener(v -> startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)));
    }

    private void closeDrawer() {
        navigationDrawer.closeDrawer(GravityCompat.START);
    }

    private void askPassword() {

        Security.authenticateUser(MainActivity.this, new Security.AuthCallBack() {
            @Override
            public void onAuthenticated() {
                closeDrawer();
                selectNavigationItem(NAVIGATION_ITEM_HIDDEN_FOLDERS);
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
            if (Prefs.getLastVersionCode() < BuildConfig.VERSION_CODE) {
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
                Prefs.setLastVersionCode(BuildConfig.VERSION_CODE);
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

//        setScrollViewColor(navigationDrawerView);
        setAllScrollbarsColor();

        navigationDrawerView.setTheme(getPrimaryColor(), getBackgroundColor(), getTextColor(), getIconColor());

        // TODO Calvin: This performs a NO-OP. Find out what this is used for
        setRecentApp(getString(R.string.app_name));
    }

    private void setAllScrollbarsColor() {
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));
    }

    public void updateToolbar(String title, IIcon icon, View.OnClickListener onClickListener) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(getToolbarIcon(icon));
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    private void resetToolbar() {
        updateToolbar(
                getString(R.string.app_name),
                GoogleMaterial.Icon.gmd_menu,
                v -> navigationDrawer.openDrawer(GravityCompat.START));
    }

    public void enableNothingToSHowPlaceHolder(boolean status) {
        findViewById(R.id.nothing_to_show_placeholder).setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Deprecated
    public void checkNothing(boolean status) {
        //TODO: @jibo come vuo fare qua? o anzi sopra!
        ((TextView) findViewById(R.id.emoji_easter_egg)).setTextColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setTextColor(getSubTextColor());

        if (status && Prefs.showEasterEgg()) {
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
                SettingsActivity.startActivity(this);
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
                    public void onMediaClick(DialogInterface dialogInterface, int i) {
                        insertTextDialog.dismiss();
                    }
                });

                insertTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onMediaClick(DialogInterface dialogInterface, int i) {
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
                if (navigationDrawer.isDrawerOpen(GravityCompat.START))
                    closeDrawer();
                else finish();
            }
        } else {
            if (!((BaseFragment) getSupportFragmentManager().findFragmentByTag(RvMediaFragment.TAG)).onBackPressed())
                goBackToAlbums();
        }
    }

    @Override
    public void onAlbumClick(Album album) {
        displayMedia(album);
    }

    public void onItemSelected(@NavigationItem int navigationItemSelected) {
        closeDrawer();
        switch (navigationItemSelected) {

            case NAVIGATION_ITEM_ALL_ALBUMS:
                displayAlbums(false);
                selectNavigationItem(navigationItemSelected);
                break;

            case NAVIGATION_ITEM_ALL_MEDIA:
                displayMedia(Album.getAllMediaAlbum());
                break;

            case NAVIGATION_ITEM_HIDDEN_FOLDERS:
                if (Security.isPasswordOnHidden()) {
                    askPassword();
                } else {
                    selectNavigationItem(navigationItemSelected);
                    displayAlbums(true);
                }
                break;

            case NAVIGATION_ITEM_WALLPAPERS:
                Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                break;

            case NAVIGATION_ITEM_DONATE:
                DonateActivity.startActivity(this);
                break;

            case NAVIGATION_ITEM_SETTINGS:
                SettingsActivity.startActivity(this);
                break;

            case NAVIGATION_ITEM_ABOUT:
                AboutActivity.startActivity(this);
                break;
        }
    }

    private void selectNavigationItem(@NavigationItem int navItem) {
        navigationDrawerView.selectNavItem(navItem);
    }
}
