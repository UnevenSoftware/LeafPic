package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.GridView;
import android.widget.ImageView;
import com.leafpic.app.Adapters.PhotosAdapter;
import com.leafpic.app.utils.string;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotosActivity extends AppCompatActivity {

    HandlingAlbums albums = new HandlingAlbums(PhotosActivity.this);
    DatabaseHandler db = new DatabaseHandler(PhotosActivity.this);

    Album album;

    GridView photosgrid;
    boolean editmode = false, hidden = false;
    PhotosAdapter adapter;

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(100, 100)
                .diskCacheExtraOptions(100, 100, null)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
        initUiTweaks();


        try {
            Bundle data = getIntent().getExtras();
            album = data.getParcelable("album");
            setTitle(album.DisplayName);

            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.gridPhotos);
            adapter = new PhotosAdapter(album.photos, R.layout.photo_card);
            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView is = (ImageView) v.findViewById(R.id.pic);


                    Photo a = db.getPhoto(is.getTag().toString());

                    Intent intent = new Intent(PhotosActivity.this, PhotoActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("album", album);
                    b.putParcelable("photo", a);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                boolean hideToolBar = false;

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
                    if (hideToolBar) {
                        //toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new
                        // AccelerateInterpolator()).start();
                        getSupportActionBar().hide();
                    } else {
                        //toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                        getSupportActionBar().show();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 20) hideToolBar = true;
                    else if (dy < -5) hideToolBar = false;

                }
            });
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());


            hidden = album.isHidden();
        }
        catch (Exception e){ e.printStackTrace(); }


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
            setOptionsAlbmuMenusItemsVisible(menu, false);
        } else {
            opt = menu.findItem(R.id.endEditAlbumMode);
            opt.setEnabled(false).setVisible(false);
            setOptionsAlbmuMenusItemsVisible(menu, true);
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
            opt = menu.findItem(R.id.endEditAlbumMode);
            setOptionsAlbmuMenusItemsVisible(menu,false);
            opt.setEnabled(false).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void setOptionsAlbmuMenusItemsVisible(final Menu m, boolean val) {
        MenuItem option = m.findItem(R.id.hideAlbumButton);
        option.setEnabled(val).setVisible(val);

        option = m.findItem(R.id.deleteAlbumButton);
        option.setEnabled(val).setVisible(val);

        option = m.findItem(R.id.excludeAlbumButton);
        option.setEnabled(val).setVisible(val);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.endEditAlbumMode:
                /*editmode = false;
                invalidateOptionsMenu();
                photos.clearSelectedPhotos();
                adapter.notifyDataSetChanged();*/
                break;
            case R.id.action_settings:
                string.showToast(PhotosActivity.this, "asdasdas");

                break;

            case R.id.excludeAlbumButton:
                AlertDialog.Builder dasdf = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style
                        .AlertDialogCustom));
                dasdf.setMessage(getString(R.string.exclude_album_message));
                dasdf.setCancelable(true);
                dasdf.setPositiveButton("EXCLUDE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        albums.excludeAlbum(album);
                        finish();
                    }
                });
                dasdf.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dasdf.show();
                break;

            case R.id.deleteAlbumButton:
                AlertDialog.Builder dlg = new AlertDialog.Builder(
                        new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                dlg.setMessage(getString(R.string.delete_album_action));
                dlg.setCancelable(true);
                dlg.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        albums.deleteAlbum(album);
                        finish();
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
                    albums.unHideAlbum(album);
                    finish();
                } else {
                    AlertDialog.Builder dlg1 = new AlertDialog.Builder(
                            new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                    dlg1.setMessage(getString(R.string.hide_album_message));
                    dlg1.setPositiveButton("HIDE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            albums.hideAlbum(album);
                            finish();
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
                            albums.excludeAlbum(album);
                            finish();

                        }
                    });
                    dlg1.show();
                }

                break;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_camera:
                Photo s = (Photo) photosgrid.getSelectedItem();
                string.showToast(this, s.FolderPath);
                /*Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(i);*/
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }


    public void initUiTweaks() {

        /**** Status Bar */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.status_bar));


        /**** ToolBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setShowHideAnimationEnabled(true);
        }


    }
}
