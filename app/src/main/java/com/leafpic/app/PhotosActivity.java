package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.leafpic.app.Adapters.PhotosAdapter;
import com.leafpic.app.Base.Album;
import com.leafpic.app.Base.CustomAlbumsHandler;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Base.HandlingPhotos;
import com.leafpic.app.Base.Photo;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotosActivity extends AppCompatActivity {

    HandlingAlbums albums = new HandlingAlbums(PhotosActivity.this);
    CustomAlbumsHandler customAlbumsHandler = new CustomAlbumsHandler(PhotosActivity.this);
    HandlingPhotos photos;

    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    ImageView headerImage;
    SharedPreferences SP;

    boolean editmode = false;
    PhotosAdapter adapter;
    //PALETTE
    //Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
    // Drawable b = new Drawable.createFromPath(photos.getPreviewAlbumImg());
    //}.decode//.decodeFile(photos.getPreviewAlbumImg());
    /*
    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
        @Override
        public void onGenerated(Palette palette) {
            int primaryDark = getColor(R.color.toolbar);
            int primary = getColor(R.color.toolbar);
            collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
            collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(primary));
            //collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkVibrantColor(primaryDark));
        }
    });
    */
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_photos);

        LoadPhotos();
        initUiTweaks();
    }

    @Override
    public void onResume() {
        LoadPhotos();
        updateHeaderContent();
        super.onResume();
    }

    public void UpdatePhotos() {
        photos.updatePhotos();
        adapter.notifyDataSetChanged();

    }

    public void LoadPhotos() {
        try {
            Bundle data = getIntent().getExtras();
            final Album album = data.getParcelable("album");
            photos = new HandlingPhotos(PhotosActivity.this, album);

            mRecyclerView = (RecyclerView) findViewById(R.id.grid_photos);
            adapter = new PhotosAdapter(photos.photos, R.layout.photo_card);

            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView is = (TextView) v.findViewById(R.id.photo_path);
                    if (editmode) {
                        adapter.notifyItemChanged(photos.toggleSelectPhoto(is.getTag().toString()));
                        invalidateOptionsMenu();
                    } else {
                        photos.setCurrentPhoto(is.getTag().toString());
                        Intent intent = new Intent(PhotosActivity.this, PhotoPagerActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable("album", photos);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                }
            });
            adapter.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TextView is = (TextView) v.findViewById(R.id.photo_path);
                    adapter.notifyItemChanged(photos.toggleSelectPhoto(is.getTag().toString()));
                    editmode = true;
                    invalidateOptionsMenu();
                    return true;
                }
            });


            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setNestedScrollingEnabled(true);
            mRecyclerView.setFitsSystemWindows(true);

        }
        catch (Exception e){ e.printStackTrace(); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photos, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        MenuItem opt;

        if (editmode)
            setOptionsAlbmuMenusItemsVisible(menu, false);
         else
            setOptionsAlbmuMenusItemsVisible(menu, true);

        if (photos.hidden)
            menu.findItem(R.id.hideAlbumButton).setTitle(getString(R.string.unhide_album_action));
         else
            menu.findItem(R.id.hideAlbumButton).setTitle(getString(R.string.hide_album_action));


        if (photos.getSelectedCount() == 0) {
            editmode = false;
            setOptionsAlbmuMenusItemsVisible(menu, true);
        } else if (photos.getSelectedCount() == 1) {
            opt = menu.findItem(R.id.setAsAlbumPreview);
            opt.setEnabled(true).setVisible(true);
        } else {
            opt = menu.findItem(R.id.setAsAlbumPreview);
            opt.setEnabled(false).setVisible(false);
        }

        togglePrimaryToolbarOptions(menu);
        updateSort(menu);
        updateSelectedStuff();

        return super.onPrepareOptionsMenu(menu);
    }

    private void togglePrimaryToolbarOptions(final Menu menu) {
        MenuItem opt;
        if (editmode) {
            opt = menu.findItem(R.id.sortPhotos);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            opt = menu.findItem(R.id.deleteAction);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            opt = menu.findItem(R.id.sharePhotos);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            opt = menu.findItem(R.id.sortPhotos);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            opt = menu.findItem(R.id.deleteAction);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            opt = menu.findItem(R.id.sharePhotos);
            opt.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

    }

    private void setOptionsAlbmuMenusItemsVisible(final Menu m, boolean val) {
        MenuItem option = m.findItem(R.id.hideAlbumButton);
        option.setEnabled(val).setVisible(val);
        option = m.findItem(R.id.excludeAlbumButton);
        option.setEnabled(val).setVisible(val);
        option = m.findItem(R.id.renameAlbum);
        option.setEnabled(val).setVisible(val);
        option = m.findItem(R.id.select_all_albums_action);
        option.setEnabled(!val).setVisible(!val);

        option = m.findItem(R.id.sharePhotos);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.moveAction);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.copyAction);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.setAsAlbumPreview);
        option.setEnabled(!val).setVisible(!val);
    }

    void updateSelectedStuff() {

        int c;
        try {
            if ((c = photos.getSelectedCount()) != 0) {

                collapsingToolbarLayout.setTitle(c + "/" + photos.photos.size());
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
                        photos.selectAllPhotos();
                        adapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }
                });

            } else {

                collapsingToolbarLayout.setTitle(photos.DisplayName);
                toolbar.setNavigationIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_arrow_back)
                        .color(Color.WHITE)
                        .sizeDp(20));

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
        photos.clearSelectedPhotos();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            final Bundle b = data.getExtras();


            switch (requestCode) {
                case SelectAlbumActivity.COPY_TO_ACTION:
                    if (resultCode == RESULT_OK) {
                        StringUtils.showToast(getApplicationContext(), "copied ok");
                    }
                    break;
                case SelectAlbumActivity.MOVE_TO_ACTION:
                    if (resultCode == RESULT_OK) {
                        String ind = b.getString("photos_indexes");
                        if (ind != null) {
                            for (String asd : ind.split("ç")) {
                                //Log.wtf("asdasdasdas", photos.photos.size() + "");
                                //photos.removePhoto(Integer.valueOf(asd));
                                // TODO remove photo moved from older album [porco dio]
                                //Log.wtf("asdasdasdas", photos.photos.size() + "");
                                //adapter.removeItemAt(Integer.valueOf(asd));
                                //mRecyclerView.removeViewAt(Integer.parseInt(asd));
                                //photos.photos.remove(Integer.parseInt(asd));
                                //mRecyclerView.removeViewAt(Integer.valueOf(asd));

                                //adapter.notifyItemRemoved(Integer.parseInt(asd));
                            }
                        }
                        //adapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }
                    break;
                default:
                    break;
            }
        }
        finishEditMode();

    }

    public void updateSort(final Menu menu){

        menu.findItem(R.id.ascending_sort_action).setChecked(photos.settings.ascending);

        if (photos.settings.columnSortingMode == null || photos.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DATE_TAKEN))
            menu.findItem(R.id.date_taken_sort_action).setChecked(true);
        else if (photos.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DISPLAY_NAME))
            menu.findItem(R.id.name_sort_action).setChecked(true);
        else if (photos.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.SIZE))
            menu.findItem(R.id.size_sort_action).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.moveAction:
                Intent int1 = new Intent(PhotosActivity.this, SelectAlbumActivity.class);
                int1.putExtra("selected_photos", photos.getSelectedPhotosSerilized());
                int1.putExtra("request_code", SelectAlbumActivity.MOVE_TO_ACTION);
                int1.putExtra("photos_indexes", photos.getSelectedPhotosIndexSerilized());
                startActivityForResult(int1, SelectAlbumActivity.MOVE_TO_ACTION);
                break;
            case R.id.copyAction:
                Intent int2 = new Intent(PhotosActivity.this, SelectAlbumActivity.class);
                int2.putExtra("selected_photos", photos.getSelectedPhotosSerilized());
                int2.putExtra("request_code", SelectAlbumActivity.COPY_TO_ACTION);
                startActivityForResult(int2, SelectAlbumActivity.COPY_TO_ACTION);
                break;

            case R.id.select_all_albums_action:
                photos.selectAllPhotos();
                adapter.notifyDataSetChanged();
                invalidateOptionsMenu();
                break;


            case R.id.setAsAlbumPreview:
                photos.setSelectedPhotoAsPreview();
                finishEditMode();
                updateHeaderContent();
                break;
            case R.id.sortPhotos:
                if (!photos.hidden) {
                    final PopupMenu popup = new PopupMenu(PhotosActivity.this, findViewById(R.id.sortPhotos));
                    popup.setGravity(Gravity.AXIS_PULL_BEFORE);
                    popup.getMenuInflater().inflate(R.menu.sort, popup.getMenu());


                } else StringUtils.showToast(getApplicationContext(), " In progress");
                break;

            case R.id.name_sort_action:
                photos.setDefaultSortingMode(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                photos.sort();
                LoadPhotos();
                break;
            case R.id.size_sort_action:
                photos.setDefaultSortingMode(MediaStore.Images.ImageColumns.SIZE);
                photos.sort();
                LoadPhotos();
                break;
            case R.id.date_taken_sort_action:
                photos.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);
                photos.sort();
                LoadPhotos();
                break;
            case R.id.ascending_sort_action:
                photos.setDefaultSortingAscending(!photos.settings.ascending);
                photos.sort();
                LoadPhotos();
                break;

            case R.id.renameAlbum:
                /*new MaterialDialog.Builder(this)
                        .title("Rename Album")
                        .content("insert a fucking NAME")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, photos.DisplayName, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                               // TODO make this better
                                    albums.renameAlbum(photos.FolderPath, input.toString());
                                //onBackPressed();
                                    //finish();

                                StringUtils.showToast(getApplicationContext(), "I have to fix this!");

                            }
                        }).show();*/
                break;

            case R.id.excludeAlbumButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotosActivity.this);
                builder.setMessage(R.string.exclude_album_message)
                        .setPositiveButton("EXCLUDE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                customAlbumsHandler.excludeAlbum(photos.ID);
                                finish();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}});
                builder.show();
                break;

            case R.id.deleteAction:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(PhotosActivity.this);
                if(editmode) builder1.setMessage(R.string.delete_photos_message);
                else builder1.setMessage(R.string.delete_album_message);
                builder1.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editmode) {
                            photos.deleteSelectedPhotos();
                            adapter.notifyDataSetChanged();
                            updateHeaderContent();
                            finishEditMode();
                        } else {
                            albums.deleteAlbum(photos.FolderPath);
                            finish();
                        }
                    }
                })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}});
                builder1.show();
                break;

            case R.id.hideAlbumButton:
                if (photos.hidden) {
                    albums.unHideAlbum(photos.FolderPath);
                    finish();
                } else {

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(PhotosActivity.this);
                    builder2.setMessage(R.string.delete_album_message)
                            .setPositiveButton("HIDE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    albums.hideAlbum(photos.FolderPath, photos.photos);
                                }
                            })
                            .setNeutralButton("EXCLUDE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    customAlbumsHandler.excludeAlbum(photos.ID);
                                    finish();
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}});
                    builder2.show();
                }
                break;

            case R.id.sharePhotos:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                intent.setType("image/*");

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (Photo f : photos.selectedPhotos)
                    files.add(Uri.fromFile(new File(f.Path)));

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                finishEditMode();
                startActivity(intent);
                break;

            case android.R.id.home:
                onBackPressed();
                return true;

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

    public void initUiTweaks() {
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        int accentColor = SP.getInt("accent_color", Color.rgb(0, 77, 64));//TEAL COLOR DEFAULT
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & accentColor));
        int primaryColor = SP.getInt("primary_color", Color.rgb(0, 150, 136));//TEAL CARD BG DEFAULT
        String hexPrimaryColor = String.format("#%06X", (0xFFFFFF & primaryColor));
        /**** Navigation Bar*/
        boolean NavBar = SP.getBoolean("nav_bar", false);
        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (NavBar)) {
            getWindow().setNavigationBarColor(Color.parseColor(hexPrimaryColor));
            //getWindow().setNavigationBarColor(getColor(R.color.toolbar));
        }

        /**** Status Bar */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setStatusBarTranslucent(false);//true
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        /**** ToolBar*/

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setBackgroundColor(Color.parseColor(hexPrimaryColor));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /****SET THEME***/
        RecyclerView rw = (RecyclerView) findViewById(R.id.grid_photos);
        if (SP.getBoolean("set_dark_theme", false)){
            rw.setBackgroundColor(getColor(R.color.act_bg_dark));
        }else {
            rw.setBackgroundColor(getColor(R.color.act_bg_light));
        }


        headerImage = (ImageView) findViewById(R.id.header_image);
        Glide.with(this)
                .load(photos.getPreviewAlbumImg())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(headerImage);

        headerImage.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
        updateHeaderContent();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(photos.DisplayName);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setExpandedTitleColor(getColor(android.R.color.transparent));
        collapsingToolbarLayout.setContentScrimColor(Color.parseColor(hexPrimaryColor));//getColor(R.color.toolbar)
        collapsingToolbarLayout.setStatusBarScrimColor(Color.parseColor(hexPrimaryColor));//getColor(R.color.toolbar)

        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hexAccentColor)));

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(i);

            }
        });
    }

    private void updateHeaderContent() {
        headerImage = (ImageView) findViewById(R.id.header_image);
        Glide.with(this)
                .load(photos.getPreviewAlbumImg())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(headerImage);
        headerImage.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);

        TextView textView = (TextView) findViewById(R.id.album_name);
        textView.setText(photos.DisplayName);
        textView = (TextView) findViewById(R.id.album_photos_count);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int accentColor = SP.getInt("accent_color", Color.rgb(0, 77, 64));//TEAL COLOR DEFAULT
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & accentColor));

        textView.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + photos.photos.size() + "</font></b>" + "<font " +
                "color='#FFFFFF'> Photos</font>"));


    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //getWindow().setStatusBarColor(getColor(R.color.status_bar));
        }
    }
}
