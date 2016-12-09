package org.horaapps.leafpic.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.adapters.AlbumsAdapter;
import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;
import org.horaapps.leafpic.model.Media;
import org.horaapps.leafpic.model.base.FilterMode;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.util.Affix;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends SharedMediaActivity {

  private static String TAG = "AlbumsAct";

  private PreferenceUtil SP;

  private RecyclerView rvAlbums;
  private AlbumsAdapter albumsAdapter;
  private GridSpacingItemDecoration rvAlbumsDecoration;

  private RecyclerView rvMedia;
  private MediaAdapter mediaAdapter;
  private GridSpacingItemDecoration rvMediaDecoration;

  private FloatingActionButton fabCamera;
  private DrawerLayout mDrawerLayout;
  private Toolbar toolbar;
  private SwipeRefreshLayout swipeRefreshLayout;


  private boolean hidden = false, pickMode = false, editMode = false, albumsMode = true, firstLaunch = true;

  private View.OnLongClickListener photosOnLongClickListener = new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View v) {
      Media m = (Media) v.findViewById(R.id.photo_path).getTag();
      if (!editMode) {
        // If it is the first long press
        mediaAdapter.notifyItemChanged(getAlbum().toggleSelectMedia(m));
        editMode = true;
      } else
        getAlbum().selectAllMediaUpTo(m, mediaAdapter);

      invalidateOptionsMenu();
      return true;
    }
  };

  private View.OnClickListener photosOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Media m = (Media) v.findViewById(R.id.photo_path).getTag();
      if (!pickMode) {
        if (editMode) {
          mediaAdapter.notifyItemChanged(getAlbum().toggleSelectMedia(m));
          invalidateOptionsMenu();
        } else {
          getAlbum().setCurrentMedia(m);
          Intent intent = new Intent(MainActivity.this, SingleMediaActivity.class);
          intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
          startActivity(intent);
        }
      } else {
        setResult(RESULT_OK, new Intent().setData(m.getUri()));
        finish();
      }

    }
  };

  private View.OnLongClickListener albumOnLongCLickListener = new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View v) {

      albumsAdapter.notifyItemChanged(getAlbums().toggleSelectAlbum(((Album) v.findViewById(R.id.album_name).getTag())));
      editMode = true;
      invalidateOptionsMenu();
      return true;
    }
  };

  private View.OnClickListener albumOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Album album = (Album) v.findViewById(R.id.album_name).getTag();
      if (editMode) {
        albumsAdapter.notifyItemChanged(getAlbums().toggleSelectAlbum(album));
        invalidateOptionsMenu();
      } else {
        getAlbums().setCurrentAlbum(album);
        displayCurrentAlbumMedia(true);
        setRecentApp(getAlbums().getCurrentAlbum().getName());
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    rvAlbums = (RecyclerView) findViewById(R.id.grid_albums);
    rvMedia = ((RecyclerView) findViewById(R.id.grid_photos));
    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
    fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

    SP = PreferenceUtil.getInstance(getApplicationContext());

    initUi();
    displayData(getIntent().getExtras());
  }

  @Override
  public void onResume() {
    super.onResume();
    updateColumnsRvs();
    getAlbums().clearSelectedAlbums();
    getAlbum().clearSelectedMedia();
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

  private void displayCurrentAlbumMedia(boolean reload) {
    toolbar.setTitle(getAlbum().getName());
    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    mediaAdapter.swapDataSet(getAlbum().getMedia());
    if (reload) new PreparePhotosTask().execute();
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        displayAlbums();
      }
    });
    albumsMode = editMode = false;
    invalidateOptionsMenu();
  }

  private void displayAlbums() {
    displayAlbums(true);
  }

  private void displayAlbums(boolean reload) {
    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
    toolbar.setTitle(getString(R.string.app_name));
    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    albumsAdapter.swapDataSet(getAlbums().dispAlbums);
    if (reload) new PrepareAlbumTask().execute();
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) { mDrawerLayout.openDrawer(GravityCompat.START); }
    });

    albumsMode = true;
    editMode = false;
    invalidateOptionsMenu();
    mediaAdapter.swapDataSet(new ArrayList<Media>());
    rvMedia.scrollToPosition(0);
  }


  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      fabCamera.setVisibility(View.GONE);
    } else {
      fabCamera.setVisibility(View.VISIBLE);
      fabCamera.animate().translationY(fabCamera.getHeight() * 2).start();
    }
  }

  private boolean displayData(Bundle data){
    if (data!=null) {
      switch (data.getInt(SplashScreen.CONTENT)) {
        case SplashScreen.ALBUMS_PREFETCHED:
          displayAlbums(false);
          pickMode = data.getBoolean(SplashScreen.PICK_MODE);
          toggleRecyclersVisibility(true);
          return true;

        case SplashScreen.ALBUMS_BACKUP:
          displayAlbums(true);
          pickMode = data.getBoolean(SplashScreen.PICK_MODE);
          toggleRecyclersVisibility(true);
          return true;

        case SplashScreen.PHOTOS_PREFETCHED:
          //TODO ask password if hidden
          new Thread(new Runnable() {
            @Override
            public void run() {
              getAlbums().loadAlbums(getApplicationContext(), getAlbum().isHidden());
            }
          }).start();
          displayCurrentAlbumMedia(false);
          toggleRecyclersVisibility(false);
          return true;
      }
    }

    displayAlbums(true);
    return false;
  }

  private void initUi() {

    setSupportActionBar(toolbar);

    /** RVS **/
    rvAlbums.setHasFixedSize(true);
    rvAlbums.setItemAnimator(new DefaultItemAnimator());
    rvMedia.setHasFixedSize(true);
    rvMedia.setItemAnimator(new DefaultItemAnimator());

    albumsAdapter = new AlbumsAdapter(getAlbums().dispAlbums, MainActivity.this);
    albumsAdapter.setOnClickListener(albumOnClickListener);
    albumsAdapter.setOnLongClickListener(albumOnLongCLickListener);
    rvAlbums.setAdapter(albumsAdapter);

    mediaAdapter = new MediaAdapter(getAlbum().getMedia(), MainActivity.this);
    mediaAdapter.setOnClickListener(photosOnClickListener);
    mediaAdapter.setOnLongClickListener(photosOnLongClickListener);
    rvMedia.setAdapter(mediaAdapter);

    int spanCount = SP.getInt("n_columns_folders", 2);
    rvAlbumsDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getApplicationContext()), true);
    rvAlbums.addItemDecoration(rvAlbumsDecoration);
    rvAlbums.setLayoutManager(new GridLayoutManager(this, spanCount));

    spanCount = SP.getInt("n_columns_media", 3);
    rvMediaDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getApplicationContext()), true);
    rvMedia.setLayoutManager(new GridLayoutManager(getApplicationContext(), spanCount));
    rvMedia.addItemDecoration(rvMediaDecoration);

    /**** SWIPE TO REFRESH ****/
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        if (albumsMode) {
          getAlbums().clearSelectedAlbums();
          new PrepareAlbumTask().execute();
        } else {
          getAlbum().clearSelectedMedia();
          new PreparePhotosTask().execute();
        }
      }
    });

    /**** DRAWER ****/
    mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
            mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
      public void onDrawerClosed(View view) {  }
      public void onDrawerOpened(View drawerView) {  }
    });

    findViewById(R.id.ll_drawer_Donate).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, DonateActivity.class));
      }
    });

    findViewById(R.id.ll_drawer_Setting).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
      }
    });

    findViewById(R.id.ll_drawer_About).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
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
        if (!hidden && Security.isPasswordOnHidden(getApplicationContext())){
          Security.askPassword(MainActivity.this, new Security.PasswordInterface() {
            @Override
            public void onSuccess() {
              hidden = true;
              mDrawerLayout.closeDrawer(GravityCompat.START);
              new PrepareAlbumTask().execute();
            }

            @Override
            public void onError() {
              Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
          });
        } else {
          hidden = true;
          mDrawerLayout.closeDrawer(GravityCompat.START);
          new PrepareAlbumTask().execute();
        }
      }
    });

    findViewById(R.id.ll_drawer_Wallpapers).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
      }
    });

    /**** FAB ***/
    fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
    fabCamera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
      }
    });
  }

  @Override
  public void updateUiElements() {
    //TODO: MUST BE FIXED
    toolbar.setPopupTheme(getPopupToolbarStyle());
    toolbar.setBackgroundColor(getPrimaryColor());

    /**** SWIPE TO REFRESH ****/
    swipeRefreshLayout.setColorSchemeColors(getAccentColor());
    swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getBackgroundColor());

    setStatusBarColor();
    setNavBarColor();

    fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
    fabCamera.setVisibility(SP.getBoolean(getString(R.string.preference_show_fab), false) ? View.VISIBLE : View.GONE);
    rvAlbums.setBackgroundColor(getBackgroundColor());
    rvMedia.setBackgroundColor(getBackgroundColor());
    mediaAdapter.updatePlaceholder(getApplicationContext());
    albumsAdapter.updateTheme(getApplicationContext());

    setScrollViewColor((ScrollView) findViewById(R.id.drawer_scrollbar));
    Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
    drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));

    findViewById(R.id.Drawer_Header).setBackgroundColor(getPrimaryColor());
    findViewById(R.id.Drawer_Body).setBackgroundColor(getDrawerBackground());
    findViewById(R.id.drawer_scrollbar).setBackgroundColor(getDrawerBackground());
    findViewById(R.id.Drawer_Body_Divider).setBackgroundColor(getIconColor());

    /** TEXT VIEWS **/
    int color = getTextColor();
    ((TextView) findViewById(R.id.Drawer_Default_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.Drawer_Setting_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.Drawer_Donate_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.Drawer_wallpapers_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.Drawer_About_Item)).setTextColor(color);
    ((TextView) findViewById(R.id.Drawer_hidden_Item)).setTextColor(color);

    /** ICONS **/
    color = getIconColor();
    ((IconicsImageView) findViewById(R.id.Drawer_Default_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.Drawer_Donate_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.Drawer_Setting_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.Drawer_wallpapers_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.Drawer_About_Icon)).setColor(color);
    ((IconicsImageView) findViewById(R.id.Drawer_hidden_Icon)).setColor(color);

    setRecentApp(getString(R.string.app_name));
  }

  private void updateColumnsRvs() {
    int spanCount = SP.getInt("n_columns_folders", 2);
    if ( spanCount != ((GridLayoutManager) rvAlbums.getLayoutManager()).getSpanCount()) {
      rvAlbums.removeItemDecoration(rvAlbumsDecoration);
      rvAlbumsDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getApplicationContext()), true);
      rvAlbums.addItemDecoration(rvAlbumsDecoration);
      rvAlbums.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    spanCount = SP.getInt("n_columns_media", 3);
    if (spanCount != ((GridLayoutManager) rvMedia.getLayoutManager()).getSpanCount()) {
      ((GridLayoutManager) rvMedia.getLayoutManager()).getSpanCount();
      rvMedia.removeItemDecoration(rvMediaDecoration);
      rvMediaDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getApplicationContext()), true);
      rvMedia.setLayoutManager(new GridLayoutManager(getApplicationContext(), spanCount));
      rvMedia.addItemDecoration(rvMediaDecoration);
    }
  }

  //region TESTING

