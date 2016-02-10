package com.leafpic.app;

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
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.text.Html;
import android.text.InputType;
import android.transition.Slide;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.leafpic.app.Adapters.PhotosAdapter;
import com.leafpic.app.Base.*;
import com.leafpic.app.utils.StringUtils;

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

    public void LoadPhotos() {

        try {
            Bundle data = getIntent().getExtras();
            final Album album = data.getParcelable("album");
            photos = new HandlingPhotos(PhotosActivity.this, album);

            mRecyclerView = (RecyclerView) findViewById(R.id.gridPhotos);
            adapter = new PhotosAdapter(photos.photos, R.layout.photo_card);

            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView is = (TextView) v.findViewById(R.id.path);
                    if (editmode) {
                        adapter.notifyItemChanged(photos.toggleSelectPhoto(is.getTag().toString()));
                        invalidateOptionsMenu();
                    } else {
                        photos.setCurrentPhoto(is.getTag().toString());
                        Intent intent = new Intent(PhotosActivity.this, PhotoActivity.class);
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
                    TextView is = (TextView) v.findViewById(R.id.path);
                    adapter.notifyItemChanged(photos.selectPhoto(is.getTag().toString(), true));
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

    /*private void updateSelectedPhotsCount() {
        getSupportActionBar().setTitle(photos.getSelectedCount() + "");
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photos, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        MenuItem opt;

        if (editmode) {
            opt = menu.findItem(R.id.endEditAlbumMode);
            opt.setEnabled(true).setVisible(true);
            setOptionsAlbmuMenusItemsVisible(menu, false);
        } else {
            opt = menu.findItem(R.id.endEditAlbumMode);
            opt.setEnabled(false).setVisible(false);
            setOptionsAlbmuMenusItemsVisible(menu, true);
        }

        if (photos.hidden) {
            opt = menu.findItem(R.id.hideAlbumButton);
            opt.setTitle(getString(R.string.unhide_album_action));
        } else {
            opt = menu.findItem(R.id.hideAlbumButton);
            opt.setTitle(getString(R.string.hide_album_action));
        }

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

        option = m.findItem(R.id.sharePhotos);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.moveAction);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.copyAction);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.endEditAlbumMode);
        option.setEnabled(!val).setVisible(!val);
        option = m.findItem(R.id.setAsAlbumPreview);
        option.setEnabled(!val).setVisible(!val);
    }

    private void finishEditMode() {
        editmode = false;
        invalidateOptionsMenu();
        photos.clearSelectedPhotos();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // Bundle b = data.getExtras();


        switch (requestCode) {
            case SelectAlbumActivity.COPY_TO_ACTION:
                if (resultCode == RESULT_OK) {
                    // StringUtils.showToast(getApplicationContext(), b.getString("album_path"));
                }
                break;
            case SelectAlbumActivity.MOVE_TO_ACTION:
                onResume();
                //LoadPhotos();
                StringUtils.showToast(getApplicationContext(), "album_path");
                if (resultCode == RESULT_OK) {
                    //LoadPhotos();
                    //StringUtils.showToast(getApplicationContext(),"album_path");

                    /*String newAlbumPath = b.getString("album_path");
                    String selected_photos_paths = b.getString("selected_photos");
                    if(selected_photos_paths != null) {
                         String paths[] = selected_photos_paths.split("ç");
                        Log.wtf("asdasd", selected_photos_paths);
                        for (String path : paths) {
                            int pos = photos.movePhoto(path,newAlbumPath);
                            //adapter.notifyDataSetChanged();
                            //mRecyclerView.removeViewAt(pos);
                            //recyc
                           Log.wtf("asdfdas",pos+"");
                            //adapter.notifyItemChanged(pos
                             //       );


                        }
                        //adapter.notifyDataSetChanged();
                        photos.clearSelectedPhotos();
                        onResume();
                        invalidateOptionsMenu();

                    }*/

                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.moveAction:

                Intent int1 = new Intent(PhotosActivity.this, SelectAlbumActivity.class);
                int1.putExtra("selected_photos", photos.getSelectedPhotosSerilized());
                int1.putExtra("request_code", SelectAlbumActivity.MOVE_TO_ACTION);
                startActivityForResult(int1, SelectAlbumActivity.MOVE_TO_ACTION);
                break;
            case R.id.copyAction:
                Intent int2 = new Intent(PhotosActivity.this, SelectAlbumActivity.class);
                startActivityForResult(int2, SelectAlbumActivity.COPY_TO_ACTION);
                break;
            case R.id.endEditAlbumMode:
                finishEditMode();
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
                    popup.getMenu().findItem(R.id.ascending_sort_action).setChecked(photos.settings.ascending);

                    if (photos.settings.columnSortingMode == null || photos.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DATE_TAKEN))
                        popup.getMenu().findItem(R.id.date_taken_sort_action).setChecked(true);
                    else if (photos.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                        popup.getMenu().findItem(R.id.name_sort_action).setChecked(true);
                    else if (photos.settings.columnSortingMode.equals(MediaStore.Images.ImageColumns.SIZE))
                        popup.getMenu().findItem(R.id.size_sort_action).setChecked(true);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.name_sort_action:
                                    photos.setDefaultSortingMode(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                                    break;
                                case R.id.size_sort_action:
                                    photos.setDefaultSortingMode(MediaStore.Images.ImageColumns.SIZE);
                                    break;
                                case R.id.date_taken_sort_action:
                                    photos.setDefaultSortingMode(MediaStore.Images.ImageColumns.DATE_TAKEN);
                                    break;
                                case R.id.ascending_sort_action:
                                    photos.setDefaultSortingAscending(!photos.settings.ascending);
                                    break;

                                default:
                                    break;
                            }
                            photos.sort();
                            LoadPhotos();
                            return true;
                        }
                    });

                    popup.show();
                } else StringUtils.showToast(getApplicationContext(), " In progress");
                break;

            case R.id.renameAlbum:
                new MaterialDialog.Builder(this)
                        .title("Rename Album")
                        .content("insert a fucking NAME")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, photos.DisplayName, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                               /* TODO make this better
                                    albums.renameAlbum(photos.FolderPath, input.toString());
                                //onBackPressed();
                                    //finish();*/

                                StringUtils.showToast(getApplicationContext(), "I have to fix this!");

                            }
                        }).show();
                break;

            case R.id.excludeAlbumButton:
                new MaterialDialog.Builder(this)
                        .content(R.string.exclude_album_message)
                        .positiveText("EXCLUDE")
                        .negativeText("CANCEL")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                customAlbumsHandler.excludeAlbum(photos.ID);
                                finish();
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
                                if (editmode) {
                                    photos.deleteSelectedPhotos();
                                    adapter.notifyDataSetChanged();
                                    updateHeaderContent();
                                } else {
                                    albums.deleteAlbum(photos.FolderPath);
                                    finish();
                                }
                            }
                        })
                        .show();
                break;

            case R.id.hideAlbumButton:
                if (photos.hidden) {
                    albums.unHideAlbum(photos.FolderPath);
                    finish();
                } else {
                    new MaterialDialog.Builder(this)
                            .content(R.string.hide_album_message)
                            .positiveText("HIDE")
                            .negativeText("CANCEL")
                            .neutralText("EXCLUDE")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    albums.hideAlbum(photos.FolderPath, photos.photos);
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    customAlbumsHandler.excludeAlbum(photos.ID);
                                    finish();
                                }
                            })
                            .show();
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
                startActivity(intent);
                break;

            case android.R.id.home:
                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);
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

    public void fabClick(View v){
        Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(i);
    }

    public void initUiTweaks() {

        /**** Navigation Bar*/
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean NavBar = SP.getBoolean("nav_bar", false);
        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && (NavBar)) {
            getWindow().setNavigationBarColor(getColor(R.color.toolbar));
        }

        /**** Status Bar */
        Window window = getWindow();
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setStatusBarTranslucent(false);//true
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //getWindow().setStatusBarColor(getColor(R.color.status_bar));

        /**** ToolBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        headerImage = (ImageView) findViewById(R.id.image);
        Glide.with(this)
                .load(photos.getPreviewAlbumImg())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(headerImage);

        //OSCURA IMMAGINE COOLAPSIONG
        headerImage.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
        updateHeaderContent();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(photos.DisplayName);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setExpandedTitleColor(getColor(android.R.color.transparent));
        collapsingToolbarLayout.setContentScrimColor(getColor(R.color.toolbar));
        collapsingToolbarLayout.setStatusBarScrimColor(getColor(R.color.toolbar));

        //setPalette();
        /*  RALLENTA TROPPO IL CARICAMENTO DIO PORCO
        File image = new File(photos.getPreviewAlbumImg());
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        setPalette(bitmap);
        */
    }

    private void updateHeaderContent() {
        headerImage = (ImageView) findViewById(R.id.image);
        Glide.with(this)
                .load(photos.getPreviewAlbumImg())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(headerImage);
        headerImage.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);

        TextView textView = (TextView) findViewById(R.id.AlbumName);
        textView.setText(photos.DisplayName);
        textView = (TextView) findViewById(R.id.AlbumNPhotos);

        SharedPreferences SP;
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String SColor = SP.getString("PrefColor", "#03A9F4");
        textView.setText(Html.fromHtml("<b><font color='" + SColor + "'>" + photos.photos.size() + "</font></b>" + "<font " +
                "color='#FFFFFF'> Photos</font>"));
        int color = Color.parseColor(SColor);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(color));
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
