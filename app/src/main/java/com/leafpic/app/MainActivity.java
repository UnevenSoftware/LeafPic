package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leafpic.app.Adapters.AlbumsAdapter;
import com.leafpic.app.Adapters.PhotosAdapter;
import com.leafpic.app.Base.Album;
import com.leafpic.app.Base.AlbumSettings;
import com.leafpic.app.Base.CustomAlbumsHandler;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Base.Media;
import com.leafpic.app.Views.GridSpacingItemDecoration;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.ColorPalette;
import com.leafpic.app.utils.Measure;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ThemedActivity {

    public static String TAG = "AlbumsAct";

    HandlingAlbums albums = new HandlingAlbums(MainActivity.this);
    CustomAlbumsHandler customAlbumsHandler = new CustomAlbumsHandler(MainActivity.this);
    Album album = new Album(MainActivity.this);

    RecyclerView mRecyclerView;
    AlbumsAdapter adapt;
    PhotosAdapter adapter;
    FloatingActionButton fabCamera;
    DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    private SwipeRefreshLayout SwipeContainerRV;

    boolean editmode = false, albumsMode = true;
    int nReloads=-1;

    GridSpacingItemDecoration albumsDecoration;
    GridSpacingItemDecoration photosDecoration;

    View.OnLongClickListener photosOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView is = (TextView) v.findViewById(R.id.photo_path);
            adapter.notifyItemChanged(album.toggleSelectPhoto(is.getTag().toString()));
            editmode = true;
            invalidateOptionsMenu();
            return true;
        }
    };

    View.OnClickListener photosOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (nReloads % 2 != 0) {
                TextView is = (TextView) v.findViewById(R.id.photo_path);
                if (editmode) {
                    adapter.notifyItemChanged(album.toggleSelectPhoto(is.getTag().toString()));
                    invalidateOptionsMenu();
                } else {
                    album.setCurrentPhoto(is.getTag().toString());
                    Intent intent = new Intent(MainActivity.this, PhotoPagerActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("album", album);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        }
    };

    private View.OnLongClickListener albumOnLongCLickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView a = (TextView) v.findViewById(R.id.album_name);
            adapt.notifyItemChanged(albums.toggleSelectAlbum(a.getTag().toString()));
            editmode = true;
            invalidateOptionsMenu();
            return true;
        }
    };

    private View.OnClickListener albumOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (nReloads % 2 == 0) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                if (editmode) {
                    adapt.notifyItemChanged(albums.toggleSelectAlbum(a.getTag().toString()));
                    invalidateOptionsMenu();
                } else
                    openAlbum(albums.getAlbum(a.getTag().toString()));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        try {
            Bundle data = getIntent().getExtras();
            albums = data.getParcelable("albums");
            assert albums != null;
            albums.setContext(MainActivity.this);
        } catch (NullPointerException e) { e.printStackTrace(); }

        /**** SET UP UI ****/
        initUI();
        setupUI();


    }

    @Override
    public void onResume() {
        super.onResume();
        albums.clearSelectedAlbums();
        setupUI();
        invalidateOptionsMenu();
        if (albumsMode) {
            if (nReloads != -1) new PrepareAlbumTask().execute();
        } else new PreparePhotosTask().execute();

        nReloads++;
    }

    public void openAlbum(Album a) {
        album = a;
        toolbar.setTitle(a.DisplayName);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        mRecyclerView.removeItemDecoration(albumsDecoration);
        mRecyclerView.addItemDecoration(photosDecoration);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Measure.getPhotosColums(getApplicationContext())));
        album.setContext(MainActivity.this);

        adapter = new PhotosAdapter(album.medias, MainActivity.this);
        new PreparePhotosTask().execute();

        adapter.setOnClickListener(photosOnClickListener);
        adapter.setOnLongClickListener(photosOnLongClickListener);
        mRecyclerView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlbums();
            }
        });
        albumsMode = editmode = false;
        invalidateOptionsMenu();
    }


    public void displayAlbums() {
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
        toolbar.setTitle(getString(R.string.app_name));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Measure.getAlbumsColums(getApplicationContext())));
        mRecyclerView.removeItemDecoration(photosDecoration);
        mRecyclerView.addItemDecoration(albumsDecoration);

        adapt = new AlbumsAdapter(albums.dispAlbums, getApplicationContext());
        new PrepareAlbumTask().execute();

        adapt.setOnClickListener(albumOnClickListener);
        adapt.setOnLongClickListener(albumOnLongCLickListener);
        mRecyclerView.setAdapter(adapt);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mDrawerLayout.openDrawer(Gravity.START);}
        });
        albumsMode = true;
        editmode = false;
        invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        int nSpan;
        if (albumsMode){
            nSpan = Measure.getAlbumsColums(MainActivity.this);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, nSpan));
            mRecyclerView.removeItemDecoration(albumsDecoration);
            albumsDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
            mRecyclerView.addItemDecoration(albumsDecoration);
        }else {
            nSpan = Measure.getPhotosColums(MainActivity.this);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, nSpan));
            mRecyclerView.removeItemDecoration(photosDecoration);
            photosDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
            mRecyclerView.addItemDecoration(photosDecoration);
        }
    }

    public void initUI() {

        /**** TOOLBAR ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        if(!isDarkTheme())
            toolbar.setPopupTheme(R.style.LightActionBarMenu);
        //TODO:FIX IT PLIS CUZ I KNOW U CAN
        /*else
            toolbar.setPopupTheme(R.style.DarkActionBarMenu);*/

        /**** RECYCLER VIEW ****/
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        albumsDecoration = new GridSpacingItemDecoration(Measure.getAlbumsColums(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);
        photosDecoration = new GridSpacingItemDecoration(Measure.getPhotosColums(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);
        mRecyclerView.addItemDecoration(albumsDecoration);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Measure.getAlbumsColums(getApplicationContext())));

        adapt = new AlbumsAdapter(albums.dispAlbums, getApplicationContext());
        adapt.setOnClickListener(albumOnClickListener);
        adapt.setOnLongClickListener(albumOnLongCLickListener);
        mRecyclerView.setAdapter(adapt);

        /**** SWIPE TO REFRESH ****/
        SwipeContainerRV = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        SwipeContainerRV.setColorSchemeResources(R.color.accent_blue);
        SwipeContainerRV.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
            }
        });


        /**** DRAWER ****/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                //Put your code here
                // materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            }

            public void onDrawerOpened(View drawerView) {
                //Put your code here
                //materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
            }
        });

        TextView logo = (TextView) findViewById(R.id.txtLogo);
        logo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Figa.ttf"));

        /**** FAB ***/
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!albumsMode && album.areFiltersActive()) {
                    album.filterMedias(Album.FILTER_ALL);
                    adapter.updateDataset(album.medias);
                    toolbar.getMenu().findItem(R.id.all_media_filter).setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(MainActivity.this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                } else startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
            }
        });

        setRecentApp(getString(R.string.app_name));
    }

    //region UI/GRAPHIC
    public void setupUI() {

        //toolbar.setPopupTheme(isDarkTheme() ? R.style.MyDarkToolbarStyle : R.style.MyLightToolbarStyle);
        //setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(getPrimaryColor());

        //TODO:FIX IT PLIS CUZ I KNOW U CAN
        /*else
            toolbar.setPopupTheme(R.style.DarkActionBarMenu);*/

        setStatusBarColor();
        setNavBarColor();
        fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        setDrawerTheme();
        mRecyclerView.setBackgroundColor(getBackgroundColor());
    }

    public void setDrawerTheme() {
        RelativeLayout DrawerHeader = (RelativeLayout) findViewById(R.id.Drawer_Header);
        DrawerHeader.setBackgroundColor(getPrimaryColor());

        LinearLayout DrawerBody = (LinearLayout) findViewById(R.id.Drawer_Body);
        DrawerBody.setBackgroundColor(getDrawerBackground());//getBackgroundColor()

        ScrollView DrawerScroll = (ScrollView) findViewById(R.id.Drawer_Body_Scroll);
        DrawerScroll.setBackgroundColor(getDrawerBackground());//getBackgroundColor()

        View DrawerDivider2 = findViewById(R.id.Drawer_Body_Divider);
        DrawerDivider2.setBackgroundColor(ColorPalette.getTransparentColor(
                ContextCompat.getColor(MainActivity.this, R.color.md_black_1000), 150));

        /** drawer items **/
        TextView txtDD = (TextView) findViewById(R.id.Drawer_Default_Item);
        TextView txtDH = (TextView) findViewById(R.id.Drawer_Hidden_Item);
        TextView txtDMoments = (TextView) findViewById(R.id.Drawer_Moments_Item);
        TextView txtDS = (TextView) findViewById(R.id.Drawer_Setting_Item);
        TextView txtDDonate = (TextView) findViewById(R.id.Drawer_Donate_Item);
        TextView txtGithub = (TextView) findViewById(R.id.Drawer_github_Item);
        TextView txtWall = (TextView) findViewById(R.id.Drawer_wallpapers_Item);
        TextView txtAbout = (TextView) findViewById(R.id.Drawer_About_Item);



        IconicsImageView imgDD = (IconicsImageView) findViewById(R.id.Drawer_Default_Icon);
        IconicsImageView imgWall = (IconicsImageView) findViewById(R.id.Drawer_wallpapers_Icon);
        IconicsImageView imgDH = (IconicsImageView) findViewById(R.id.Drawer_Hidden_Icon);
        IconicsImageView imgDMoments = (IconicsImageView) findViewById(R.id.Drawer_Moments_Icon);
        IconicsImageView imgDDonate = (IconicsImageView) findViewById(R.id.Drawer_Donate_Icon);
        IconicsImageView imgGithub = (IconicsImageView) findViewById(R.id.Drawer_github_Icon);
        IconicsImageView imgDS = (IconicsImageView) findViewById(R.id.Drawer_Setting_Icon);
        IconicsImageView imgAbout = (IconicsImageView) findViewById(R.id.Drawer_About_Icon);

        /**textViews Colors*/
        int color = getTextColor();
        txtDD.setTextColor(color);
        txtDH.setTextColor(color);
        txtDMoments.setTextColor(color);
        txtDS.setTextColor(color);
        txtDDonate.setTextColor(color);
        txtGithub.setTextColor(color);
        txtWall.setTextColor(color);
        txtAbout.setTextColor(color);

        /*
        color = isDarkTheme()
                ? ColorPalette.getLightBackgroundColor(getApplicationContext())
                : ColorPalette.getDarkBackgroundColor(getApplicationContext());
        */
        color=getIconColor();

        imgDD.setColor(color);
        imgDDonate.setColor(color);
        imgDH.setColor(color);
        imgDMoments.setColor(color);
        imgGithub.setColor(color);
        imgDS.setColor(color);
        imgWall.setColor(color);
        imgAbout.setColor(color);

        /****DRAWER CLICK LISTENER****/
        findViewById(R.id.ll_drawer_Setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ll_drawer_About).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ll_drawer_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/DNLDsht/LeafPic"));
                startActivity(i);
            }
        });
    }
    //endregion

    void updateSelectedStuff() {
        int c;
        try {
            if (albumsMode) {
                if ((c = albums.getSelectedCount()) != 0) {
                    toolbar.setTitle(c + "/" + albums.dispAlbums.size());
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_check));
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editmode = false;
                            albums.clearSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (albums.getSelectedCount() == albums.dispAlbums.size())
                                albums.clearSelectedAlbums();
                            else albums.selectAllAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                } else {
                    toolbar.setTitle(getString(R.string.app_name));
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
                    toolbar.setOnClickListener(null);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.openDrawer(GravityCompat.START);
                        }
                    });
                }
            } else {
                if ((c = album.getSelectedCount()) != 0) {
                    toolbar.setTitle(c + "/" + album.medias.size());
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_check));
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishEditMode();
                        }
                    });
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (album.getSelectedCount() == album.medias.size())
                                album.clearSelectedPhotos();
                            else album.selectAllPhotos();
                            adapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                } else {
                    toolbar.setTitle(album.DisplayName);
                    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
                    toolbar.setOnClickListener(null);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            displayAlbums();
                        }
                    });
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void finishEditMode() {
        editmode = false;
        invalidateOptionsMenu();
        album.clearSelectedPhotos();
        adapter.notifyDataSetChanged();
    }
    //region MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_albums, menu);

        if (albumsMode) {
            menu.findItem(R.id.select_all).setTitle(
                    getString(albums.getSelectedCount() == adapt.getItemCount()
                            ? R.string.clear_selected
                            : R.string.select_all));
        } else {
            menu.findItem(R.id.select_all).setTitle(getString(
                    album.getSelectedCount() == adapter.getItemCount()
                            ? R.string.clear_selected
                            : R.string.select_all));

            menu.findItem(R.id.ascending_sort_action).setChecked(album.settings.ascending);
            if (album.settings.columnSortingMode == null || album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DATE_TAKEN))
                menu.findItem(R.id.date_taken_sort_action).setChecked(true);
            else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                menu.findItem(R.id.name_sort_action).setChecked(true);
            else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.SIZE))
                menu.findItem(R.id.size_sort_action).setChecked(true);
        }

        menu.findItem(R.id.search_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_search));
        menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        menu.findItem(R.id.sort_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_sort));
        menu.findItem(R.id.filter_menu).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_filter_list));
        menu.findItem(R.id.sharePhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));

        final MenuItem searchItem = menu.findItem(R.id.search_action);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Coming soon!");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (albumsMode) {
            editmode = albums.getSelectedCount() != 0;
            menu.setGroupVisible(R.id.album_options_menu, editmode);
            menu.setGroupVisible(R.id.photos_option_men, false);
        } else {
            editmode = album.getSelectedCount() != 0;
            menu.setGroupVisible(R.id.photos_option_men, editmode);
            menu.setGroupVisible(R.id.album_options_menu, !editmode);
        }

        togglePrimaryToolbarOptions(menu);
        updateSelectedStuff();

        /** custom items **/
        menu.findItem(R.id.select_all).setVisible(editmode);
        menu.findItem(R.id.delete_action).setVisible((albumsMode && editmode) || (!albumsMode));
        menu.findItem(R.id.setAsAlbumPreview).setVisible(!albumsMode && album.getSelectedCount() == 1);
        menu.findItem(R.id.clear_album_preview).setVisible(!albumsMode && album.hasCustomCover());
        menu.findItem(R.id.renameAlbum).setVisible((albumsMode && albums.getSelectedCount()==1) || (!albumsMode && !editmode));

        return super.onPrepareOptionsMenu(menu);
    }

    private void togglePrimaryToolbarOptions(final Menu menu) {
        menu.setGroupVisible(R.id.general_action, !editmode);

        if (!editmode) {
            menu.findItem(R.id.size_sort_action).setVisible(!albumsMode);
            menu.findItem(R.id.filter_menu).setVisible(!albumsMode);
            menu.findItem(R.id.search_action).setVisible(albumsMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.select_all:
                if (albumsMode) {
                    if (albums.getSelectedCount() == adapt.getItemCount()) {
                        editmode = false;
                        albums.clearSelectedAlbums();
                    } else albums.selectAllAlbums();
                    adapt.notifyDataSetChanged();
                } else {
                    if (album.getSelectedCount() == adapter.getItemCount()) {
                        editmode = false;
                        album.clearSelectedPhotos();
                    } else album.selectAllPhotos();
                    adapter.notifyDataSetChanged();
                }
                invalidateOptionsMenu();
                break;

            case R.id.name_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATA);
                    new PrepareAlbumTask().execute();
                } else {
                    album.setDefaultSortingMode(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                    new PreparePhotosTask().execute();
                }
                item.setChecked(true);
                break;
            case R.id.date_taken_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);
                    new PrepareAlbumTask().execute();
                } else {
                    album.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);
                    new PreparePhotosTask().execute();
                }
                item.setChecked(true);
                break;

            case R.id.size_sort_action:
                if (!albumsMode) {
                    album.setDefaultSortingMode(MediaStore.Images.ImageColumns.SIZE);
                    new PreparePhotosTask().execute();
                    item.setChecked(true);
                }
                break;

            case R.id.ascending_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingAscending(!item.isChecked());
                    new PrepareAlbumTask().execute();
                } else {
                    album.setDefaultSortingAscending(!album.settings.ascending);
                    new PreparePhotosTask().execute();
                }
                item.setChecked(!item.isChecked());
                break;

            case R.id.all_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_ALL);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                }
                break;
            case R.id.video_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_VIDEO);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                break;
            case R.id.image_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_IMAGE);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                break;

            case R.id.gifs_media_filter:
                if (!albumsMode) {
                    album.filterMedias(Album.FILTER_GIF);
                    adapter.updateDataset(album.medias);
                    item.setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                break;

            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;

            case R.id.sharePhotos:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sent_to_action));

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (Media f : album.selectedMedias)
                    files.add(f.getUri());

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setType(StringUtils.getGenericMIME(album.selectedMedias.get(0).MIME));
                finishEditMode();
                startActivity(intent);
                break;

            case R.id.excludeAlbumButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.exclude_album_message)
                        .setPositiveButton(this.getString(R.string.exclude), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (albumsMode) {
                                    albums.excludeSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                } else {
                                    customAlbumsHandler.excludeAlbum(album.ID);
                                    displayAlbums();
                                }
                            }
                        })
                        .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
                break;

            case R.id.hideAlbumButton:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setMessage(R.string.hide_album_message)
                        .setPositiveButton(this.getString(R.string.hide), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (albumsMode) {
                                    albums.hideSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                } else {
                                    albums.hideAlbum(album.Path);
                                    displayAlbums();
                                }
                            }
                        })
                        .setNeutralButton(this.getString(R.string.exclude), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (albumsMode) {
                                    albums.excludeSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                } else {
                                    customAlbumsHandler.excludeAlbum(album.ID);
                                    displayAlbums();
                                }
                            }
                        })
                        .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder2.show();
                break;

            case R.id.renameAlbum:
                class ReanameAlbum extends AsyncTask<String, Integer, Void> {

                    @Override
                    protected void onPreExecute() {
                        SwipeContainerRV.setRefreshing(true);
                        super.onPreExecute();
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        Log.wtf(TAG,"update "  +values[0]);
                    }

                    @Override
                    protected Void doInBackground(String... arg0) {
                        Log.wtf("",arg0[0]);
                        //albums.renameAlbum(albums.getSelectedAlbum(0).Path, arg0[0]);
                        try {
                            File from = new File(albums.getSelectedAlbum(0).Path);
                            File to = new File(StringUtils.getAlbumPathRenamed(albums.getSelectedAlbum(0).Path, arg0[0]));
                           /* String s[] = from.list(), dirPath = from.getAbsolutePath();
                            for (String paht : s) {
                                scanFile(new String[]{dirPath + "/" + paht});
                            }*/
                            if(from.renameTo(to)) {
                                String s[] = to.list();
                                String dirPath = to.getAbsolutePath();
                                for (int i = 0; i < s.length; i++) {
                                    scanFile(new String[]{dirPath + "/" + s[i]});
                                    setProgress(i);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void result) {
                        //adapt.updateDataset(albums.dispAlbums);
                        //nReloads++;
                        new PrepareAlbumTask().execute();
                        //SwipeContainerRV.setRefreshing(false);
                    }
                }

                final AlertDialog.Builder RenameDialog = new AlertDialog.Builder(
                        MainActivity.this,
                        isDarkTheme()
                        ? R.style.AlertDialog_Dark
                        : R.style.AlertDialog_Light);

                final View Rename_dialogLayout = getLayoutInflater().inflate(R.layout.rename_dialog, null);
                final TextView title = (TextView) Rename_dialogLayout.findViewById(R.id.rename_title);
                final EditText txt_edit = (EditText) Rename_dialogLayout.findViewById(R.id.dialog_txt);
                CardView cv_Rename_Dialog = (CardView) Rename_dialogLayout.findViewById(R.id.rename_card);

                cv_Rename_Dialog.setBackgroundColor(getCardBackgroundColor());
                title.setBackgroundColor(getPrimaryColor());
                title.setText(getString(R.string.rename_album));
                txt_edit.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
                txt_edit.setTextColor(getTextColor());
                txt_edit.setText(albumsMode ? albums.getSelectedAlbum(0).DisplayName :album.DisplayName);
                RenameDialog.setView(Rename_dialogLayout);

                RenameDialog.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {dialog.cancel();}});
                RenameDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (txt_edit.length() != 0) {
                            if (albumsMode) {
                                new ReanameAlbum().execute(txt_edit.getText().toString());
                                //onResume();
                            } else {
                                //albums.renameAlbum(album.Path, txt_edit.getText().toString());
                                //toolbar.setTitle(album.DisplayName = txt_edit.getText().toString());
                            }
                        } else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.nothing_changed));

                    }
                });
                RenameDialog.show();
                txt_edit.requestFocus();
            break;

            /**TODO redo foollowing merged stuff **/

            case R.id.delete_action:

                class DeletePhotos extends AsyncTask<String, Integer, Void> {
                    @Override
                    protected void onPreExecute() {
                        SwipeContainerRV.setRefreshing(true);
                        super.onPreExecute();
                    }
                    @Override
                    protected Void doInBackground(String... arg0) {
                        ArrayList<Media> selected= album.getSelectedMedias();
                        String paths[] = new String[selected.size()];
                        for (int i = 0; i <selected.size(); i++) {
                            File file = new File(selected.get(i).Path);
                            if (file.delete()) {
                                paths[i]=selected.get(i).Path;
                                //MediaScannerConnection.scanFile(MainActivity.this, new String[]{selected.get(i).Path}, null, null);
                                //getContentResolver().delete(selected.get(i).getUri(), null, null);
                                //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                album.medias.remove(selected.get(i));
                            }
                        }
                        MediaScannerConnection.scanFile(MainActivity.this, paths, null, null);
                        album.clearSelectedPhotos();
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void result) {
                        if (album.medias.size()==0){
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) { e.printStackTrace(); }
                            displayAlbums();
                            //onResume();
                            //displayAlbums();
                            //adapt.notifyDataSetChanged();
                        }
                        else {
                            adapter.updateDataset(album.medias);
                            invalidateOptionsMenu();
                            SwipeContainerRV.setRefreshing(false);
                        }
                    }
                }

                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage(R.string.delete_album_message)
                        .setPositiveButton(this.getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (albumsMode) {
                                    albums.deleteSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                } else
                                    new DeletePhotos().execute();
                            }
                        })
                        .setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder1.show();
                break;

            /*case R.id.delete_action:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(PhotosActivity.this);
                if(editmode) builder1.setMessage(R.string.delete_photos_message);
                else builder1.setMessage(R.string.delete_album_message);
                builder1.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editmode) {
                            album.deleteSelectedPhotos();

                            if (album.medias.size() == 0) {
                                startActivity(new Intent(PhotosActivity.this, MainActivity.class));
                                return;
                            }

                            adapter.notifyDataSetChanged();
                            updateHeaderContent();
                            finishEditMode();
                        } else {
                            albums.deleteAlbum(album.Path);
                            finish();
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}});
                builder1.show();
                break;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (albumsMode) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawer(GravityCompat.START);
            else finish();
        } else
            displayAlbums();

    }

    public void scanFile(String[] path) {
        MediaScannerConnection.scanFile(getApplicationContext(), path, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                System.out.println("Photo rename COMPLETED: " + path);
            }
        });
    }

    public class PrepareAlbumTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            nReloads++;
            SwipeContainerRV.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            nReloads--;
            albums.loadPreviewAlbums();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapt.updateDataset(albums.dispAlbums);
            nReloads++;
            SwipeContainerRV.setRefreshing(false);
        }
    }

    public class PreparePhotosTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            nReloads++;
            SwipeContainerRV.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            nReloads--;
            album.updatePhotos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.updateDataset(album.medias);
            nReloads++;
            SwipeContainerRV.setRefreshing(false);
        }
    }
}
