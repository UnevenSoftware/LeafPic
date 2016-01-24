package com.leafpic.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.leafpic.app.Adapters.AlbumsAdapter;
import com.leafpic.app.utils.string;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class AlbumsActivity extends AppCompatActivity {
    DatabaseHandler db = new DatabaseHandler(AlbumsActivity.this);
    HandlingAlbums albums = new HandlingAlbums(AlbumsActivity.this);

    boolean editmode = false, hidden = false;
    RecyclerView.Adapter mAdapter;
    RecyclerView mRecyclerView;
    AlbumsAdapter adapt;
    // MediaStoreObserver
    // observer;
    Toolbar toolbar;
    SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initUiTweaks();
        checkPermissions();
    }

    @Override
    public void onDestroy() {
        //getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        /*db.updatePhotos();
        albums.loadAlbums();
        adapt.notifyDataSetChanged();*/
        checkPermissions();
        //    adapt.notifyDataSetChanged();
        super.onResume();
    }

    public void initUiTweaks(){

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.toolbar));
            SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean NavBar = SP.getBoolean("nav_bar", false);
            //boolean NightTheme = SP.getBoolean("set_theme", false);
            if(NavBar==true)
                getWindow().setNavigationBarColor(getColor(R.color.toolbar));
        }

        /**** ToolBar*/
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Default").withIcon(FontAwesome.Icon.faw_picture_o);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Hidden").withIcon(FontAwesome.Icon.faw_eye_slash);
        PrimaryDrawerItem item21 = new PrimaryDrawerItem().withName("Map").withIcon(FontAwesome.Icon.faw_globe);
        PrimaryDrawerItem item22 = new PrimaryDrawerItem().withName("Calendar").withIcon(FontAwesome.Icon.faw_calendar_o);


        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("Settings").withIcon(FontAwesome.Icon.faw_cog);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName("GitHub").withIcon(FontAwesome.Icon.faw_github);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName("Donate").withIcon(FontAwesome.Icon.faw_gift);


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
                            case 1://deafult
                                hidden = false;
                                checkPermissions();
                                break;
                            case 2://hidden
                                hidden = true;
                                checkPermissions();
                                break;
                            case 6://settings
                                Intent intent = new Intent(AlbumsActivity.this, Preferences_Activity.class);
                                startActivity(intent);
                                break;
                            case 7://github
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
    }

    public  void checkPermissions(){

        if (ContextCompat.checkSelfPermission(AlbumsActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AlbumsActivity.this,
                    Manifest.permission.INTERNET))
                string.showToast(AlbumsActivity.this, "eddai dammi internet");
            else
                ActivityCompat.requestPermissions(AlbumsActivity.this,
                        new String[]{Manifest.permission.INTERNET}, 1);
        }

        if (ContextCompat.checkSelfPermission(AlbumsActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AlbumsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
                string.showToast(AlbumsActivity.this, "no storage permission");
            else
                ActivityCompat.requestPermissions(AlbumsActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else
            //got the power
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
        int c;// = albums.getSelectedCount();
        if ((c = albums.getSelectedCount()) != 0)
            setTitle("  " + c + "/" + albums.dispAlbums.size());
        else setTitle("LeafPic");
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
                string.showToast(getApplicationContext(), "asdasd");//NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.sort_action:
                View sort_btn = findViewById(R.id.sort_action);
                PopupMenu popup = new PopupMenu(AlbumsActivity.this, sort_btn);
                popup.setGravity(Gravity.AXIS_CLIP);

                popup.getMenuInflater()
                        .inflate(R.menu.sort, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                AlbumsActivity.this,
                                "You Clicked : " + item.getTitle(),
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
                AlertDialog.Builder dasdf = new AlertDialog.Builder(
                        new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                dasdf.setMessage(getString(R.string.exclude_album_message));
                dasdf.setCancelable(true);
                dasdf.setPositiveButton("EXCLUDE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        albums.excludeSelectedAlbums();
                        adapt.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }
                });
                dasdf.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dasdf.show();
                break;

            case R.id.deleteAction:
                AlertDialog.Builder dlg = new AlertDialog.Builder(
                        new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                dlg.setMessage(getString(R.string.delete_album_message));
                dlg.setCancelable(true);
                dlg.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        albums.deleteSelectedAlbums();
                        adapt.notifyDataSetChanged();
                        invalidateOptionsMenu();

                    }
                });
                dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dlg.show();
                break;

            case R.id.hideAlbumButton:
                if (hidden) {
                    albums.unHideSelectedAlbums();
                    adapt.notifyDataSetChanged();
                    invalidateOptionsMenu();
                } else {
                    AlertDialog.Builder dlg1 = new AlertDialog.Builder(
                            new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                    dlg1.setMessage(getString(R.string.hide_album_message));
                    dlg1.setPositiveButton("HIDE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            albums.hideSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                    dlg1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dlg1.setNeutralButton("EXCLUDE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            albums.excludeSelectedAlbums();
                            adapt.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                    dlg1.show();
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
                    string.showToast(AlbumsActivity.this, "i got NET");
                break;
        }
    }


    private void loadAlbums() {

        if (hidden)
            albums.loadPreviewHiddenAlbums();
        else {
            db.updatePhotos();
            albums.loadPreviewAlbums();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.gridAlbums);
        adapt = new AlbumsAdapter(albums.dispAlbums, R.layout.album_card);

        adapt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.picturetext);
                String s = a.getTag().toString();
                adapt.notifyItemChanged(albums.selectAlbum(s, true));
                editmode = true;
                invalidateOptionsMenu();
                return true;
            }
        });

        adapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.picturetext);
                String s = a.getTag().toString();
                Album album = albums.getAlbum(s);

                int pos;
                if (editmode) {
                    if (album.isSelected()) pos = albums.selectAlbum(s, false);
                    else pos = albums.selectAlbum(s, true);
                    adapt.notifyItemChanged(pos);
                    invalidateOptionsMenu();
                } else {
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

    }
}
