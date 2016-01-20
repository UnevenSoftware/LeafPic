package com.leafpic.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.leafpic.app.Adapters.PhotosAdapter;

/**
 * Created by dnld on 12/12/15.
 */
public class PhotosActivity extends AppCompatActivity {
    HandlingAlbums albums = new HandlingAlbums(PhotosActivity.this);

    HandlingPhotos photos;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView image;


    boolean hideToolBar = false;
    boolean editmode = false;
    PhotosAdapter adapter;

    @Override
    public void onResume() {
        //string.showToast(this, album.Path);
        super.onResume();

    }


    private void setPalette() { //TODO remaake doesn't work wiht image loaded by Glide
        try {
            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
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
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_photos);

       /* ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(100, 100)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().destroy();
        ImageLoader.getInstance().init(config);*/

        try {
            Bundle data = getIntent().getExtras();
            final Album album = data.getParcelable("album");
            photos = new HandlingPhotos(PhotosActivity.this, album.Path, album.isHidden());

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
                    return false;
                }
            });


            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (hideToolBar) {
                        //toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                        // AccelerateInterpolator()).start();
                        //getSupportActionBar().hide();
                    } else {
                        //toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                        //getSupportActionBar().show();
                    }
                    hideToolBar=!hideToolBar;
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
            mRecyclerView.setNestedScrollingEnabled(true);


            initUiTweaks();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Rename Album");
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                builder.setView(input);

                builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        albums.renameAlbum(photos.FolderPath, input.getText().toString());
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.show();
                break;


            case R.id.excludeAlbumButton:
                AlertDialog.Builder dasdf = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style
                        .AlertDialogCustom));
                dasdf.setMessage(getString(R.string.exclude_album_message));
                dasdf.setCancelable(true);
                dasdf.setPositiveButton("EXCLUDE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        albums.excludeAlbum(photos.FolderPath);
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

            case R.id.deleteAction:
                AlertDialog.Builder dlg = new AlertDialog.Builder(
                        new ContextThemeWrapper(this, R.style.AlertDialogCustom));

                if (editmode) dlg.setMessage(getString(R.string.delete_photos_message));
                else dlg.setMessage(getString(R.string.delete_album_message));

                dlg.setCancelable(true);
                dlg.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editmode) {
                            photos.deleteSelectedPhotos();
                            adapter.notifyDataSetChanged();
                        } else {
                            albums.deleteAlbum(photos.FolderPath);
                            finish();
                        }
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
                if (photos.hidden) {
                    albums.unHideAlbum(photos.FolderPath);
                    finish();
                } else {
                    AlertDialog.Builder dlg1 = new AlertDialog.Builder(
                            new ContextThemeWrapper(this, R.style.AlertDialogCustom));
                    dlg1.setMessage(getString(R.string.hide_album_message));
                    dlg1.setPositiveButton("HIDE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            albums.hideAlbum(photos.FolderPath);
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
                            albums.excludeAlbum(photos.FolderPath);
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
        /*
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.toolbar));
        }
        */
        /**** Status Bar */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setStatusBarTranslucent(true);
        //window.setStatusBarColor(getColor(R.color.status_bar));


        /**** ToolBar*/

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = (ImageView) findViewById(R.id.image);
        Glide.with(this)
                .load(photos.getPreviewAlbumImg())
                .centerCrop()
                .placeholder(R.drawable.ic_empty)
                .crossFade()
                .into(image);


        //OSCURA LIMMAGINE
        image.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);

        TextView textView = (TextView) findViewById(R.id.AlbumName);
        textView.setText(photos.DisplayName);
        //SpannableString content = new SpannableString(photos.DisplayName);
        //content.setSpan(new UnderlineSpan(), 10, content.length(), 0);
        //textView.setText(content);
        textView = (TextView) findViewById(R.id.AlbumNPhotos);
        textView.setText(Html.fromHtml("<b><font color='#FBC02D'>" + photos.photos.size()+ "</font></b>" + "<font " +
                "color='#FFFFFF'> Photos</font>"));

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(photos.DisplayName);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setExpandedTitleColor(getColor(android.R.color.transparent));

        setPalette();
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
