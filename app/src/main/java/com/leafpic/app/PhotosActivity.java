package com.leafpic.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotosActivity extends AppCompatActivity {
    HandlingAlbums albums = new HandlingAlbums(PhotosActivity.this);

    HandlingPhotos photos;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView image;
    SharedPreferences SP;

    boolean editmode = false;
    PhotosAdapter adapter;

    Bitmap bit;


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


    private void setPalette() { //TODO remaake doesn't work wiht image loaded by Glide

            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        // Drawable b = new Drawable.createFromPath(photos.getPreviewAlbumImg());
        //}.decode//.decodeFile(photos.getPreviewAlbumImg());
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    int primaryDark = getColor(R.color.trasparent_toolbar);
                    int primary = getColor(R.color.toolbar);
                    collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
                    collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(primary));
                    //collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkVibrantColor(primaryDark));
                }
            });


    }

    public void LoadPhotos() {
        try {
            Bundle data = getIntent().getExtras();
            final Album album = data.getParcelable("album");
            photos = new HandlingPhotos(PhotosActivity.this, album);

            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.gridPhotos);
            adapter = new PhotosAdapter(photos.photos, R.layout.photo_card);

            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView is = (TextView) v.findViewById(R.id.path);
                    Photo f = photos.getPhoto(is.getTag().toString());
                    int pos;
                    if (editmode) {
                        if (f.isSelected()) pos = photos.selectPhoto(f.Path, false);
                        else pos = photos.selectPhoto(f.Path, true);
                        adapter.notifyItemChanged(pos);
                        invalidateOptionsMenu();
                        //updateSelectedPhotsCount();
                    } else {
                        photos.setCurrentPhoto(f.Path);
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
                    //intebdupdateSelectedPhotsCount();
                    return false;
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
            opt = menu.findItem(R.id.endEditAlbumMode);
            setOptionsAlbmuMenusItemsVisible(menu, true);
            opt.setEnabled(false).setVisible(false);
            opt = menu.findItem(R.id.setAsAlbumPreview);
            opt.setEnabled(false).setVisible(false);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.endEditAlbumMode:
                editmode = false;
                invalidateOptionsMenu();
                photos.clearSelectedPhotos();
                adapter.notifyDataSetChanged();
                break;
            case R.id.renameAlbum:
                new MaterialDialog.Builder(this)
                        .title("Rename Album")
                        .content("insert a fucking NAME")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, photos.DisplayName, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                albums.renameAlbum(photos.FolderPath, input.toString());
                                finish();//TODO make this better
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
                                albums.excludeAlbum(photos.FolderPath);
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
                                    albums.hideAlbum(photos.FolderPath);
                                    finish();
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    albums.excludeAlbum(photos.FolderPath);
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
                intent.setType("image/*"); /* This example is sharing jpeg images. */

                ArrayList<Uri> files = new ArrayList<Uri>();

                for (Photo f : photos.selectedPhotos /* List of the files you want to send */) {
                    File file = new File(f.Path);
                    files.add(Uri.fromFile(file));
                }

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(intent);
                break;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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

    //FABCLICK
    public void fabClicked(View v){
        Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(i);
    }

    public void initUiTweaks() {

        /**** Navigation Bar*/
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean NavBar = SP.getBoolean("nav_bar", false);
        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)&&(NavBar==true)) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.toolbar));
        }

        /**** Status Bar */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setStatusBarTranslucent(true);
        window.setStatusBarColor(getColor(R.color.status_bar));


        /**** ToolBar*/

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        image = (ImageView) findViewById(R.id.image);
        Glide.with(this)
                .load(photos.getPreviewAlbumImg())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .into(image);


        //OSCURA LIMMAGINE
        image.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);

        updateHeaderContent();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(photos.DisplayName);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setExpandedTitleColor(getColor(android.R.color.transparent));

        //setPalette();
    }

    private void updateHeaderContent() {
        TextView textView = (TextView) findViewById(R.id.AlbumName);
        textView.setText(photos.DisplayName);
        textView = (TextView) findViewById(R.id.AlbumNPhotos);
        textView.setText(Html.fromHtml("<b><font color='#FBC02D'>" + photos.photos.size() + "</font></b>" + "<font " +
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
        }
    }
}