//  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//  @Override
//  public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
//    if (resultCode == RESULT_OK) {
//      if (requestCode == REQUEST_CODE_SD_CARD_PERMISSIONS) {
//        Uri treeUri = resultData.getData();
//        // Persist URI in shared preference so that you can use it later.
//        ContentHelper.saveSdCardInfo(getApplicationContext(), treeUri);
//        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        Toast.makeText(this, R.string.got_permission_wr_sdcard, Toast.LENGTH_SHORT).choseProvider();
//      }
//    }
//  }
//  //endregion
//
//  private void requestSdCardPermissions() {
//    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
//
//    AlertDialogsHelper.getTextDialog(MainActivity.this, dialogBuilder,
//            R.string.sd_card_write_permission_title, R.string.sd_card_permissions_message);
//
//    dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
//      @Override
//      public void onClick(DialogInterface dialogInterface, int i) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
//          startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_SD_CARD_PERMISSIONS);
//      }
//    });
//    dialogBuilder.choseProvider();
//  }

  private void updateSelectedStuff() {
    if (albumsMode) {
      if (editMode) toolbar.setTitle(getAlbums().getSelectedCount() + "/" + getAlbums().dispAlbums.size());
      else {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mDrawerLayout.openDrawer(GravityCompat.START);
          }
        });
      }
    } else {
      if (editMode) toolbar.setTitle(getAlbum().getSelectedMediaCount() + "/" + getAlbum().getMedia().size());
      else {
        toolbar.setTitle(getAlbum().getName());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            displayAlbums();
          }
        });
      }
    }

    if (editMode) {
      toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_check));
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          finishEditMode();
        }
      });
    }
  }

  private void finishEditMode() {
    editMode = false;
    if (albumsMode) {
      getAlbums().clearSelectedAlbums();
      albumsAdapter.notifyDataSetChanged();
    } else {
      getAlbum().clearSelectedMedia();
      mediaAdapter.notifyDataSetChanged();
    }
    invalidateOptionsMenu();
  }

  private void checkNothing() {
    TextView a = (TextView) findViewById(R.id.nothing_to_show);
    a.setTextColor(getTextColor());
    a.setVisibility((albumsMode && getAlbums().dispAlbums.size() == 0) || (!albumsMode && getAlbum().getMedia().size() == 0) ? View.VISIBLE : View.GONE);
  }

  //region MENU
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_albums, menu);

    if (albumsMode) {
      menu.findItem(R.id.select_all).setTitle(
              getString(getAlbums().getSelectedCount() == albumsAdapter.getItemCount()
                      ? R.string.clear_selected
                      : R.string.select_all));
      menu.findItem(R.id.ascending_sort_action).setChecked(getAlbums().getSortingOrder() == SortingOrder.ASCENDING);
      switch (getAlbums().getSortingMode()) {
        case NAME:  menu.findItem(R.id.name_sort_action).setChecked(true); break;
        case SIZE:  menu.findItem(R.id.size_sort_action).setChecked(true); break;
        case DATE: default:
          menu.findItem(R.id.date_taken_sort_action).setChecked(true); break;
        case NUMERIC:  menu.findItem(R.id.numeric_sort_action).setChecked(true); break;
      }

    } else {
      menu.findItem(R.id.select_all).setTitle(getString(
              getAlbum().getSelectedMediaCount() == mediaAdapter.getItemCount()
                      ? R.string.clear_selected
                      : R.string.select_all));
      menu.findItem(R.id.ascending_sort_action).setChecked(getAlbum().settings.getSortingOrder() == SortingOrder.ASCENDING);
      switch (getAlbum().settings.getSortingMode()) {
        case NAME:  menu.findItem(R.id.name_sort_action).setChecked(true); break;
        case SIZE:  menu.findItem(R.id.size_sort_action).setChecked(true); break;
        case TYPE:  menu.findItem(R.id.type_sort_action).setChecked(true); break;
        case DATE: default:
          menu.findItem(R.id.date_taken_sort_action).setChecked(true); break;
        case NUMERIC:  menu.findItem(R.id.numeric_sort_action).setChecked(true); break;
      }
    }


    menu.findItem(R.id.hideAlbumButton).setTitle(hidden ? getString(R.string.unhide) : getString(R.string.hide));
    menu.findItem(R.id.search_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_search));
    menu.findItem(R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
    menu.findItem(R.id.sort_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_sort));
    menu.findItem(R.id.filter_menu).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_filter_list));
    menu.findItem(R.id.sharePhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));

    final MenuItem searchItem = menu.findItem(R.id.search_action);
    final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
    searchView.setQueryHint(getString(R.string.coming_soon));

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    if (albumsMode) {
      editMode = getAlbums().getSelectedCount() != 0;
      menu.setGroupVisible(R.id.album_options_menu, editMode);
      menu.setGroupVisible(R.id.photos_option_men, false);
    } else {
      editMode = getAlbum().thereAreMediaSelected();
      menu.setGroupVisible(R.id.photos_option_men, editMode);
      menu.setGroupVisible(R.id.album_options_menu, !editMode);
    }

    togglePrimaryToolbarOptions(menu);
    updateSelectedStuff();

    menu.findItem(R.id.select_all).setVisible(editMode);
    menu.findItem(R.id.installShortcut).setVisible(albumsMode && editMode);
    menu.findItem(R.id.type_sort_action).setVisible(!albumsMode);
    menu.findItem(R.id.delete_action).setVisible(!albumsMode || editMode);

    menu.findItem(R.id.clear_album_preview).setVisible(!albumsMode && getAlbum().hasCustomCover());
    menu.findItem(R.id.renameAlbum).setVisible((albumsMode && getAlbums().getSelectedCount() == 1) || (!albumsMode && !editMode));
    if (getAlbums().getSelectedCount() == 1)
      menu.findItem(R.id.set_pin_album).setTitle(getAlbums().getSelectedAlbum(0).isPinned() ? getString(R.string.un_pin) : getString(R.string.pin));
    menu.findItem(R.id.set_pin_album).setVisible(albumsMode && getAlbums().getSelectedCount() == 1);
    menu.findItem(R.id.setAsAlbumPreview).setVisible(!albumsMode);
    menu.findItem(R.id.affixPhoto).setVisible(!albumsMode && getAlbum().getSelectedMediaCount() > 1);
    return super.onPrepareOptionsMenu(menu);
  }

  private void togglePrimaryToolbarOptions(final Menu menu) {
    menu.setGroupVisible(R.id.general_action, !editMode);

    if (!editMode) {
      menu.findItem(R.id.filter_menu).setVisible(!albumsMode);
      menu.findItem(R.id.search_action).setVisible(albumsMode);
    }
  }

  //endregion

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

      case R.id.select_all:
        if (albumsMode) {
          if (getAlbums().getSelectedCount() == albumsAdapter.getItemCount()) {
            editMode = false;
            getAlbums().clearSelectedAlbums();
          } else getAlbums().selectAllAlbums();
          albumsAdapter.notifyDataSetChanged();
        } else {
          if (getAlbum().getSelectedMediaCount() == mediaAdapter.getItemCount()) {
            editMode = false;
            getAlbum().clearSelectedMedia();
          } else getAlbum().selectAllMedia();
          mediaAdapter.notifyDataSetChanged();
        }
        invalidateOptionsMenu();
        return true;

      case R.id.set_pin_album:
        getAlbums().getSelectedAlbum(0).togglePinAlbum(getApplicationContext());
        getAlbums().sortAlbums(getApplicationContext());
        getAlbums().clearSelectedAlbums();
        albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        invalidateOptionsMenu();
        return true;

      case R.id.settings:
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        return true;

      case R.id.installShortcut:
        getAlbums().installShortcutForSelectedAlbums(this.getApplicationContext());
        finishEditMode();
        return true;

      case R.id.hideAlbumButton:
        final AlertDialog.Builder hideDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

        AlertDialogsHelper.getTextDialog(MainActivity.this,hideDialogBuilder,
                hidden ? R.string.unhide : R.string.hide,
                hidden ? R.string.unhide_album_message : R.string.hide_album_message);

        hideDialogBuilder.setPositiveButton(getString(hidden ? R.string.unhide : R.string.hide).toUpperCase(), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            if (albumsMode) {
              if (hidden) getAlbums().unHideSelectedAlbums(getApplicationContext());
              else getAlbums().hideSelectedAlbums(getApplicationContext());
              albumsAdapter.notifyDataSetChanged();
              invalidateOptionsMenu();
            } else {
              if(hidden) getAlbums().unHideAlbum(getAlbum().getPath(), getApplicationContext());
              else getAlbums().hideAlbum(getAlbum().getPath(), getApplicationContext());
              displayAlbums(true);
            }
          }
        });
        if (!hidden) {
          hideDialogBuilder.setNeutralButton(this.getString(R.string.white_list).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              startActivity(new Intent(getApplicationContext(), WhiteListActivity.class));
            }
          });
        }
        hideDialogBuilder.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
        hideDialogBuilder.show();
        return true;

      case R.id.delete_action:
        class DeletePhotos extends AsyncTask<String, Integer, Boolean> {
          @Override
          protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
          }

          @Override
          protected Boolean doInBackground(String... arg0) {
            if (albumsMode)
              return getAlbums().deleteSelectedAlbums(MainActivity.this);
            else {
              if (editMode)
                return getAlbum().deleteSelectedMedia(getApplicationContext());
              else {
                boolean succ = getAlbums().deleteAlbum(getAlbum(), getApplicationContext());
                getAlbum().getMedia().clear();
                return succ;
              }
            }
          }

          @Override
          protected void onPostExecute(Boolean result) {
            if (result) {
              if (albumsMode) {
                getAlbums().clearSelectedAlbums();
                albumsAdapter.notifyDataSetChanged();
              } else {
                if (getAlbum().getMedia().size() == 0) {
                  getAlbums().removeCurrentAlbum();
                  albumsAdapter.notifyDataSetChanged();
                  displayAlbums();
                } else
                  mediaAdapter.swapDataSet(getAlbum().getMedia());
              }
            } else requestSdCardPermissions();

            invalidateOptionsMenu();
            checkNothing();
            swipeRefreshLayout.setRefreshing(false);
          }
        }

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
        AlertDialogsHelper.getTextDialog(this, deleteDialog, R.string.delete, albumsMode || !editMode ? R.string.delete_album_message : R.string.delete_photos_message);

        deleteDialog.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
        deleteDialog.setPositiveButton(this.getString(R.string.delete).toUpperCase(), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            if (Security.isPasswordOnDelete(getApplicationContext())) {

              Security.askPassword(MainActivity.this, new Security.PasswordInterface() {
                @Override
                public void onSuccess() {
                  new DeletePhotos().execute();
                }

                @Override
                public void onError() {
                  Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                }
              });
            } else new DeletePhotos().execute();
          }
        });
        deleteDialog.show();
        return true;

      case R.id.sharePhotos:
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sent_to_action));

        ArrayList<Uri> files = new ArrayList<Uri>();
        for (Media f : getAlbum().getSelectedMedia())
          files.add(f.getUri());

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.setType(StringUtils.getGenericMIME(getAlbum().getSelectedMedia(0).getMimeType()));
        finishEditMode();
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
        return true;

      case R.id.all_media_filter:
        if (!albumsMode) {
          getAlbum().filterMedias(getApplicationContext(), FilterMode.ALL);
          mediaAdapter.swapDataSet(getAlbum().getMedia());
          item.setChecked(true);
          checkNothing();
        }
        return true;

      case R.id.video_media_filter:
        if (!albumsMode) {
          getAlbum().filterMedias(getApplicationContext(), FilterMode.VIDEO);
          mediaAdapter.swapDataSet(getAlbum().getMedia());
          item.setChecked(true);
          checkNothing();
        }
        return true;

      case R.id.image_media_filter:
        if (!albumsMode) {
          getAlbum().filterMedias(getApplicationContext(), FilterMode.IMAGES);
          mediaAdapter.swapDataSet(getAlbum().getMedia());
          item.setChecked(true);
          checkNothing();
        }
        return true;

      case R.id.gifs_media_filter:
        if (!albumsMode) {
          getAlbum().filterMedias(getApplicationContext(), FilterMode.GIF);
          mediaAdapter.swapDataSet(getAlbum().getMedia());
          item.setChecked(true);
          checkNothing();
        }
        return true;

      case R.id.name_sort_action:
        if (albumsMode) {
          getAlbums().setDefaultSortingMode(SortingMode.NAME);
          getAlbums().sortAlbums(getApplicationContext());
          albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        } else {
          getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.NAME);
          getAlbum().sortPhotos();
          mediaAdapter.swapDataSet(getAlbum().getMedia());
        }
        item.setChecked(true);
        return true;

      case R.id.date_taken_sort_action:
        if (albumsMode) {
          getAlbums().setDefaultSortingMode(SortingMode.DATE);
          getAlbums().sortAlbums(getApplicationContext());
          albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        } else {
          getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.DATE);
          getAlbum().sortPhotos();
          mediaAdapter.swapDataSet(getAlbum().getMedia());
        }
        item.setChecked(true);
        return true;

      case R.id.size_sort_action:
        if (albumsMode) {
          getAlbums().setDefaultSortingMode(SortingMode.SIZE);
          getAlbums().sortAlbums(getApplicationContext());
          albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        } else {
          getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.SIZE);
          getAlbum().sortPhotos();
          mediaAdapter.swapDataSet(getAlbum().getMedia());
        }
        item.setChecked(true);
        return true;

      case R.id.type_sort_action:
        if (!albumsMode) {
          getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.TYPE);
          getAlbum().sortPhotos();
          mediaAdapter.swapDataSet(getAlbum().getMedia());
          item.setChecked(true);
        }

        return true;

      case R.id.numeric_sort_action:
        if (albumsMode) {
          getAlbums().setDefaultSortingMode(SortingMode.NUMERIC);
          getAlbums().sortAlbums(getApplicationContext());
          albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        } else {
          getAlbum().setDefaultSortingMode(getApplicationContext(), SortingMode.NUMERIC);
          getAlbum().sortPhotos();
          mediaAdapter.swapDataSet(getAlbum().getMedia());
        }
        item.setChecked(true);
        return true;

      case R.id.ascending_sort_action:
        if (albumsMode) {
          getAlbums().setDefaultSortingAscending(item.isChecked() ? SortingOrder.DESCENDING : SortingOrder.ASCENDING);
          getAlbums().sortAlbums(getApplicationContext());
          albumsAdapter.swapDataSet(getAlbums().dispAlbums);
        } else {
          getAlbum().setDefaultSortingAscending(getApplicationContext(), item.isChecked() ? SortingOrder.DESCENDING : SortingOrder.ASCENDING);
          getAlbum().sortPhotos();
          mediaAdapter.swapDataSet(getAlbum().getMedia());
        }
        item.setChecked(!item.isChecked());
        return true;

      //region Affix
      // TODO: 11/21/16 move away from here
      case  R.id.affixPhoto:

        //region Async MediaAffix
        class affixMedia extends AsyncTask<Affix.Options, Integer, Void> {
          private AlertDialog dialog;
          @Override
          protected void onPreExecute() {
            AlertDialog.Builder progressDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

            dialog = AlertDialogsHelper.getProgressDialog(MainActivity.this, progressDialog,
                    getString(R.string.affix), getString(R.string.affix_text));
            dialog.show();
            super.onPreExecute();
          }

          @Override
          protected Void doInBackground(Affix.Options... arg0) {
            ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
            for (int i = 0; i<getAlbum().getSelectedMediaCount(); i++) {
              if(!getAlbum().getSelectedMedia(i).isVideo())
                bitmapArray.add(getAlbum().getSelectedMedia(i).getBitmap());
            }

            if (bitmapArray.size() > 1)
              Affix.AffixBitmapList(getApplicationContext(), bitmapArray, arg0[0]);
            else runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(getApplicationContext(), R.string.affix_error, Toast.LENGTH_SHORT).show();
              }
            });
            return null;
          }
          @Override
          protected void onPostExecute(Void result) {
            editMode = false;
            getAlbum().clearSelectedMedia();
            dialog.dismiss();
            invalidateOptionsMenu();
            mediaAdapter.notifyDataSetChanged();
            new PreparePhotosTask().execute();
          }
        }
        //endregion

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
        final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_affix, null);

        dialogLayout.findViewById(R.id.affix_title).setBackgroundColor(getPrimaryColor());
        ((CardView) dialogLayout.findViewById(R.id.affix_card)).setCardBackgroundColor(getCardBackgroundColor());

        //ITEMS
        final SwitchCompat swVertical = (SwitchCompat) dialogLayout.findViewById(R.id.affix_vertical_switch);
        final SwitchCompat swSaveHere = (SwitchCompat) dialogLayout.findViewById(R.id.save_here_switch);

        final LinearLayout llSwVertical = (LinearLayout) dialogLayout.findViewById(R.id.ll_affix_vertical);
        final LinearLayout llSwSaveHere = (LinearLayout) dialogLayout.findViewById(R.id.ll_affix_save_here);

        final RadioGroup radioFormatGroup = (RadioGroup) dialogLayout.findViewById(R.id.radio_format);

        final TextView txtQuality = (TextView) dialogLayout.findViewById(R.id.affix_quality_title);
        final SeekBar seekQuality = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_quality);

        //region THEME STUFF
        setScrollViewColor((ScrollView) dialogLayout.findViewById(R.id.affix_scrollView));

        /** TextViews **/
        int color = getTextColor();
        ((TextView) dialogLayout.findViewById(R.id.affix_vertical_title)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.compression_settings_title)).setTextColor(color);
        ((TextView) dialogLayout.findViewById(R.id.save_here_title)).setTextColor(color);

        /** Sub TextViews **/
        color = getSubTextColor();
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

        themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_jpeg));
        themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_png));
        themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_webp));

        setSwitchColor(swVertical, getAccentColor());
        setSwitchColor(swSaveHere, getAccentColor());
        //endregion

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
        seekQuality.setProgress(50); //DEFAULT

        swVertical.setClickable(false);
        llSwVertical.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            swVertical.setChecked(!swVertical.isChecked());
            setSwitchColor(swVertical, getAccentColor());
          }
        });

        swSaveHere.setClickable(false);
        llSwSaveHere.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            swSaveHere.setChecked(!swSaveHere.isChecked());
            setSwitchColor(swSaveHere, getAccentColor());
          }
        });

        builder.setView(dialogLayout);
        builder.setPositiveButton(this.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            Bitmap.CompressFormat compressFormat;
            switch (radioFormatGroup.getCheckedRadioButtonId()) {
              case R.id.radio_jpeg: default:
                compressFormat = Bitmap.CompressFormat.JPEG; break;
              case R.id.radio_png:
                compressFormat = Bitmap.CompressFormat.PNG; break;
              case R.id.radio_webp:
                compressFormat = Bitmap.CompressFormat.WEBP; break;
            }

            Affix.Options options = new Affix.Options(
                    swSaveHere.isChecked() ? getAlbum().getPath() : Affix.getDefaultDirectoryPath(),
                    compressFormat,
                    seekQuality.getProgress(),
                    swVertical.isChecked());
            new affixMedia().execute(options);
          }});
        builder.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
        builder.show();


        return true;
      //endregion

      case R.id.action_move:
        SelectAlbumBuilder.with(getSupportFragmentManager())
                .title(getString(R.string.move_to))
                .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                  @Override
                  public void folderSelected(String path) {
                    swipeRefreshLayout.setRefreshing(true);
                    if (getAlbum().moveSelectedMedia(getApplicationContext(), path) > 0) {
                      if (getAlbum().getMedia().size() == 0) {
                        getAlbums().removeCurrentAlbum();
                        albumsAdapter.notifyDataSetChanged();
                        displayAlbums();
                      }
                      mediaAdapter.swapDataSet(getAlbum().getMedia());
                      finishEditMode();
                      invalidateOptionsMenu();
                    } else requestSdCardPermissions();

                    swipeRefreshLayout.setRefreshing(false);
                  }}).show();
        return true;

      case R.id.action_copy:
        SelectAlbumBuilder.with(getSupportFragmentManager())
              .title(getString(R.string.copy_to))
                .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                  @Override
                  public void folderSelected(String path) {
                    boolean success = getAlbum().copySelectedPhotos(getApplicationContext(), path);
                    finishEditMode();

                    if (!success) // TODO: 11/21/16 handle in other way
                      requestSdCardPermissions();
                  }
                }).show();

        return true;

      case R.id.renameAlbum:
        AlertDialog.Builder renameDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
        final EditText editTextNewName = new EditText(getApplicationContext());
        editTextNewName.setText(albumsMode ? getAlbums().getSelectedAlbum(0).getName() : getAlbum().getName());

        AlertDialogsHelper.getInsertTextDialog(MainActivity.this, renameDialogBuilder,
                editTextNewName, R.string.rename_album);

        renameDialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), null);

        renameDialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //This should br empty it will be overwrite later
            //to avoid dismiss of the dialog
          }
        });
        final AlertDialog renameDialog = renameDialogBuilder.create();
        renameDialog.show();

        renameDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener( new View.OnClickListener() {
          @Override
          public void onClick(View dialog) {
            if (editTextNewName.length() != 0) {
              swipeRefreshLayout.setRefreshing(true);
              boolean success;
              if (albumsMode){

                int index = getAlbums().dispAlbums.indexOf(getAlbums().getSelectedAlbum(0));
                getAlbums().getAlbum(index).updatePhotos(getApplicationContext());
                success = getAlbums().getAlbum(index).renameAlbum(getApplicationContext(),
                        editTextNewName.getText().toString());
                albumsAdapter.notifyItemChanged(index);
              } else {
                success = getAlbum().renameAlbum(getApplicationContext(), editTextNewName.getText().toString());
                toolbar.setTitle(getAlbum().getName());
                mediaAdapter.notifyDataSetChanged();
              }
              renameDialog.dismiss();
              if (!success) requestSdCardPermissions();
              swipeRefreshLayout.setRefreshing(false);
            } else {
              StringUtils.showToast(getApplicationContext(), getString(R.string.insert_something));
              editTextNewName.requestFocus();
            }
          }});
        return true;

      case R.id.clear_album_preview:
        if (!albumsMode) {
          getAlbum().removeCoverAlbum(getApplicationContext());
        }
        return true;

      case R.id.setAsAlbumPreview:
        if (!albumsMode) {
          getAlbum().setSelectedPhotoAsPreview(getApplicationContext());
          finishEditMode();
        }
        return true;

      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
  }

  private void toggleRecyclersVisibility(boolean albumsMode){
    rvAlbums.setVisibility(albumsMode ? View.VISIBLE : View.GONE);
    rvMedia.setVisibility(albumsMode ? View.GONE : View.VISIBLE);
  }

  @Override
  public void onBackPressed() {
    if (editMode) finishEditMode();
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

  private class PrepareAlbumTask extends AsyncTask<Void, Integer, Void> {

    @Override
    protected void onPreExecute() {
      swipeRefreshLayout.setRefreshing(true);
      toggleRecyclersVisibility(true);
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
      getAlbums().loadAlbums(getApplicationContext(), hidden);
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      albumsAdapter.swapDataSet(getAlbums().dispAlbums);
      checkNothing();
      swipeRefreshLayout.setRefreshing(false);
      getAlbums().saveBackup(getApplicationContext());
    }
  }

  private class PreparePhotosTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      swipeRefreshLayout.setRefreshing(true);
      toggleRecyclersVisibility(false);
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
      getAlbum().updatePhotos(getApplicationContext());
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      mediaAdapter.swapDataSet(getAlbum().getMedia());
      if (!hidden)
        HandlingAlbums.addAlbumToBackup(getApplicationContext(), getAlbum());
      checkNothing();
      swipeRefreshLayout.setRefreshing(false);
    }
  }
}
