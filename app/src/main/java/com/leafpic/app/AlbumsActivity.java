package com.leafpic.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.leafpic.app.Adapters.AlbumsAdapter;
import com.leafpic.app.Base.Album;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Base.HiddenPhotosHandler;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialize.color.Material;

import java.io.File;

public class AlbumsActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback {

    HandlingAlbums albums = new HandlingAlbums(AlbumsActivity.this);
    RecyclerView mRecyclerView;
    AlbumsAdapter adapt;

    Toolbar toolbar;
    SharedPreferences SP;
    boolean editmode = false, hidden = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_albums);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initUiTweaks();
        checkPermissions();
        //APPINTRO TREAD
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

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        albums.clearSelectedAlbums();
        updateSelectedStuff();
        invalidateOptionsMenu();
        checkPermissions();

        initUiTweaks();

        super.onResume();
    }


    public void initUiTweaks(){
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        /**** Nav Bar ****/
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean NavBar = SP.getBoolean("nav_bar", false);
            if (NavBar)
                getWindow().setNavigationBarColor(getColor(R.color.primary));
        }
        /**** ToolBar *****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**** Status Bar */
        getWindow().setStatusBarColor(getColor(R.color.primary));
        //getWindow().setStatusBarColor(getColor(R.color.toolbar));

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.Relative_Album_layout);
        boolean darkTheme = SP.getBoolean("set_dark_theme", false);
        if (darkTheme==true){
            //setTheme(R.style.AppTheme_Dark);
            rl.setBackgroundColor(getColor(R.color.background_material_dark));
        }else {
            //setTheme(R.style.AppTheme);
            rl.setBackgroundColor(getColor(R.color.background_material_light));
        }

        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Default").withIcon(FontAwesome.Icon.faw_picture_o);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Hidden").withIcon(FontAwesome.Icon.faw_eye_slash);
        PrimaryDrawerItem item21 = new PrimaryDrawerItem().withName("Map").withIcon(FontAwesome.Icon.faw_globe);
        PrimaryDrawerItem item22 = new PrimaryDrawerItem().withName("Calendar").withIcon(FontAwesome.Icon.faw_calendar_o);

        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName("Settings").withIcon(FontAwesome.Icon.faw_cog);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withName("GitHub").withIcon(FontAwesome.Icon.faw_github);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withName("Donate").withIcon(FontAwesome.Icon.faw_gift);
        //SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("Settings").withIcon(FontAwesome.Icon.faw_cog);
        //SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName("GitHub").withIcon(FontAwesome.Icon.faw_github);
        //SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName("Donate").withIcon(FontAwesome.Icon.faw_gift);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.side_wall)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        new ProfileDrawerItem().withName(SP.getString("username", "NA")).withIcon(getDrawable(R.drawable.default_profile))
                )
                .build();

        final Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        item1,
                        item2,
                        item21,
                        item22,
                        new DividerDrawerItem(),
                        item3,
                        item4,
                        item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 1: //deafult
                                hidden = false;
                                checkPermissions();
                                break;
                            case 2: //hidden
                                hidden = true;
                                checkPermissions();
                                break;
                            case 6: //settings
                                //MyPreferenceFragment f = new MyPreferenceFragment();
                                // FragmentManager fragment = getFragmentManager();
                                Intent intent = new Intent(AlbumsActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                break;
                            case 7: //github
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DNLDsht/LeafPic/"));
                                startActivity(browserIntent);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                })
                .build();
        addHiddenFolder_FABEvent();
    }

    public void addHiddenFolder_FABEvent() {
        FloatingActionButton btnAddFolder = (FloatingActionButton) findViewById(R.id.fab_add_folder);

        if (hidden) {
            btnAddFolder.setVisibility(View.VISIBLE);
            int color = Color.parseColor(SP.getString("PrefColor", "#03A9F4"));

            btnAddFolder.setBackgroundTintList(ColorStateList.valueOf(color));
            btnAddFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FolderChooserDialog.Builder(AlbumsActivity.this)
                            .chooseButton(R.string.md_choose_label)
                            .initialPath(Environment.getExternalStorageDirectory().getPath())
                            .show();
                }
            });
        } else
            btnAddFolder.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFolderSelection(@NonNull File folder) {
        HiddenPhotosHandler h = new HiddenPhotosHandler(getApplicationContext());
        StringUtils.showToast(getApplicationContext(), folder.getAbsolutePath());
        h.addImagesFromFolder(folder);

        albums.loadPreviewHiddenAlbums();
        adapt.notifyDataSetChanged();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    public  void checkPermissions(){

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
            opt = menu.findItem(R.id.endEditAlbumMode);
            opt.setEnabled(true).setVisible(true);
            setOptionsAlbmuMenusItemsVisible(menu,true);
        } else {
            opt = menu.findItem(R.id.endEditAlbumMode);
            opt.setEnabled(false).setVisible(false);
            setOptionsAlbmuMenusItemsVisible(menu,false);
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


        if (albums.getSelectedCount()==0) {
            editmode = false;
            opt = menu.findItem(R.id.endEditAlbumMode);
            setOptionsAlbmuMenusItemsVisible(menu,false);
            opt.setEnabled(false).setVisible(false);
        }
        updateSelectedStuff();
        return super.onPrepareOptionsMenu(menu);
    }

    void updateSelectedStuff() {
        int c;
        if ((c = albums.getSelectedCount()) != 0)
            setTitle(c + "/" + albums.dispAlbums.size());
        else setTitle(getString(R.string.app_name));
    }

    private void setOptionsAlbmuMenusItemsVisible(final Menu m, boolean val) {
        MenuItem option = m.findItem(R.id.hideAlbumButton);
        option.setEnabled(val).setVisible(val);
        option = m.findItem(R.id.deleteAction);
        option.setEnabled(val).setVisible(val);
        option = m.findItem(R.id.excludeAlbumButton);
        option.setEnabled(val).setVisible(val);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                StringUtils.showToast(getApplicationContext(), "asdasd");
                return true;

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
            case R.id.endEditAlbumMode:
                editmode = false;
                invalidateOptionsMenu();
                albums.clearSelectedAlbums();
                adapt.notifyDataSetChanged();
                break;

            case R.id.excludeAlbumButton:
                new MaterialDialog.Builder(this)
                        .content(R.string.exclude_album_message)
                        .positiveText("EXCLUDE")
                        .negativeText("CANCEL")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                albums.excludeSelectedAlbums();
                                adapt.notifyDataSetChanged();
                                invalidateOptionsMenu();
                            }
                        })
                        .show();
                break;

            case R.id.deleteAction:
                new MaterialDialog.Builder(this)
                        .content(R.string.delete_album_message)
                        .positiveText("DELETE")
                        .negativeText("CANCEL")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                albums.deleteSelectedAlbums();
                                adapt.notifyDataSetChanged();
                                invalidateOptionsMenu();
                            }
                        })
                        .show();
                break;

            case R.id.hideAlbumButton:
                if (hidden) {
                    albums.unHideSelectedAlbums();
                    adapt.notifyDataSetChanged();
                    invalidateOptionsMenu();
                } else {
                    new MaterialDialog.Builder(this)
                            .content(R.string.hide_album_message)
                            .positiveText("HIDE")
                            .negativeText("CANCEL")
                            .neutralText("EXCLUDE")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    albums.hideSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    albums.excludeSelectedAlbums();
                                    adapt.notifyDataSetChanged();
                                    invalidateOptionsMenu();
                                }
                            })
                            .show();
                }
                break;

            case R.id.action_camera:
                Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(i);
                return true;

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
        addHiddenFolder_FABEvent();

        if (hidden) {
            //LOAD
            /*
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title("Hidden Albums")
                    .content("Scaning for hidden media...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(true);

            final MaterialDialog dialog = builder.build();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
            */
            albums.loadPreviewHiddenAlbums();
            //dialog.dismiss();
        } else
            albums.loadPreviewAlbums();


        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        adapt = new AlbumsAdapter(albums.dispAlbums, R.layout.album_card);
        adapt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                adapt.notifyItemChanged(albums.selectAlbum(a.getTag().toString(), true));
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
        adapt.notifyDataSetChanged();
    }
}
