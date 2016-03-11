package com.leafpic.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.leafpic.app.Adapters.AlbumsAdapter;
import com.leafpic.app.Base.Album;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Views.GridSpacingItemDecoration;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;


public class AlbumsActivity extends ThemedActivity /*implements FolderChooserDialog.FolderCallback */ {

    public boolean RVdecor = true;
    HandlingAlbums albums = new HandlingAlbums(AlbumsActivity.this);
    RecyclerView mRecyclerView;
    AlbumsAdapter adapt;
    FloatingActionButton fabCamera;
    DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    boolean editmode = false, hidden = false;
    SwipeRefreshLayout SwipeContainerRV;

    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        /****TODO: WORK BUT, MUST BE FIXXED BETTER****/
        SwipeContainerRV = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        SwipeContainerRV.setColorSchemeResources(R.color.accent_amber,
                R.color.accent_blue,
                R.color.accent_teal,
                R.color.accent_brown);

        SwipeContainerRV.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refreshItems();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadAlbums();
                        SwipeContainerRV.setRefreshing(false);
                    }
                }, 500);
            }
        });
        /************************/

        initUiTweaks();
        checkPermissions();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
                if (isFirstStart) {
                    //albums.loadPreviewHiddenAlbums();
                    Intent i = new Intent(AlbumsActivity.this, IntroActivity.class);
                    startActivity(i);
                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean("firstStart", false);
                    e.apply();
                }
            }
        });
        t.start();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();
        albums.clearSelectedAlbums();
        updateSelectedStuff();
        invalidateOptionsMenu();
        checkPermissions();
        initUiTweaks();
    }

    public void initUiTweaks() {
        /******TOOLBAR***/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getPrimaryColor());
        setNavBarColor();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        /**** STATUS BAR*****/
        setStatusBarColor();

        /**** FAB ***/
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(i);
            }
        });

        setDrawerTheme();
        mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            /* Called when drawer is closed */
            public void onDrawerClosed(View view) {
                //Put your code here
                // materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
            }
            /* Called when a drawer is opened */
            public void onDrawerOpened(View drawerView) {
                //Put your code here
                //materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
            }
        });

        setRecentApp(getString(R.string.app_name));
    }

    public void setDrawerTheme(){

        RelativeLayout DrawerHeader = (RelativeLayout) findViewById(R.id.Drawer_Header);
        DrawerHeader.setBackgroundColor(getPrimaryColor());

        LinearLayout DrawerBody = (LinearLayout) findViewById(R.id.Drawer_Body);
        DrawerBody.setBackgroundColor(getBackgroundColor());

        ScrollView DrawerScroll = (ScrollView) findViewById(R.id.Drawer_Body_Scroll);
        DrawerScroll.setBackgroundColor(getBackgroundColor());


        View DrawerDivider = findViewById(R.id.Drawer_Divider);
        DrawerDivider.setBackgroundColor(getAccentColor());
        View DrawerDivider2 = findViewById(R.id.Drawer_Body_Divider);
        DrawerDivider2.setBackgroundColor(ContextCompat.getColor(AlbumsActivity.this, R.color.drawer_transparent_gray));

        /** drawer items **/
        TextView txtDD = (TextView) findViewById(R.id.Drawer_Default_Item);
        TextView txtDH = (TextView) findViewById(R.id.Drawer_Hidden_Item);
        TextView txtDMoments = (TextView) findViewById(R.id.Drawer_Moments_Item);
        TextView txtDMap = (TextView) findViewById(R.id.Drawer_Map_Item);
        TextView txtDS = (TextView) findViewById(R.id.Drawer_Setting_Item);
        TextView txtDDonate = (TextView) findViewById(R.id.Drawer_Donate_Item);

        ImageView imgDD = (ImageView) findViewById(R.id.Drawer_Default_Icon);
        ImageView imgDH = (ImageView) findViewById(R.id.Drawer_Hidden_Icon);
        ImageView imgDMoments = (ImageView) findViewById(R.id.Drawer_Moments_Icon);
        ImageView imgDMap = (ImageView) findViewById(R.id.Drawer_Map_Icon);
        ImageView imgDS = (ImageView) findViewById(R.id.Drawer_Setting_Icon);
        ImageView imgDDonate = (ImageView) findViewById(R.id.Drawer_Donate_Icon);

        /**textViews Colors*/
        txtDD.setTextColor(getTextColor());
        txtDH.setTextColor(getTextColor());
        txtDMoments.setTextColor(getTextColor());
        txtDMap.setTextColor(getTextColor());
        txtDS.setTextColor(getTextColor());
        txtDDonate.setTextColor(getTextColor());

        if (isDarkTheme()){
            imgDD.setImageResource(R.mipmap.ic_image_white_24dp);
            imgDH.setImageResource(R.mipmap.ic_visibility_off_white_24dp);
            imgDMoments.setImageResource(R.mipmap.ic_event_available_white_24dp);
            imgDMap.setImageResource(R.mipmap.ic_location_on_white_24dp);
            imgDS.setImageResource(R.mipmap.ic_settings_white_24dp);
            imgDDonate.setImageResource(R.mipmap.ic_card_giftcard_white_24dp);
        } else {
            imgDD.setImageResource(R.mipmap.ic_image_black_24dp);
            imgDH.setImageResource(R.mipmap.ic_visibility_off_black_24dp);
            imgDMoments.setImageResource(R.mipmap.ic_event_available_black_24dp);
            imgDMap.setImageResource(R.mipmap.ic_location_on_black_24dp);
            imgDS.setImageResource(R.mipmap.ic_settings_black_24dp);
            imgDDonate.setImageResource(R.mipmap.ic_card_giftcard_black_24dp);
        }

        /****DRAWER CLICK LISTENER****/
        findViewById(R.id.ll_drawer_Setting).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(AlbumsActivity.this, SettingActivity.class);
                 startActivity(intent);
             }
         });
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    public void checkPermissions() {

        if (ContextCompat.checkSelfPermission(AlbumsActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AlbumsActivity.this,
                    Manifest.permission.INTERNET))
                StringUtils.showToast(AlbumsActivity.this, "eddai dammi internet");
            else
                ActivityCompat.requestPermissions(AlbumsActivity.this,
                        new String[]{Manifest.permission.INTERNET}, 1);
        }

        if (ContextCompat.checkSelfPermission(AlbumsActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AlbumsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
                StringUtils.showToast(AlbumsActivity.this, "no storage permission");
            else
                ActivityCompat.requestPermissions(AlbumsActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else
            loadAlbums();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_albums, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        MenuItem opt;

        if (editmode) {

            setOptionsAlbmuMenusItemsVisible(menu, true);

            //opt = menu.findItem(R.id.action_camera);
            //opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            opt = menu.findItem(R.id.sort_action);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            opt = menu.findItem(R.id.deleteAction);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {

            setOptionsAlbmuMenusItemsVisible(menu, false);

            //opt = menu.findItem(R.id.action_camera);
            //opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            opt = menu.findItem(R.id.sort_action);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            opt = menu.findItem(R.id.deleteAction);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }


        if (hidden) {
            opt = menu.findItem(R.id.refreshhiddenAlbumsButton);
            opt.setEnabled(true).setVisible(true);
            opt = menu.findItem(R.id.hideAlbumButton);
            opt.setTitle(getString(R.string.unhide_album_action));
        } else {
            opt = menu.findItem(R.id.refreshhiddenAlbumsButton);
            opt.setEnabled(false).setVisible(false);
            opt = menu.findItem(R.id.hideAlbumButton);
            opt.setTitle(getString(R.string.hide_album_action));
        }

        if (albums.getSelectedCount() == 0) {
            editmode = false;
            invalidateOptionsMenu();
        }

        updateSelectedStuff();
        return super.onPrepareOptionsMenu(menu);
    }

    void updateSelectedStuff() {
        int c;
        try {

            if ((c = albums.getSelectedCount()) != 0) {
                getSupportActionBar().setTitle(c + "/" + albums.dispAlbums.size());
                toolbar.setNavigationIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_check)
                        .color(Color.WHITE)
                        .sizeDp(20));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editmode = false;
                        invalidateOptionsMenu();
                        albums.clearSelectedAlbums();
                        adapt.notifyDataSetChanged();
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
                getSupportActionBar().setTitle(getString(R.string.app_name));
                toolbar.setNavigationIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_menu)
                        .color(Color.WHITE)
                        .sizeDp(20));
                toolbar.setOnClickListener(null);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void setOptionsAlbmuMenusItemsVisible(final Menu menu, boolean val) {
        MenuItem opt = menu.findItem(R.id.hideAlbumButton);
        opt.setEnabled(val).setVisible(val);
        opt = menu.findItem(R.id.deleteAction);
        opt.setEnabled(val).setVisible(val);
        opt = menu.findItem(R.id.excludeAlbumButton);
        opt.setEnabled(val).setVisible(val);

        opt = menu.findItem(R.id.select_all_albums_action);
        opt.setEnabled(val).setVisible(val);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.filterPhotos:
                final PopupMenu popupfilter = new PopupMenu(AlbumsActivity.this, findViewById(R.id.filterPhotos));
                popupfilter.setGravity(Gravity.AXIS_PULL_BEFORE);
                popupfilter.getMenuInflater().inflate(R.menu.filter, popupfilter.getMenu());
                break;

            case R.id.sort_action:
                View sort_btn = findViewById(R.id.sort_action);
                PopupMenu popup = new PopupMenu(AlbumsActivity.this, sort_btn);
                popup.setGravity(Gravity.AXIS_CLIP);

                popup.getMenuInflater()
                        .inflate(R.menu.sort, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                AlbumsActivity.this,
                                "You Clicked: " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });

                popup.show(); //showing popup menu
                break;

            case R.id.refreshhiddenAlbumsButton:
                albums.loadPreviewHiddenAlbums();
                adapt.notifyDataSetChanged();
                break;

            case R.id.select_all_albums_action:
                albums.selectAllAlbums();
                adapt.notifyDataSetChanged();
                invalidateOptionsMenu();
                break;

            case R.id.excludeAlbumButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(AlbumsActivity.this);
                builder.setMessage(R.string.exclude_album_message)
                        .setPositiveButton("EXCLUDE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                albums.excludeSelectedAlbums();
                                adapt.notifyDataSetChanged();
                                invalidateOptionsMenu();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
                break;

            case R.id.deleteAction:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(AlbumsActivity.this);
                builder1.setMessage(R.string.delete_album_message)
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                albums.deleteSelectedAlbums();
                                adapt.notifyDataSetChanged();
                                invalidateOptionsMenu();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder1.show();
                break;

            case R.id.hideAlbumButton:
                if (hidden) {
                    albums.unHideSelectedAlbums();
                    adapt.notifyDataSetChanged();
                    invalidateOptionsMenu();
                } else {

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(AlbumsActivity.this);
                    builder2.setMessage(R.string.delete_album_message)
                            .setPositiveButton("HIDE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    albums.hideSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                }
                            })
                            .setNeutralButton("EXCLUDE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    albums.excludeSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    builder2.show();
                }
                break;
            case R.id.settingsTry_albums_action:
                Intent asd = new Intent(AlbumsActivity.this, SettingActivity.class);
                startActivity(asd);
                break;
            /*
            case R.id.action_camera:
                Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(i);
                return true;
            */

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadAlbums();
                break;
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    StringUtils.showToast(AlbumsActivity.this, "i got NET");
                break;
        }
    }

    private void loadAlbums() {

        if (hidden) {
            albums.loadPreviewHiddenAlbums();
        } else
            albums.loadPreviewAlbums();


        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        adapt = new AlbumsAdapter(albums.dispAlbums, getApplicationContext());
        adapt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                adapt.notifyItemChanged(albums.toggleSelectAlbum(a.getTag().toString()));
                editmode = true;
                invalidateOptionsMenu();
                return true;
            }
        });

        adapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                if (editmode) {
                    adapt.notifyItemChanged(albums.toggleSelectAlbum(a.getTag().toString()));
                    invalidateOptionsMenu();
                } else {
                    Album album = albums.getAlbum(a.getTag().toString());
                    Intent intent = new Intent(AlbumsActivity.this, PhotosActivity.class);

                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                    Bundle b = new Bundle();
                    b.putParcelable("album", album);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapt);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (RVdecor) {
            mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 5, true));
            RVdecor = false;
        }
        adapt.notifyDataSetChanged();
        mRecyclerView.setBackgroundColor(getBackgroundColor());
    }

    private void hideViews() {
        fabCamera.animate().translationY(fabCamera.getHeight()*2/*+fabBottomMargin*/).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {
        //mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        fabCamera.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Albums Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.leafpic.app/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Albums Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.leafpic.app/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
