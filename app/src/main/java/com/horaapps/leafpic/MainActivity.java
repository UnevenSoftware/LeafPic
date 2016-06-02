package com.horaapps.leafpic;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
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
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.horaapps.leafpic.Adapters.AlbumsAdapter;
import com.horaapps.leafpic.Adapters.PhotosAdapter;
import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.AlbumSettings;
import com.horaapps.leafpic.Base.CustomAlbumsHandler;
import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Base.ImageFileFilter;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.Views.GridSpacingItemDecoration;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.AffixMedia;
import com.horaapps.leafpic.utils.AffixOptions;
import com.horaapps.leafpic.utils.AlertDialogsHelper;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.Measure;
import com.horaapps.leafpic.utils.SecurityHelper;
import com.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class MainActivity extends ThemedActivity {

    public static String TAG = "AlbumsAct";

    CustomAlbumsHandler customAlbumsHandler = new CustomAlbumsHandler(MainActivity.this);
    SharedPreferences SP;
    SecurityHelper securityObj;

    HandlingAlbums albums;
    RecyclerView recyclerViewAlbums;
    AlbumsAdapter albumsAdapter;
    GridSpacingItemDecoration albumsDecoration;

    Album album;
    RecyclerView recyclerViewMedia;
    PhotosAdapter mediaAdapter;
    GridSpacingItemDecoration photosDecoration;

    FloatingActionButton fabCamera;
    DrawerLayout mDrawerLayout;
    Toolbar toolbar;
    SelectAlbumBottomSheet bottomSheetDialogFragment;
    SwipeRefreshLayout swipeRefreshLayout;

    boolean hidden = false, pickmode = false, editmode = false, albumsMode = true, firstLaunch = true;

    View.OnLongClickListener photosOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int index = Integer.parseInt(v.findViewById(R.id.photo_path).getTag().toString());
            if (!editmode) {
                // If it is the first long press
                mediaAdapter.notifyItemChanged(album.toggleSelectPhoto(index));
                editmode = true;
            } else
                album.selectAllPhotosUpTo(index, mediaAdapter);

            invalidateOptionsMenu();
            return true;
        }
    };

    View.OnClickListener photosOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                int index = Integer.parseInt(v.findViewById(R.id.photo_path).getTag().toString());
                if (!pickmode) {
                    if (editmode) {
                        mediaAdapter.notifyItemChanged(album.toggleSelectPhoto(index));
                        invalidateOptionsMenu();
                    } else {
                        album.setCurrentPhotoIndex(index);
                        Intent intent = new Intent(MainActivity.this, PhotoPagerActivity.class);
                        intent.setAction(PhotoPagerActivity.ACTION_OPEN_ALBUM);
                        startActivity(intent);
                    }
                } else {
                    setResult(RESULT_OK, new Intent().setData(album.getMedia(index).getUri()));
                    finish();
                }

        }
    };

    private View.OnLongClickListener albumOnLongCLickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int index = Integer.parseInt(v.findViewById(R.id.album_name).getTag().toString());
            albumsAdapter.notifyItemChanged(albums.toggleSelectAlbum(index));
            editmode = true;
            invalidateOptionsMenu();
            return true;
        }
    };

    private View.OnClickListener albumOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = Integer.parseInt(v.findViewById(R.id.album_name).getTag().toString());
            if (editmode) {
                albumsAdapter.notifyItemChanged(albums.toggleSelectAlbum(index));
                invalidateOptionsMenu();
            } else {
                albums.setCurrentAlbumIndex(index);
                displayCurrentAlbumMedia(true);
                setRecentApp(albums.getCurrentAlbum().getName());
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        albums = new HandlingAlbums(getApplicationContext());
        album = new Album();
        albumsMode = true;
        editmode = false;
        securityObj = new SecurityHelper(MainActivity.this);

        initUI();
        setupUI();

        displayPreFetchedData(getIntent().getExtras());
    }

    @Override
    public void onResume() {
        super.onResume();
        securityObj.updateSecuritySetting();
        setupUI();
        albums.clearSelectedAlbums();
        album.clearSelectedPhotos();
        if (SP.getBoolean("auto_update_media", false)) {
            if (albumsMode) { if (!firstLaunch) new PrepareAlbumTask().execute(); }
            else new PreparePhotosTask().execute();
        } else {
            albumsAdapter.notifyDataSetChanged();
            mediaAdapter.notifyDataSetChanged();
        }
        invalidateOptionsMenu();
        firstLaunch = false;
    }



    public void displayCurrentAlbumMedia(boolean reload) {
        album = ((MyApplication) getApplicationContext()).getCurrentAlbum();
        album.setSettings(getApplicationContext());
        toolbar.setTitle(album.getName());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (reload) {
            //display available medias before reload
            mediaAdapter.updateDataSet(album.media);
            new PreparePhotosTask().execute();
        }
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
        if (SP.getBoolean("auto_update_media", false))
            displayAlbums(true);
        else {
            displayAlbums(false);
            albumsAdapter.updateDataset(albums.dispAlbums);
            toggleRecyclersVisibilty(true);
        }
    }

    public void displayAlbums(boolean reload) {
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
        toolbar.setTitle(getString(R.string.app_name));
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (reload) new PrepareAlbumTask().execute();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mDrawerLayout.openDrawer(GravityCompat.START); }
        });

        albumsMode = true;
        editmode = false;
        invalidateOptionsMenu();
        mediaAdapter.updateDataSet(new ArrayList<Media>());
        recyclerViewMedia.scrollToPosition(0);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int nSpan;

        if (albumsMode) {
            nSpan = Measure.getAlbumsColumns(MainActivity.this);
            recyclerViewAlbums.setLayoutManager(new GridLayoutManager(this, nSpan));
            recyclerViewAlbums.removeItemDecoration(albumsDecoration);
            albumsDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
            recyclerViewAlbums.addItemDecoration(albumsDecoration);
        } else {
            nSpan = Measure.getPhotosColumns(MainActivity.this);
            recyclerViewMedia.setLayoutManager(new GridLayoutManager(this, nSpan));
            recyclerViewMedia.removeItemDecoration(photosDecoration);
            photosDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
            recyclerViewMedia.addItemDecoration(photosDecoration);
        }

        int status_height = Measure.getStatusBarHeight(getResources()),
        navBarHeight =  Measure.getNavBarHeight(MainActivity.this);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            recyclerViewAlbums.setPadding(0, 0, 0, status_height);
            recyclerViewMedia.setPadding(0, 0, 0, status_height);
            fabCamera.setVisibility(View.GONE);
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            toolbar.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();

            swipeRefreshLayout.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();
            recyclerViewAlbums.setPadding(0, 0, 0, status_height + navBarHeight);
            recyclerViewMedia.setPadding(0, 0, 0, status_height + navBarHeight);
            fabCamera.animate().translationY(fabCamera.getHeight() * 2).start();
            fabCamera.setVisibility(View.VISIBLE);
        }
    }

    public void displayPreFetchedData(Bundle data){
        try {
            if (data!=null) {
                int content = data.getInt(SplashScreen.CONTENT);
                if (content == SplashScreen.ALBUMS_PREFETCHED) {
                    albums = ((MyApplication) getApplicationContext()).getAlbums();
                    displayAlbums(false);
                    pickmode = data.getBoolean(SplashScreen.PICK_MODE);
                    albumsAdapter.updateDataset(albums.dispAlbums);
                    toggleRecyclersVisibilty(true);
                } else if (content == SplashScreen.PHOTS_PREFETCHED) {
                    albums = ((MyApplication) getApplicationContext()).getAlbums();
                    album = ((MyApplication) getApplicationContext()).getCurrentAlbum();
                    //TODO ask password if hidden
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            albums.loadPreviewAlbums(getApplicationContext(), album.isHidden());
                        }
                    }).start();
                    displayCurrentAlbumMedia(false);
                    mediaAdapter.updateDataSet(album.media);
                    toggleRecyclersVisibilty(false);

                }
            } else {
                albums = new HandlingAlbums(getApplicationContext());
                displayAlbums(true);
            }
        } catch (NullPointerException e) { e.printStackTrace(); }

    }

    public void initUI() {

        /**** TOOLBAR ****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**** RECYCLER VIEW ****/
        recyclerViewAlbums = (RecyclerView) findViewById(R.id.grid_albums);
        recyclerViewMedia = ((RecyclerView) findViewById(R.id.grid_photos));
        recyclerViewAlbums.setHasFixedSize(true);
        recyclerViewAlbums.setItemAnimator(new DefaultItemAnimator());
        recyclerViewMedia.setHasFixedSize(true);
        recyclerViewMedia.setItemAnimator(new DefaultItemAnimator());

        albumsDecoration = new GridSpacingItemDecoration(Measure.getAlbumsColumns(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);
        photosDecoration = new GridSpacingItemDecoration(Measure.getPhotosColumns(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);

        recyclerViewAlbums.addItemDecoration(albumsDecoration);
        recyclerViewMedia.addItemDecoration(photosDecoration);

        albumsAdapter = new AlbumsAdapter(albums.dispAlbums, MainActivity.this);
        recyclerViewAlbums.setLayoutManager(new GridLayoutManager(this, Measure.getAlbumsColumns(getApplicationContext())));

        albumsAdapter.setOnClickListener(albumOnClickListener);
        albumsAdapter.setOnLongClickListener(albumOnLongCLickListener);
        recyclerViewAlbums.setAdapter(albumsAdapter);

        mediaAdapter = new PhotosAdapter(album.media, MainActivity.this);
        recyclerViewMedia.setLayoutManager(new GridLayoutManager(this, Measure.getPhotosColumns(getApplicationContext())));
        mediaAdapter.setOnClickListener(photosOnClickListener);
        mediaAdapter.setOnLongClickListener(photosOnLongClickListener);
        recyclerViewMedia.setAdapter(mediaAdapter);

        /**** SWIPE TO REFRESH ****/
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getAccentColor());
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getBackgroundColor());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (albumsMode) {
                    albums.clearSelectedAlbums();
                    new PrepareAlbumTask().execute();
                } else {
                    album.clearSelectedPhotos();
                    new PreparePhotosTask().execute();
                }
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

        ((TextView) findViewById(R.id.txtLogo)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Figa.ttf"));

        /**** FAB ***/
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fabCamera.animate().translationY(-Measure.getNavBarHeight(MainActivity.this)).setInterpolator(new DecelerateInterpolator(2)).start();
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!albumsMode && album.areFiltersActive()) {
                    album.filterMedias(getApplicationContext(), ImageFileFilter.FILTER_ALL);
                    mediaAdapter.updateDataSet(album.media);
                    checkNothing();
                    toolbar.getMenu().findItem(R.id.all_media_filter).setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(MainActivity.this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                } else startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
*/
                //region TESTING
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 42);
                }

                //newFolderDialog();
                //endregion
            }
        });



        int statusBarHeight = Measure.getStatusBarHeight(getResources()),
            navBarHeight = Measure.getNavBarHeight(MainActivity.this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        toolbar.animate().translationY(statusBarHeight).setInterpolator(new DecelerateInterpolator()).start();

        swipeRefreshLayout.animate().translationY(statusBarHeight).setInterpolator(new DecelerateInterpolator()).start();

        recyclerViewAlbums.setPadding(0, 0, 0, statusBarHeight + navBarHeight);
        recyclerViewMedia.setPadding(0, 0, 0, statusBarHeight + navBarHeight);
        setRecentApp(getString(R.string.app_name));

        Display aa = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (aa.getRotation() == Surface.ROTATION_90) {//1
            Configuration configuration = new Configuration();
            configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
            onConfigurationChanged(configuration);
        }
    }
    ArrayAdapter<String> directoryList;

    //region TESTING
    private void newFolderDialog(final int value) {
        final File curFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        directoryList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout
                .simple_list_item_1, Arrays.asList(curFolder.list())){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view!=null){
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    text.setTextColor(getTextColor());
                }
                return view;
            }
        };

        final AlertDialog.Builder dialogExplorer = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
        final ListView dialogExplorerListView;
        View dialogExplorerLayout = getLayoutInflater().inflate(R.layout.dialog_explorer, null);

        final TextView textViewCurrentPath = (TextView) dialogExplorerLayout.findViewById(R.id.current_path);
        textViewCurrentPath.setTextColor(getTextColor());

        final RelativeLayout relativeLayoutTitle = (RelativeLayout) dialogExplorerLayout.findViewById(R.id.ll_explorer_dialog_title);
        relativeLayoutTitle.setBackgroundColor(getPrimaryColor());

        final LinearLayout linearCreateNewFolder = (LinearLayout) dialogExplorerLayout.findViewById(R.id.new_folder_layout);
        IconicsImageView imgFolder = (IconicsImageView) dialogExplorerLayout.findViewById(R.id.folder);
        imgFolder.setColor(getIconColor());

        final EditText editTextFolderName = (EditText) dialogExplorerLayout.findViewById(R.id.folder_name_edit_text);



        /**** Scrollbar *****/
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));


        IconicsImageView btnUP;
        btnUP = (IconicsImageView) dialogExplorerLayout.findViewById(R.id.directory_up);
        btnUP.setColor(getIconColor());

        IconicsImageView newFolder;
        newFolder = (IconicsImageView) dialogExplorerLayout.findViewById(R.id.toggle_create_new_folder_icon);

        newFolder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                linearCreateNewFolder.setVisibility(linearCreateNewFolder.getVisibility()==View.GONE ? View.VISIBLE : View.GONE);
            }
        });



        //((IconicsImageView) dialogExplorerLayout.findViewById(R.id.folder_icon)).setColor(getIconColor());

        dialogExplorerListView = (ListView) dialogExplorerLayout.findViewById(R.id.folder_list);
        btnUP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                File current = new File(textViewCurrentPath.getText().toString());
                Log.wtf(TAG,current.getAbsolutePath());
                if(current.isDirectory() && !(textViewCurrentPath.getText().toString().equals("/")) ) {
                    directoryList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout
                            .simple_list_item_1, HandlingAlbums.getSubFolders(current.getParentFile())){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (view!=null){
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(getTextColor());
                            }
                            return view;
                        }
                    };
                    textViewCurrentPath.setText(current.getParentFile().getAbsolutePath());
                    dialogExplorerListView.setAdapter(directoryList);
                }
            }
        });
        dialogExplorer.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(linearCreateNewFolder.getVisibility()==View.VISIBLE && !editTextFolderName.getText().equals("")) {
                    String path = textViewCurrentPath.getText().toString();
                    File folder = new File(path + File.separator + editTextFolderName.getText().toString());
                    boolean success = true;
                    if (!folder.exists())
                        success = folder.mkdir();
                    if (success) {
                        if(value==1){
                            album.copySelectedPhotos(getApplicationContext(), path + File.separator + editTextFolderName.getText().toString() + File.separator);
                            finishEditMode();
                        } else { new MovePhotos().execute(path + File.separator + editTextFolderName.getText().toString() + File.separator);}
                    } else {
                        Toast.makeText(MainActivity.this, "Folder Already Exist!", Toast.LENGTH_SHORT).show();
                    }
                }
                //String path = textViewCurrentPath.getText().toString();
                //Toast.makeText(MainActivity.this, path, Toast.LENGTH_SHORT).show();
            }
        });
        dialogExplorer.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        /*deleteDialog.setNeutralButton(R.string.new_folder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String path = textViewCurrentPath.getText().toString();
                Toast.makeText(MainActivity.this, "new folder", Toast.LENGTH_SHORT).show();
            }
        });*/

        dialogExplorerListView.setAdapter(directoryList);
        textViewCurrentPath.setText(curFolder.getAbsolutePath());
        dialogExplorer.setView(dialogExplorerLayout);

        dialogExplorerListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                File selected = new File(textViewCurrentPath.getText()+"/"+ directoryList.getItem(position));

                Log.wtf("asd",selected.isDirectory()+ " - " + selected.getAbsolutePath());
                if(selected.isDirectory()){
                directoryList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout
                        .simple_list_item_1, HandlingAlbums.getSubFolders(selected)) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        if (view!=null){
                            TextView text = (TextView) view.findViewById(android.R.id.text1);
                            text.setTextColor(getTextColor());
                        }
                        return view;
                    }
                };
                textViewCurrentPath.setText(selected.getAbsolutePath());
                dialogExplorerListView.setAdapter(directoryList);
                } else {
                    Toast.makeText(getApplicationContext(), selected.toString() + "selected ", Toast.LENGTH_SHORT).show();
                    //dialog.dismiss();
                }
            }
        });
        dialogExplorer.show();


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == 42) {
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = resultData.getData();

                // Persist URI in shared preference so that you can use it later.
                // Use your own framework here instead of PreferenceUtil.
                //PreferenceUtil.setSharedPreferenceUri(R.string.key_internal_uri_extsdcard,
                  //      treeUri);

                // Persist access permissions.
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            }
        }
    }
    //endregion

    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isNavigationBarColored())
                super.setNavBarColor();
            else
                getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(
                        ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 110));
        }
    }

    //region UI/GRAPHIC
    public void setupUI() {
        //TODO: MUST BE FIXXED
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());

        /**** SWIPE TO REFRESH ****/
        swipeRefreshLayout.setColorSchemeColors(getAccentColor());
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getBackgroundColor());

        setStatusBarColor();
        setNavBarColor();

        fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        setDrawerTheme();
        recyclerViewAlbums.setBackgroundColor(getBackgroundColor());
        recyclerViewMedia.setBackgroundColor(getBackgroundColor());
        mediaAdapter.updatePlaceholder(getApplicationContext(), getBasicTheme());

        /**** DRAWER ****/
        setScrollViewColor((ScrollView) findViewById(R.id.drawer_scrollbar));

        /**** recyclers drawable *****/
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));
    }

    public void setDrawerTheme() {
        RelativeLayout DrawerHeader = (RelativeLayout) findViewById(R.id.Drawer_Header);
        DrawerHeader.setBackgroundColor(getPrimaryColor());

        LinearLayout DrawerBody = (LinearLayout) findViewById(R.id.Drawer_Body);
        DrawerBody.setBackgroundColor(getDrawerBackground());//getBackgroundColor()

        ScrollView DrawerScroll = (ScrollView) findViewById(R.id.drawer_scrollbar);
        DrawerScroll.setBackgroundColor(getDrawerBackground());//getBackgroundColor()

        View DrawerDivider2 = findViewById(R.id.Drawer_Body_Divider);
        DrawerDivider2.setBackgroundColor(getIconColor());

        /** drawer items **/
        TextView txtDD = (TextView) findViewById(R.id.Drawer_Default_Item);
        //TextView txtDH = (TextView) findViewById(R.id.Drawer_Tags_Item);
        //TextView txtDMoments = (TextView) findViewById(R.id.Drawer_Moments_Item);
        TextView txtDS = (TextView) findViewById(R.id.Drawer_Setting_Item);
        TextView txtDDonate = (TextView) findViewById(R.id.Drawer_Donate_Item);
        TextView txtWall = (TextView) findViewById(R.id.Drawer_wallpapers_Item);
        TextView txtAbout = (TextView) findViewById(R.id.Drawer_About_Item);
        TextView txtHidden = (TextView) findViewById(R.id.Drawer_hidden_Item);

        IconicsImageView imgDD = (IconicsImageView) findViewById(R.id.Drawer_Default_Icon);
        IconicsImageView imgWall = (IconicsImageView) findViewById(R.id.Drawer_wallpapers_Icon);
        //IconicsImageView imgDH = (IconicsImageView) findViewById(R.id.Drawer_Tags_Icon);
        //IconicsImageView imgDMoments = (IconicsImageView) findViewById(R.id.Drawer_Moments_Icon);
        IconicsImageView imgDDonate = (IconicsImageView) findViewById(R.id.Drawer_Donate_Icon);
        IconicsImageView imgDS = (IconicsImageView) findViewById(R.id.Drawer_Setting_Icon);
        IconicsImageView imgAbout = (IconicsImageView) findViewById(R.id.Drawer_About_Icon);
        IconicsImageView imgHidden = (IconicsImageView) findViewById(R.id.Drawer_hidden_Icon);

        /**textViews Colors*/
        int color = getTextColor();
        txtDD.setTextColor(color);
        //txtDH.setTextColor(color);
        //txtDMoments.setTextColor(color);
        txtDS.setTextColor(color);
        txtDDonate.setTextColor(color);
        txtWall.setTextColor(color);
        txtAbout.setTextColor(color);
        txtHidden.setTextColor(color);

        color = getIconColor();

        imgDD.setColor(color);
        imgDDonate.setColor(color);
        //imgDH.setColor(color);
        //imgDMoments.setColor(color);
        imgDS.setColor(color);
        imgWall.setColor(color);
        imgAbout.setColor(color);
        imgHidden.setColor(color);

        /****DRAWER CLICK LISTENER****/
        findViewById(R.id.ll_drawer_Donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DonateActivity.class);
                startActivity(intent);
            }
        });
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

        findViewById(R.id.ll_drawer_Default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidden = false;
                mDrawerLayout.closeDrawer(GravityCompat.START);
                new PrepareAlbumTask().execute();
            }
        });
        findViewById(R.id.ll_drawer_hidden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (securityObj.isActiveSecurity() && securityObj.isPasswordOnHidden()){

                    AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder (MainActivity.this, getDialogStyle());

                    final EditText editTextPassword = securityObj.getInsertPasswordDialog(MainActivity.this, passwordDialogBuilder);

                    passwordDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

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
                                hidden = true;
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                new PrepareAlbumTask().execute();
                                passwordDialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                editTextPassword.getText().clear();
                                editTextPassword.requestFocus();
                            }
                        }
                    });
                } else {
                    hidden = true;
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    new PrepareAlbumTask().execute();
                }
            }
        });

        /*
        findViewById(R.id.ll_drawer_Moments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comingSoonDialog("Moments");
            }
        });

        findViewById(R.id.ll_drawer_Tags).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comingSoonDialog("Tags");
            }
        });
        */
        findViewById(R.id.ll_drawer_Wallpapers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comingSoonDialog("Wallpapers");
            }
        });
    }
    //endregion


    void comingSoonDialog(String title) {
        AlertDialog.Builder comingSoonDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
        AlertDialogsHelper.getTextDialog(this, comingSoonDialog, title, getString(R.string.coming_soon));
        comingSoonDialog.setPositiveButton(this.getString(R.string.ok_action), null);
        comingSoonDialog.show();
    }

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
                            albumsAdapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                    toolbar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (albums.getSelectedCount() == albums.dispAlbums.size())
                                albums.clearSelectedAlbums();
                            else albums.selectAllAlbums();
                            albumsAdapter.notifyDataSetChanged();
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
            }  else {
                if ((c = album.getSelectedCount()) != 0) {
                    toolbar.setTitle(c + "/" + album.media.size());
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
                            if (album.getSelectedCount() == album.media.size())
                                album.clearSelectedPhotos();
                            else album.selectAllPhotos();
                            mediaAdapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }
                    });
                } else {
                    toolbar.setTitle(album.getName());
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
        if (albumsMode) {
            albums.clearSelectedAlbums();
            albumsAdapter.notifyDataSetChanged();
        } else {
            album.clearSelectedPhotos();
            mediaAdapter.notifyDataSetChanged();
        }
        invalidateOptionsMenu();
    }

    public void checkNothing() {
        TextView a = (TextView) findViewById(R.id.nothing_to_show);
        a.setTextColor(getTextColor());
        a.setVisibility((albumsMode && albums.dispAlbums.size() == 0) || (!albumsMode && album.media.size() == 0) ? View.VISIBLE : View.GONE);
    }

    //region MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_albums, menu);

        if (albumsMode) {
            menu.findItem(R.id.select_all).setTitle(
                    getString(albums.getSelectedCount() == albumsAdapter.getItemCount()
                            ? R.string.clear_selected
                            : R.string.select_all));
            menu.findItem(R.id.ascending_sort_action).setChecked(albums.isAscending());
            switch (albums.getColumnSortingMode()) {
                case AlbumSettings.SORT_BY_NAME:  menu.findItem(R.id.name_sort_action).setChecked(true); break;
                case AlbumSettings.SORT_BY_SIZE:  menu.findItem(R.id.size_sort_action).setChecked(true); break;
                case AlbumSettings.SORT_BY_DATE:
                    default:
                        menu.findItem(R.id.date_taken_sort_action).setChecked(true);
                        break;
            }

        } else {
            menu.findItem(R.id.select_all).setTitle(getString(
                    album.getSelectedCount() == mediaAdapter.getItemCount()
                            ? R.string.clear_selected
                            : R.string.select_all));
            menu.findItem(R.id.ascending_sort_action).setChecked(album.settings.ascending);
            switch (album.settings.columnSortingMode) {
                case AlbumSettings.SORT_BY_NAME:  menu.findItem(R.id.name_sort_action).setChecked(true); break;
                case AlbumSettings.SORT_BY_SIZE:  menu.findItem(R.id.size_sort_action).setChecked(true); break;
                case AlbumSettings.SORT_BY_DATE:
                default:
                    menu.findItem(R.id.date_taken_sort_action).setChecked(true);
                    break;
            }
        }
        menu.findItem(R.id.hideAlbumButton).setTitle(hidden ? getString(R.string.unhide) : getString(R.string.hide));
        menu.findItem(R.id.search_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_search));
        menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        menu.findItem(R.id.sort_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_sort));
        menu.findItem(R.id.filter_menu).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_filter_list));
        menu.findItem(R.id.sharePhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));

        final MenuItem searchItem = menu.findItem(R.id.search_action);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.coming_soon));

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

        menu.findItem(R.id.select_all).setVisible(editmode);
        menu.findItem(R.id.installShortcut).setVisible(albumsMode && editmode);
        menu.findItem(R.id.delete_action).setVisible((albumsMode && editmode) || (!albumsMode));
        menu.findItem(R.id.setAsAlbumPreview).setVisible(!albumsMode && album.getSelectedCount() == 1);
        menu.findItem(R.id.clear_album_preview).setVisible(!albumsMode && album.hasCustomCover());
        menu.findItem(R.id.renameAlbum).setVisible((albumsMode && albums.getSelectedCount() == 1) || (!albumsMode && !editmode));
        menu.findItem(R.id.affixPhoto).setVisible(!albumsMode && album.getSelectedCount() > 1);
        return super.onPrepareOptionsMenu(menu);
    }

    private void togglePrimaryToolbarOptions(final Menu menu) {
        menu.setGroupVisible(R.id.general_action, !editmode);

        if (!editmode) {
            menu.findItem(R.id.filter_menu).setVisible(!albumsMode);
            menu.findItem(R.id.search_action).setVisible(albumsMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.select_all:
                if (albumsMode) {
                    if (albums.getSelectedCount() == albumsAdapter.getItemCount()) {
                        editmode = false;
                        albums.clearSelectedAlbums();
                    } else albums.selectAllAlbums();
                    albumsAdapter.notifyDataSetChanged();
                } else {
                    if (album.getSelectedCount() == mediaAdapter.getItemCount()) {
                        editmode = false;
                        album.clearSelectedPhotos();
                    } else album.selectAllPhotos();
                    mediaAdapter.notifyDataSetChanged();
                }
                invalidateOptionsMenu();
                return true;

            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                return true;

            case R.id.installShortcut:
                albums.installShortcutForSelectedAlbums(this.getApplicationContext());
                finishEditMode();
                return true;

            case R.id.hideAlbumButton:
                final AlertDialog.Builder hideDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

                AlertDialogsHelper.getTextDialog(MainActivity.this,hideDialogBuilder,
                        getString(hidden ? R.string.unhide : R.string.hide),
                        getString(hidden ? R.string.unhide_album_message : R.string.hide_album_message));

                hideDialogBuilder.setPositiveButton(getString(hidden ? R.string.unhide : R.string.hide), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (albumsMode) {
                            if (hidden) albums.unHideSelectedAlbums(getApplicationContext());
                            else albums.hideSelectedAlbums(getApplicationContext());
                            albumsAdapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        } else {
                            if(hidden) albums.unHideAlbum(album.getPath(), getApplicationContext());
                            else albums.hideAlbum(album.getPath(), getApplicationContext());
                            displayAlbums(true);
                        }
                    }
                });
                if (!hidden) {
                    hideDialogBuilder.setNeutralButton(this.getString(R.string.exclude), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (albumsMode) {
                                albums.excludeSelectedAlbums(getApplicationContext());
                                albumsAdapter.notifyDataSetChanged();
                                invalidateOptionsMenu();
                            } else {
                                customAlbumsHandler.excludeAlbum(album.getPath());
                                displayAlbums(true);
                            }
                        }
                    });
                }
                hideDialogBuilder.setNegativeButton(this.getString(R.string.cancel), null);
                hideDialogBuilder.show();
                return true;

            case R.id.delete_action:
                class DeletePhotos extends AsyncTask<String, Integer, Void> {
                    @Override
                    protected void onPreExecute() {
                        swipeRefreshLayout.setRefreshing(true);
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(String... arg0) {
                        if (albumsMode)
                            albums.deleteSelectedAlbums(MainActivity.this);
                        else {
                            if (editmode)
                                album.deleteSelectedMedia(getApplicationContext());
                            else {
                                albums.deleteAlbum(album, getApplicationContext());
                                album.media.clear();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        if (albumsMode) {
                            albums.clearSelectedAlbums();
                            albumsAdapter.notifyDataSetChanged();
                        } else {
                            if (album.media.size() == 0) {
                                albums.removeCurrentAlbum();
                                albumsAdapter.notifyDataSetChanged();
                                displayAlbums();
                            }
                            else
                                mediaAdapter.updateDataSet(album.media);
                        }
                        invalidateOptionsMenu();
                        checkNothing();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                AlertDialogsHelper.getTextDialog(this, deleteDialog, getString(R.string.delete), getString(albumsMode || (!albumsMode && !editmode) ? R.string.delete_album_message : R.string.delete_photos_message));

                deleteDialog.setNegativeButton(this.getString(R.string.cancel), null);
                deleteDialog.setPositiveButton(this.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (securityObj.isActiveSecurity() && securityObj.isPasswordOnDelete()) {
                            AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                            final EditText editTextPassword  = securityObj.getInsertPasswordDialog(MainActivity.this,passwordDialogBuilder);
                            passwordDialogBuilder.setNegativeButton(getString(R.string.cancel), null);

                            passwordDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //This should br empty it will be overwrite later
                                    //to avoid dismiss of the dialog on wrong password
                                }
                            });

                            final AlertDialog passwordDialog = passwordDialogBuilder.create();
                            passwordDialog.show();

                            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (securityObj.checkPassword(editTextPassword.getText().toString())) {
                                        passwordDialog.dismiss();
                                        new DeletePhotos().execute();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                        editTextPassword.getText().clear();
                                        editTextPassword.requestFocus();
                                    }
                                }
                            });
                        } else new DeletePhotos().execute();
                    }
                });
                deleteDialog.show();

                return true;
            case R.id.excludeAlbumButton:

                final AlertDialog.Builder excludeDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                /*AlertDialogsHelper.getTextDialog(MainActivity.this, excludeDialogBuilder,
                        getString(R.string.exclude),getString(R.string.exclude_album_message));*/

                final View excludeDialogLayout = getLayoutInflater().inflate(R.layout.exclude_dialog, null);
                TextView textViewExcludeTitle = (TextView) excludeDialogLayout.findViewById(R.id.text_dialog_title);
                TextView textViewExcludeMessage = (TextView) excludeDialogLayout.findViewById(R.id.text_dialog_message);
                final Spinner spinnerParents = (Spinner) excludeDialogLayout.findViewById(R.id.parents_folder);


                ((CardView) excludeDialogLayout.findViewById(R.id.message_card)).setCardBackgroundColor(getCardBackgroundColor());
                textViewExcludeTitle.setBackgroundColor(getPrimaryColor());
                textViewExcludeTitle.setText(getString(R.string.exclude));

                if(albumsMode && albums.getSelectedCount() > 1) {
                    textViewExcludeMessage.setText(R.string.exclude_albums_message);
                    spinnerParents.setVisibility(View.GONE);
                } else {

                    textViewExcludeMessage.setText(R.string.exclude_album_message);
                    spinnerParents.setAdapter(getSpinnerAdapter(albumsMode ? albums.getSelectedAlbum(0).getParentsFolders() : album.getParentsFolders()));
                }

                textViewExcludeMessage.setTextColor(getTextColor());
                excludeDialogBuilder.setView(excludeDialogLayout);

                excludeDialogBuilder.setPositiveButton(this.getString(R.string.exclude), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (albumsMode && albums.getSelectedCount() > 1) {
                            albums.excludeSelectedAlbums(getApplicationContext());
                            albumsAdapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        } else {
                            StringUtils.showToast(getApplicationContext(), spinnerParents.getSelectedItem().toString());
                            customAlbumsHandler.excludeAlbum(spinnerParents.getSelectedItem().toString());
                            albums.loadExcludedFolders(getApplicationContext());
                            displayAlbums(true);
                        }
                    }
                });
                excludeDialogBuilder.setNegativeButton(this.getString(R.string.cancel), null);
                excludeDialogBuilder.show();
                return true;

            case R.id.sharePhotos:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sent_to_action));

                ArrayList<Uri> files = new ArrayList<Uri>();
                for (Media f : album.selectedMedias)
                    files.add(f.getUri());

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setType(StringUtils.getGenericMIME(album.selectedMedias.get(0).getMIME()));
                finishEditMode();
                startActivity(intent);
                return true;

            case R.id.all_media_filter:
                if (!albumsMode) {
                    album.filterMedias(getApplicationContext(), ImageFileFilter.FILTER_ALL);
                    mediaAdapter.updateDataSet(album.media);
                    item.setChecked(true);
                    checkNothing();
                    //TODO improve
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                }
                return true;

            case R.id.video_media_filter:
                if (!albumsMode) {
                    album.filterMedias(getApplicationContext(), ImageFileFilter.FILTER_VIDEO);
                    mediaAdapter.updateDataSet(album.media);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                return true;

            case R.id.image_media_filter:
                if (!albumsMode) {
                    album.filterMedias(getApplicationContext(), ImageFileFilter.FILTER_IMAGES);
                    mediaAdapter.updateDataSet(album.media);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                return true;

            case R.id.gifs_media_filter:
                if (!albumsMode) {
                    album.filterMedias(getApplicationContext(), ImageFileFilter.FILTER_GIFS);
                    mediaAdapter.updateDataSet(album.media);
                    item.setChecked(true);
                    checkNothing();
                    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                }
                return true;

            case R.id.name_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(AlbumSettings.SORT_BY_NAME);
                    albums.sortAlbums(getApplicationContext());
                    albumsAdapter.updateDataset(albums.dispAlbums);
                } else {
                    album.setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_NAME);
                    album.sortPhotos();
                    mediaAdapter.updateDataSet(album.media);
                }
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(AlbumSettings.SORT_BY_DATE);
                    albums.sortAlbums(getApplicationContext());
                    albumsAdapter.updateDataset(albums.dispAlbums);
                } else {
                    album.setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_DATE);
                    album.sortPhotos();
                    mediaAdapter.updateDataSet(album.media);
                }
                item.setChecked(true);
                return true;

            case R.id.size_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingMode(AlbumSettings.SORT_BY_SIZE);
                    albums.sortAlbums(getApplicationContext());
                    albumsAdapter.updateDataset(albums.dispAlbums);
                } else {
                    album.setDefaultSortingMode(getApplicationContext(),AlbumSettings.SORT_BY_SIZE);
                    album.sortPhotos();
                    mediaAdapter.updateDataSet(album.media);
                }
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_action:
                if (albumsMode) {
                    albums.setDefaultSortingAscending(!item.isChecked());
                    albums.sortAlbums(getApplicationContext());
                    albumsAdapter.updateDataset(albums.dispAlbums);
                } else {
                    album.setDefaultSortingAscending(getApplicationContext(), !item.isChecked());
                    album.sortPhotos();
                    mediaAdapter.updateDataSet(album.media);
                }
                item.setChecked(!item.isChecked());
                return true;

            //region Affix
            case  R.id.affixPhoto:

                final AlertDialog.Builder AffixDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                final View dialogLayout = getLayoutInflater().inflate(R.layout.affix_dialog, null);

                dialogLayout.findViewById(R.id.affix_title).setBackgroundColor(getPrimaryColor());
                ((CardView) dialogLayout.findViewById(R.id.affix_card)).setCardBackgroundColor(getCardBackgroundColor());

                //ITEMS
                final SwitchCompat swVertical = (SwitchCompat) dialogLayout.findViewById(R.id.affix_vertical_switch);
                final SwitchCompat swSaveHere = (SwitchCompat) dialogLayout.findViewById(R.id.save_here_switch);

                final RadioGroup radioFormatGroup = (RadioGroup) dialogLayout.findViewById(R.id.radio_format);
                final RadioButton radio_jpg = (RadioButton) dialogLayout.findViewById(R.id.radio_jpeg);
                final RadioButton radio_png = (RadioButton) dialogLayout.findViewById(R.id.radio_png);
                final RadioButton radio_webp = (RadioButton) dialogLayout.findViewById(R.id.radio_webp);

                final TextView txtQuality = (TextView) dialogLayout.findViewById(R.id.affix_quality_title);
                final SeekBar seekQuality = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_quality);

                final ScrollView scrollView = (ScrollView) dialogLayout.findViewById(R.id.affix_scrollView);
                setScrollViewColor(scrollView);

                /** TextViews **/
                int color = getTextColor();
                ((TextView) dialogLayout.findViewById(R.id.affix_vertical_title)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.compression_settings_title)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.save_here_title)).setTextColor(color);

                /** Sub TextViews **/
                color = getTextColor();
                ((TextView) dialogLayout.findViewById(R.id.save_here_sub)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_vertical_sub)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_format_sub)).setTextColor(color);
                txtQuality.setTextColor(color);

                /** Icons **/
                color = getIconColor();
                ((IconicsImageView) dialogLayout.findViewById(R.id.affix_quality_icon)).setColor(color);
                ((IconicsImageView) dialogLayout.findViewById(R.id.affix_format_icon)).setColor(color);
                ((IconicsImageView) dialogLayout.findViewById(R.id.affix_vertical_icon)).setColor(color);
                ((IconicsImageView) dialogLayout.findViewById(R.id.save_here_icon)).setColor(color);

                seekQuality.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
                seekQuality.getThumb().setColorFilter(new PorterDuffColorFilter(getAccentColor(),PorterDuff.Mode.SRC_IN));

                seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtQuality.setText(Html.fromHtml(
                                String.format(Locale.getDefault(), "%s <b>%d</b>", getString(R.string.quality), progress)));
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                seekQuality.setProgress(90); //DEFAULT


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    radio_jpg.setButtonTintList(getRadioButtonColor());
                    radio_png.setButtonTintList(getRadioButtonColor());
                    radio_webp.setButtonTintList(getRadioButtonColor());
                    radio_jpg.setTextColor(getTextColor());
                    radio_png.setTextColor(getTextColor());
                    radio_webp.setTextColor(getTextColor());
                }

                //SWITCH
                updateSwitchColor(swVertical, getAccentColor());
                updateSwitchColor(swSaveHere, getAccentColor());

                swVertical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        updateSwitchColor(swVertical, getAccentColor());
                    }
                });

                swSaveHere.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        updateSwitchColor(swSaveHere, getAccentColor());
                    }
                });

                //Affixing On Background//
                class affixMedia extends AsyncTask<String, Integer, Void> {
                    AlertDialog dialog;
                    @Override
                    protected void onPreExecute() {
                        dialog = ProgressDialog(getString(R.string.affix), getString(R.string.affix_text));
                        dialog.show();
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(String... arg0) {
                        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
                        for (int i=0;i<album.getSelectedCount();i++){
                            if(!album.selectedMedias.get(i).isVideo())
                                bitmapArray.add(album.selectedMedias.get(i).getBitmap());
                        }

                        if (bitmapArray.size()>1) {

                            Bitmap.CompressFormat compressFormat;
                            switch (radioFormatGroup.getCheckedRadioButtonId()) {
                                case R.id.radio_jpeg: default:
                                    compressFormat = Bitmap.CompressFormat.JPEG; break;
                                case R.id.radio_png:
                                    compressFormat = Bitmap.CompressFormat.PNG; break;
                                case R.id.radio_webp:
                                    compressFormat = Bitmap.CompressFormat.WEBP; break;
                            }

                            AffixOptions options = new AffixOptions(
                                    swSaveHere.isChecked() ? album.getPath() : AffixMedia.getDefaultDirectoryPath(),
                                    compressFormat,
                                    seekQuality.getProgress(),
                                    swVertical.isChecked());

                            AffixMedia.AffixBitmapList(getApplicationContext(), bitmapArray, options);

                        } else {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run(){ Toast.makeText(getApplicationContext(),R.string.affix_error,Toast.LENGTH_SHORT).show(); }
                            });
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void result) {
                        editmode = false;
                        album.clearSelectedPhotos();
                        dialog.dismiss();
                        invalidateOptionsMenu();
                        mediaAdapter.notifyDataSetChanged();
                        //new PreparePhotosTask().execute();
                    }
                }
                //Dialog Buttons
                AffixDialog.setView(dialogLayout);
                AffixDialog.setPositiveButton(this.getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {new affixMedia().execute();}});
                AffixDialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}});
                AffixDialog.show();
                return true;
            //endregion

            case R.id.action_move:
                bottomSheetDialogFragment = new SelectAlbumBottomSheet();
                bottomSheetDialogFragment.setCurrentPath(album.getPath());
                bottomSheetDialogFragment.setTitle(getString(R.string.move_to));
                bottomSheetDialogFragment.setHidden(hidden);
                bottomSheetDialogFragment.setAlbumArrayList(albums.dispAlbums);
                bottomSheetDialogFragment.setOnClickListenerNewFolder(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialogFragment.dismiss();
                        newFolderDialog(2);
                    }
                });
                bottomSheetDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = v.findViewById(R.id.title_bottom_sheet_item).getTag().toString();
                        new MovePhotos().execute(path);
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
               return true;

            case R.id.action_copy:
                bottomSheetDialogFragment = new SelectAlbumBottomSheet();
                bottomSheetDialogFragment.setCurrentPath(album.getPath());
                bottomSheetDialogFragment.setTitle(getString(R.string.copy_to));
                bottomSheetDialogFragment.setAlbumArrayList(albums.dispAlbums);
                bottomSheetDialogFragment.setHidden(hidden);
                bottomSheetDialogFragment.setOnClickListenerNewFolder(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialogFragment.dismiss();
                        newFolderDialog(1);
                    }
                });
                bottomSheetDialogFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = v.findViewById(R.id.title_bottom_sheet_item).getTag().toString();
                        album.copySelectedPhotos(getApplicationContext(), path);
                        finishEditMode();
                        bottomSheetDialogFragment.dismiss();
                    }
                });
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                return true;

            case R.id.renameAlbum:
                AlertDialog.Builder renameDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(albumsMode ? albums.getSelectedAlbum(0).getName() : album.getName());

                AlertDialogsHelper.getInsertTextDialog(MainActivity.this, renameDialogBuilder,
                        editTextNewName, getString(R.string.rename_album));

                renameDialogBuilder.setNegativeButton(getString(R.string.cancel), null);

                renameDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //This should br empty it will be overwrite later
                        //to avoid dismiss of the dialog on wrong password
                    }
                });
                final AlertDialog renameDialog = renameDialogBuilder.create();
                renameDialog.show();

                renameDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View dialog) {
                        if (editTextNewName.length() != 0) {
                            if (albumsMode){
                                int index = albums.dispAlbums.indexOf(albums.getSelectedAlbum(0));
                                albums.getAlbum(index).updatePhotos(getApplicationContext());
                                albums.getAlbum(index).renameAlbum(getApplicationContext(), editTextNewName.getText().toString());
                                albumsAdapter.notifyItemChanged(index);
                            } else {
                                album.renameAlbum(getApplicationContext(), editTextNewName.getText().toString());
                                toolbar.setTitle(album.getName());
                                mediaAdapter.notifyDataSetChanged();
                            }
                            renameDialog.dismiss();
                        } else {
                            StringUtils.showToast(getApplicationContext(), getString(R.string.insert_something));
                            editTextNewName.requestFocus();
                        }
                    }});
                return true;

            case R.id.clear_album_preview:
                if (!albumsMode) {
                    CustomAlbumsHandler as = new CustomAlbumsHandler(getApplicationContext());
                    as.clearAlbumPreview(album.getPath());
                    album.setSettings(getApplicationContext());
                }
                return true;

            case R.id.setAsAlbumPreview:
                if (!albumsMode) {
                    album.setSelectedPhotoAsPreview(getApplicationContext());
                    finishEditMode();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void toggleRecyclersVisibilty(boolean albumsMode){
            recyclerViewAlbums.setVisibility(albumsMode ? View.VISIBLE : View.GONE);
            recyclerViewMedia.setVisibility(albumsMode ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (editmode) finishEditMode();
        else {
            if (albumsMode) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                else finish();
            } else {
                displayAlbums();
                setRecentApp(getString(R.string.app_name));
            }
        }
    }

    public AlertDialog.Builder progressDialog;
    public AlertDialog ProgressDialog(String dialogTitle, String dialogText){
        progressDialog = new AlertDialog.Builder(MainActivity.this,getDialogStyle());
        return AlertDialogsHelper.getProgressDialog(this, progressDialog, dialogTitle, dialogText);
    }

    public class PrepareAlbumTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            toggleRecyclersVisibilty(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            albums.loadPreviewAlbums(getApplicationContext(), hidden);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            albumsAdapter.updateDataset(albums.dispAlbums);
            checkNothing();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class PreparePhotosTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            toggleRecyclersVisibilty(false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            album.updatePhotos(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mediaAdapter.updateDataSet(album.media);
            checkNothing();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //MOVE SELECTED MEDIA ASYNCTASK
    class MovePhotos extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                for (int i = 0; i < album.selectedMedias.size(); i++) {
                    File from = new File(album.selectedMedias.get(i).getPath());
                    File to = new File(StringUtils.getPhotoPathMoved(album.selectedMedias.get(i).getPath(), arg0[0]));

                    if (from.renameTo(to)) {
                        MediaScannerConnection.scanFile(getApplicationContext(),
                                new String[]{ to.getAbsolutePath(), from.getAbsolutePath() }, null, null);
                        album.media.remove(album.selectedMedias.get(i));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (album.media.size() == 0) {
                albums.removeCurrentAlbum();
                albumsAdapter.notifyDataSetChanged();
                displayAlbums();
            }

            mediaAdapter.updateDataSet(album.media);
            finishEditMode();
            invalidateOptionsMenu();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //COPY SELECTED  MEDIA ASYNCTASK
}
