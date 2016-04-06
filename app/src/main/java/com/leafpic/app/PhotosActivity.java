package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.leafpic.app.Adapters.PhotosAdapter;
import com.leafpic.app.Base.Album;
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

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotosActivity extends ThemedActivity {

    HandlingAlbums albums = new HandlingAlbums(PhotosActivity.this);
    CustomAlbumsHandler customAlbumsHandler = new CustomAlbumsHandler(PhotosActivity.this);
    FloatingActionButton fabCamera;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    ImageView headerImage;
    //SwipeRefreshLayout mSwipeRefreshLayout;
    AppBarLayout appBarLayout;
    boolean editmode = false;
    PhotosAdapter adapter;

    RecyclerView mRecyclerView;
    Album album;
    View.OnLongClickListener albumOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView is = (TextView) v.findViewById(R.id.photo_path);
            adapter.notifyItemChanged(album.toggleSelectPhoto(is.getTag().toString()));
            editmode = true;
            invalidateOptionsMenu();
            return true;
        }
    };
    View.OnClickListener albumOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView is = (TextView) v.findViewById(R.id.photo_path);
            if (editmode) {
                adapter.notifyItemChanged(album.toggleSelectPhoto(is.getTag().toString()));
                invalidateOptionsMenu();
            } else {
                album.setCurrentPhoto(is.getTag().toString());
                Intent intent = new Intent(PhotosActivity.this, PhotoPagerActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("album", album);
                intent.putExtras(b);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_photos);

        try {
            Bundle data = getIntent().getExtras();
            final Album album = data.getParcelable("album");
            assert album != null;
            //photos = new HandlingPhotos(PhotosActivity.this, album);
            this.album = album;
            this.album.setContext(getApplicationContext());
            //this.album.setSettings();

            // if (photos.medias == null)
            //   finish();

        } catch (Exception e) {
           // Log.d("asdff", "onCreate: asddsad", e);
            finish();
        }
        //LoadPhotos();
        initUiTweaks();
    }

    @Override
    public void onResume() {
        super.onResume();
        new PreparePhotosTask().execute();
        //LoadPhotos();
        updateHeaderContent();
        updateSelectedStuff();
        setupUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photos, menu);
        menu.findItem(R.id.ascending_sort_action).setChecked(album.settings.ascending);

        if (album.settings.columnSortingMode == null || album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DATE_TAKEN))
            menu.findItem(R.id.date_taken_sort_action).setChecked(true);
        else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DISPLAY_NAME))
            menu.findItem(R.id.name_sort_action).setChecked(true);
        else if (album.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.SIZE))
            menu.findItem(R.id.size_sort_action).setChecked(true);

        menu.findItem(R.id.select_all_albums_action).setTitle(getString(
                album.getSelectedCount() == adapter.getItemCount()
                        ? R.string.clear_selected
                        : R.string.select_all));
        menu.findItem(R.id.filter_menu).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_filter_list));
        menu.findItem(R.id.sortPhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_sort));
        menu.findItem(R.id.sharePhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.deleteAction).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        setOptionsAlbmuMenusItemsVisible(menu, !editmode);

        if (album.getSelectedCount() == 0) {
            editmode = false;
            setOptionsAlbmuMenusItemsVisible(menu, true);
        } else if (album.getSelectedCount() == 1)
            menu.findItem(R.id.setAsAlbumPreview).setEnabled(true).setVisible(true);
         else
            menu.findItem(R.id.setAsAlbumPreview).setEnabled(false).setVisible(false);

        menu.findItem(R.id.clear_album_preview).setVisible(album.hasCustomCover());

        togglePrimaryToolbarOptions(menu);
        updateSelectedStuff();
        return super.onPrepareOptionsMenu(menu);
    }

    private void togglePrimaryToolbarOptions(final Menu menu) {
        if (editmode) {
            menu.findItem(R.id.sortPhotos).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.findItem(R.id.deleteAction).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.findItem(R.id.sharePhotos).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            menu.findItem(R.id.sortPhotos).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.findItem(R.id.deleteAction).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.findItem(R.id.sharePhotos).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    private void setOptionsAlbmuMenusItemsVisible(final Menu m, boolean val) {
        m.setGroupVisible(R.id.album_options_menu, val);
        m.setGroupVisible(R.id.general_action, val);
        m.setGroupVisible(R.id.photos_option_men, !val);
    }

    void updateSelectedStuff() {

        int c;
        try {
            if ((c = album.getSelectedCount()) != 0) {

                collapsingToolbarLayout.setTitle(c + "/" + album.medias.size());
                toolbar.setNavigationIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_check)
                        .color(Color.WHITE)
                        .sizeDp(20));
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { finishEditMode(); }});

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

                collapsingToolbarLayout.setTitle(album.DisplayName);
                toolbar.setNavigationIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_arrow_back)
                        .color(Color.WHITE)
                        .sizeDp(18));

                toolbar.setOnClickListener(null);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {onBackPressed();}
                });
            }
        }catch (NullPointerException e){e.printStackTrace();}

    }

    private void finishEditMode() {
        editmode = false;
        invalidateOptionsMenu();
        album.clearSelectedPhotos();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            final Bundle b = data.getExtras();


            switch (requestCode) {
                case PickAlbumActivity.COPY_TO_ACTION:
                    if (resultCode == RESULT_OK) {
                        StringUtils.showToast(getApplicationContext(), "copied ok");
                    }
                    break;
                case PickAlbumActivity.MOVE_TO_ACTION:
                    if (resultCode == RESULT_OK) {
                        /*String ind = b.getString("photos_indexes");
                        if (ind != null) {
                            Log.wtf("lengh", "" + photos.medias.size());

                            for (String asd : ind.split("ç")) {

                                int a = Integer.valueOf(asd);
                                Log.wtf("asd", "" + a);
                                photos.medias.remove(a);
                                //adapter.notifyItemRemoved(a);
                            }
                            Log.wtf("lengh", "" + photos.medias.size());
                            adapter.notifyDataSetChanged();
                            invalidateOptionsMenu();
                        }*/

                    }
                    break;
                default:
                    break;
            }
        }
        finishEditMode();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            /*case R.id.moveAction:
                Intent int1 = new Intent(PhotosActivity.this, PickAlbumActivity.class);
                int1.putExtra("selected_photos", photos.getSelectedPhotosSerilized());
                int1.putExtra("request_code", PickAlbumActivity.MOVE_TO_ACTION);
                int1.putExtra("photos_indexes", photos.getSelectedPhotosIndexSerilized());
                startActivityForResult(int1, PickAlbumActivity.MOVE_TO_ACTION);
                break;
            case R.id.copyAction:
                Intent int2 = new Intent(PhotosActivity.this, PickAlbumActivity.class);
                int2.putExtra("selected_photos", photos.getSelectedPhotosSerilized());
                int2.putExtra("request_code", PickAlbumActivity.COPY_TO_ACTION);
                startActivityForResult(int2, PickAlbumActivity.COPY_TO_ACTION);
                break;*/

            case R.id.select_all_albums_action:
                if(album.getSelectedCount()==adapter.getItemCount()){
                    editmode = false;
                    invalidateOptionsMenu();
                    album.clearSelectedPhotos();
                    adapter.notifyDataSetChanged();
                } else {
                    album.selectAllPhotos();
                    adapter.notifyDataSetChanged();
                    invalidateOptionsMenu();
                }
                break;

            case R.id.clear_album_preview:
                CustomAlbumsHandler as = new CustomAlbumsHandler(getApplicationContext());
                as.clearAlbumPreview(album.ID);
                album.setSettings();
                updateHeaderContent();
                break;

            case R.id.setAsAlbumPreview:
                album.setSelectedPhotoAsPreview();
                finishEditMode();
                updateHeaderContent();
                break;

            case R.id.all_media_filter:
                album.filterMedias(Album.FILTER_ALL);
                adapter.updateDataset(album.medias);
                item.setChecked(true);
                fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                break;
            case R.id.video_media_filter:
                album.filterMedias(Album.FILTER_VIDEO);
                adapter.updateDataset(album.medias);
                item.setChecked(true);
                fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                break;
            case R.id.image_media_filter:
                album.filterMedias(Album.FILTER_IMAGE);
                adapter.updateDataset(album.medias);
                item.setChecked(true);
                fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                break;

            case R.id.gifs_media_filter:
                album.filterMedias(Album.FILTER_GIF);
                adapter.updateDataset(album.medias);
                item.setChecked(true);
                fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
                break;

            case R.id.name_sort_action:
                album.setDefaultSortingMode(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                new PreparePhotosTask().execute();
                item.setChecked(true);
                break;
            case R.id.size_sort_action:
                album.setDefaultSortingMode(MediaStore.Images.ImageColumns.SIZE);
                new PreparePhotosTask().execute();
                item.setChecked(true);
                break;
            case R.id.date_taken_sort_action:
                album.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);

                new PreparePhotosTask().execute();

                item.setChecked(true);
                break;
            case R.id.ascending_sort_action:
                album.setDefaultSortingAscending(!album.settings.ascending);
                new PreparePhotosTask().execute();

                item.setChecked(!item.isChecked());
                break;

            case R.id.renameAlbum:
                final AlertDialog.Builder RenameDialog;
                if (isDarkTheme())
                    RenameDialog = new AlertDialog.Builder(PhotosActivity.this, R.style.AlertDialog_Dark);
                else
                    RenameDialog = new AlertDialog.Builder(PhotosActivity.this, R.style.AlertDialog_Light);

                final View Rename_dialogLayout = getLayoutInflater().inflate(R.layout.rename_dialog, null);
                final TextView title = (TextView) Rename_dialogLayout.findViewById(R.id.rename_title);
                final EditText txt_edit = (EditText) Rename_dialogLayout.findViewById(R.id.dialog_txt);
                CardView cv_Rename_Dialog = (CardView) Rename_dialogLayout.findViewById(R.id.rename_card);

                title.setBackgroundColor(getPrimaryColor());
                title.setText(getString(R.string.rename));
                txt_edit.setText(album.DisplayName);//da fixxare
                txt_edit.selectAll();

                txt_edit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                cv_Rename_Dialog.setBackgroundColor(getCardBackgroundColor());
                txt_edit.setTextColor(getTextColor());
                txt_edit.setHintTextColor(getTextColor());
                txt_edit.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);

                RenameDialog.setView(Rename_dialogLayout);
                RenameDialog.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                RenameDialog.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (txt_edit.length() != 0) {
                            albums.renameAlbum(album.Path, txt_edit.getText().toString());
                            album.DisplayName = txt_edit.getText().toString();
                            updateHeaderContent();
                            //UpdatePhotos();//TODO updatePhoto photos
                        } else
                            StringUtils.showToast(getApplicationContext(), getString(R.string.insert_a_name));
                    }
                });
                RenameDialog.show();
                break;

            case R.id.excludeAlbumButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotosActivity.this);
                builder.setMessage(R.string.exclude_album_message)
                        .setPositiveButton(getString(R.string.exclude), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                customAlbumsHandler.excludeAlbum(album.ID);
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}});
                builder.show();
                break;

            case R.id.deleteAction:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(PhotosActivity.this);
                if(editmode) builder1.setMessage(R.string.delete_photos_message);
                else builder1.setMessage(R.string.delete_album_message);
                builder1.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editmode) {
                            album.deleteSelectedPhotos();

                            if (album.medias.size() == 0) {
                                startActivity(new Intent(PhotosActivity.this, AlbumsActivity.class));
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
                break;

            case R.id.hideAlbumButton:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(PhotosActivity.this);
                builder2.setMessage(R.string.delete_album_message)
                        .setPositiveButton(getString(R.string.hide), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                albums.hideAlbum(album.Path);
                            }
                        })
                        .setNeutralButton(getString(R.string.exclude), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                customAlbumsHandler.excludeAlbum(album.ID);
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}});
                builder2.show();

                break;

            case R.id.sharePhotos:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sent_to_action));

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (Media f : album.selectedMedias)
                    files.add(Uri.fromFile(new File(f.Path)));

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setType(StringUtils.getGenericMIME(album.selectedMedias.get(0).MIME));
                finishEditMode();
                startActivity(intent);
                break;

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.setting:
                Intent intent2= new Intent(getApplicationContext(), SettingActivity.class);

                //intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent2);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void initUiTweaks() {

        setNavBarColor();
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        /**** ToolBar*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(!isDarkTheme())
            toolbar.setPopupTheme(R.style.LightActionBarMenu);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_photos);
        adapter = new PhotosAdapter(album.medias, getApplicationContext());
        adapter.setOnClickListener(albumOnClickListener);
        adapter.setOnLongClickListener(albumOnLongClickListener);
        mRecyclerView.setHasFixedSize(true);
        int nSpan = Measure.getPhotosColums(getApplicationContext());
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, nSpan));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(nSpan, Measure.pxToDp(2, getApplicationContext()), true));
        mRecyclerView.setFitsSystemWindows(true);
        mRecyclerView.setAdapter(adapter);

        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (album.areFiltersActive()){
                    album.filterMedias(Album.FILTER_ALL);
                    adapter.updateDataset(album.medias);
                    toolbar.getMenu().findItem(R.id.all_media_filter).setChecked(true);
                    fabCamera.setImageDrawable(new IconicsDrawable(PhotosActivity.this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
                } else startActivity( new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));

            }
        });

        /*
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if(isTraslucentStatusBar()) {
            float[] hsv = new float[3];
            int color = getPrimaryColor();
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.85f; // value component
            color = Color.HSVToColor(hsv);
            collapsingToolbarLayout.setStatusBarScrimColor(color);
        } else collapsingToolbarLayout.setStatusBarScrimColor(getPrimaryColor());
        */
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        collapsingToolbarLayout.setTitle(album.DisplayName);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setContentScrimColor(getPrimaryColor());
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        setupUI();

        if (isCollapsingToolbar()) {
            appBarLayout.setExpanded(true, true);
            updateHeaderContent();
        } else {
            appBarLayout.setExpanded(false, false);
            findViewById(R.id.album_card_divider).setVisibility(View.GONE);
        }

        setRecentApp(album.DisplayName);
    }

    public void setupUI() {

        mRecyclerView.setBackgroundColor(getBackgroundColor());
        collapsingToolbarLayout.setStatusBarScrimColor(isTraslucentStatusBar() ? ColorPalette.getOscuredColor(getPrimaryColor()): getPrimaryColor());
        if(!isDarkTheme())
            toolbar.setPopupTheme(R.style.LightActionBarMenu);
        mRecyclerView.setNestedScrollingEnabled(isCollapsingToolbar());
        fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));

    }

    private void updateHeaderContent() {
        if(isCollapsingToolbar()) {
            headerImage = (ImageView) findViewById(R.id.header_image);
            Glide.with(PhotosActivity.this)
                    .load(album.getPathCoverAlbum())
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.ic_empty)
                    .into(headerImage);
            headerImage.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);

            TextView textView = (TextView) findViewById(R.id.album_name);
            textView.setText(album.DisplayName);
            textView = (TextView) findViewById(R.id.album_photos_count);

            String hexAccentColor = String.format("#%06X", (0xFFFFFF & getAccentColor()));

            textView.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + album.count.getTotal() + "</font></b>" + "<font " +
                    "color='#FFFFFF'> " + album.getContentDescdription(getApplicationContext()) + "</font>"));

        }
    }

    private void initActivityTransitions() {
        Slide transition = new Slide();
        transition.excludeTarget(android.R.id.statusBarBackground, true);
        getWindow().setEnterTransition(transition);
        getWindow().setReturnTransition(transition);
    }

    public class PreparePhotosTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            //SwipeContainerRV.setRefreshing(true);
            //adapter.setOnLongClickListener(null);
            //adapter.setOnClickListener(null);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //checkPermissions();
            album.updatePhotos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.updateDataset(album.medias);
            // adapter.setOnClickListener(albumOnClickListener);
            // adapter.setOnLongClickListener(albumOnLongClickListener);
            // SwipeContainerRV.setRefreshing(false);
        }
    }
}