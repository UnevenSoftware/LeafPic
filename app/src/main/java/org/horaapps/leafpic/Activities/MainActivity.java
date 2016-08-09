package org.horaapps.leafpic.Activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.horaapps.leafpic.Adapters.AlbumsAdapter;
import org.horaapps.leafpic.Adapters.MediaAdapter;
import org.horaapps.leafpic.Data.AlbumSettings;
import org.horaapps.leafpic.Data.CustomAlbumsHandler;
import org.horaapps.leafpic.Data.ImageFileFilter;
import org.horaapps.leafpic.Data.Media;
import org.horaapps.leafpic.SelectAlbumBottomSheet;
import org.horaapps.leafpic.Views.GridSpacingItemDecoration;
import org.horaapps.leafpic.Views.SharedMediaActivity;
import org.horaapps.leafpic.utils.AffixMedia;
import org.horaapps.leafpic.utils.AffixOptions;
import org.horaapps.leafpic.utils.AlertDialogsHelper;
import org.horaapps.leafpic.utils.ColorPalette;
import org.horaapps.leafpic.utils.ContentHelper;
import org.horaapps.leafpic.utils.Measure;
import org.horaapps.leafpic.utils.PreferenceUtil;
import org.horaapps.leafpic.utils.SecurityHelper;
import org.horaapps.leafpic.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends SharedMediaActivity {

  private static String TAG = "AlbumsAct";
  private int REQUEST_CODE_SD_CARD_PERMISSIONS = 42;

  private CustomAlbumsHandler customAlbumsHandler = new CustomAlbumsHandler(MainActivity.this);
  private PreferenceUtil SP;
  private SecurityHelper securityObj;

  private RecyclerView rvAlbums;
  private AlbumsAdapter albumsAdapter;
  private GridSpacingItemDecoration rvAlbumsDecoration;

  private RecyclerView rvMedia;
  private MediaAdapter mediaAdapter;
  private GridSpacingItemDecoration rvMediaDecoration;

  private FloatingActionButton fabCamera;
  private DrawerLayout mDrawerLayout;
  private Toolbar toolbar;
  private SelectAlbumBottomSheet bottomSheetDialogFragment;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RelativeLayout relativeLayoutMainContent;

  //private TouchScrollBar touchScrollBar;

  private boolean hidden = false, pickMode = false, editMode = false, albumsMode = true, firstLaunch = true;

  private View.OnLongClickListener photosOnLongClickListener = new View.OnLongClickListener() {
	@Override
	public boolean onLongClick(View v) {
	  int index = Integer.parseInt(v.findViewById(org.horaapps.leafpic.R.id.photo_path).getTag().toString());
	  if (!editMode) {
		// If it is the first long press
		mediaAdapter.notifyItemChanged(getAlbum().toggleSelectPhoto(index));
		editMode = true;
	  } else
		getAlbum().selectAllPhotosUpTo(index, mediaAdapter);

	  invalidateOptionsMenu();
	  return true;
	}
  };

  private View.OnClickListener photosOnClickListener = new View.OnClickListener() {
	@Override
	public void onClick(View v) {
	  int index = Integer.parseInt(v.findViewById(org.horaapps.leafpic.R.id.photo_path).getTag().toString());
	  if (!pickMode) {
		if (editMode) {
		  mediaAdapter.notifyItemChanged(getAlbum().toggleSelectPhoto(index));
		  invalidateOptionsMenu();
		} else {
		  getAlbum().setCurrentPhotoIndex(index);
		  Intent intent = new Intent(MainActivity.this, PhotoPagerActivity.class);
		  intent.setAction(PhotoPagerActivity.ACTION_OPEN_ALBUM);
		  startActivity(intent);
		}
	  } else {
		setResult(RESULT_OK, new Intent().setData(getAlbum().getMedia(index).getUri()));
		finish();
	  }

	}
  };

  private View.OnLongClickListener albumOnLongCLickListener = new View.OnLongClickListener() {
	@Override
	public boolean onLongClick(View v) {
	  int index = Integer.parseInt(v.findViewById(org.horaapps.leafpic.R.id.album_name).getTag().toString());
	  albumsAdapter.notifyItemChanged(getAlbums().toggleSelectAlbum(index));
	  editMode = true;
	  invalidateOptionsMenu();
	  return true;
	}
  };

  private View.OnClickListener albumOnClickListener = new View.OnClickListener() {
	@Override
	public void onClick(View v) {
	  int index = Integer.parseInt(v.findViewById(org.horaapps.leafpic.R.id.album_name).getTag().toString());
	  if (editMode) {
		albumsAdapter.notifyItemChanged(getAlbums().toggleSelectAlbum(index));
		invalidateOptionsMenu();
	  } else {
		getAlbums().setCurrentAlbumIndex(index);
		displayCurrentAlbumMedia(true);
		setRecentApp(getAlbums().getCurrentAlbum().getName());
	  }
	}
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(org.horaapps.leafpic.R.layout.activity_main);

	SP = PreferenceUtil.getInstance(getApplicationContext());
	albumsMode = true;
	editMode = false;
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
	getAlbums().clearSelectedAlbums();
	getAlbum().clearSelectedPhotos();
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
	getAlbum().setSettings(getApplicationContext());
	toolbar.setTitle(getAlbum().getName());
	toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
	mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	if (reload) {
	  //display available medias before reload
	  mediaAdapter.swapDataSet(getAlbum().getMedia());
	  new PreparePhotosTask().execute();
	}
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
	if (SP.getBoolean("auto_update_media", false))
	  displayAlbums(true);
	else {
	  displayAlbums(false);
	  albumsAdapter.swapDataSet(getAlbums().dispAlbums);
	  toggleRecyclersVisibilty(true);
	}
  }

  private void displayAlbums(boolean reload) {
	toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_menu));
	toolbar.setTitle(getString(org.horaapps.leafpic.R.string.app_name));
	mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

	if (reload) new PrepareAlbumTask().execute();

	toolbar.setNavigationOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) { mDrawerLayout.openDrawer(GravityCompat.START); }
	});

	albumsMode = true;
	editMode = false;
	invalidateOptionsMenu();
	mediaAdapter.swapDataSet(new ArrayList<Media>());
	rvMedia.scrollToPosition(0);}


  @Override
  public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig);

	// rearrange column number
	int nSpan = Measure.getAlbumsColumns(MainActivity.this);
	rvAlbums.setLayoutManager(new GridLayoutManager(this, nSpan));
	rvAlbums.removeItemDecoration(rvAlbumsDecoration);
	rvAlbumsDecoration = new GridSpacingItemDecoration(nSpan, Measure.pxToDp(3, getApplicationContext()), true);
	rvAlbums.addItemDecoration(rvAlbumsDecoration);

	// TODO: 07/08/16 not change this
	//changeSpanCountRvMedia(Measure.getPhotosColumns(MainActivity.this));

	int status_height = Measure.getStatusBarHeight(getResources()),
			navBarHeight =  Measure.getNavBarHeight(MainActivity.this);

	if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	  getWindow().getDecorView().setSystemUiVisibility(
			  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

	  //rvAlbums.setPadding(0, 0, 0, status_height);
	  //rvMedia.setPadding(0, 0, 0, status_height);
	  //touchScrollBar.setPadding(0, 0, 0, status_height);

	  relativeLayoutMainContent.setPadding(0, 0, 0, status_height);
	  fabCamera.setVisibility(View.GONE);
	}
	else {
	  getWindow().getDecorView().setSystemUiVisibility(
			  View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
	  toolbar.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();
	  swipeRefreshLayout.animate().translationY(status_height).setInterpolator(new DecelerateInterpolator()).start();

	  //rvAlbums.setPadding(0, 0, 0, status_height + navBarHeight);
	  //rvMedia.setPadding(0, 0, 0, status_height + navBarHeight);
	  //touchScrollBar.setPadding(0, 0, 0, status_height + navBarHeight);
	  relativeLayoutMainContent.setPadding(0, 0, 0, status_height + navBarHeight);

	  fabCamera.animate().translationY(fabCamera.getHeight() * 2).start();
	}
  }

  private void displayPreFetchedData(Bundle data){
	try {
	  if (data!=null) {
		int content = data.getInt(SplashScreen.CONTENT);
		switch (content) {
		  case SplashScreen.ALBUMS_PREFETCHED:
			displayAlbums(false);
			pickMode = data.getBoolean(SplashScreen.PICK_MODE);
			albumsAdapter.swapDataSet(getAlbums().dispAlbums);
			toggleRecyclersVisibilty(true);
			break;

		  case SplashScreen.ALBUMS_BACKUP:
			albumsAdapter.swapDataSet(getAlbums().dispAlbums);
			displayAlbums(true);
			pickMode = data.getBoolean(SplashScreen.PICK_MODE);
			toggleRecyclersVisibilty(true);
			break;

		  case SplashScreen.PHOTS_PREFETCHED:
			//TODO ask password if hidden
			new Thread(new Runnable() {
			  @Override
			  public void run() {
				getAlbums().loadAlbums(getApplicationContext(), getAlbum().isHidden());
			  }
			}).start();
			displayCurrentAlbumMedia(false);
			mediaAdapter.swapDataSet(getAlbum().getMedia());
			toggleRecyclersVisibilty(false);
			break;
		}
	  } else
		displayAlbums(true);

	} catch (NullPointerException e) { e.printStackTrace(); }

  }

  private void initUI() {

	/**** TOOLBAR ****/
	toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
	setSupportActionBar(toolbar);

	/**** RECYCLER VIEW ****/
	rvAlbums = (RecyclerView) findViewById(org.horaapps.leafpic.R.id.grid_albums);
	rvMedia = ((RecyclerView) findViewById(org.horaapps.leafpic.R.id.grid_photos));
	rvAlbums.setHasFixedSize(true);
	rvAlbums.setItemAnimator(new DefaultItemAnimator());
	rvMedia.setHasFixedSize(true);
	rvMedia.setItemAnimator(new DefaultItemAnimator());

	changeSpanCountRvMedia(SP.getInt("span_count", Measure.getPhotosColumns(getApplicationContext())));

	rvAlbumsDecoration = new GridSpacingItemDecoration(Measure.getAlbumsColumns(MainActivity.this), Measure.pxToDp(3, getApplicationContext()), true);
	rvAlbums.addItemDecoration(rvAlbumsDecoration);


	albumsAdapter = new AlbumsAdapter(getAlbums().dispAlbums, MainActivity.this);
	rvAlbums.setLayoutManager(new GridLayoutManager(this, Measure.getAlbumsColumns(getApplicationContext())));

	albumsAdapter.setOnClickListener(albumOnClickListener);
	albumsAdapter.setOnLongClickListener(albumOnLongCLickListener);
	rvAlbums.setAdapter(albumsAdapter);

	mediaAdapter = new MediaAdapter(getAlbum().getMedia(), MainActivity.this);

	mediaAdapter.setOnClickListener(photosOnClickListener);
	mediaAdapter.setOnLongClickListener(photosOnLongClickListener);
	rvMedia.setAdapter(mediaAdapter);

	//set scale gesture detector
	final ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
	  @Override
	  public boolean onScaleBegin(ScaleGestureDetector detector) {
		swipeRefreshLayout.setEnabled(false);
		return super.onScaleBegin(detector);
	  }

	  @Override
	  public boolean onScale(ScaleGestureDetector detector) {
		int spanCount = ((GridLayoutManager) rvMedia.getLayoutManager()).getSpanCount();
		if (detector.getCurrentSpan() > 200 && detector.getTimeDelta() > 400) {
		  if (detector.getCurrentSpan() > detector.getPreviousSpan()) {
			changeSpanCountRvMedia(spanCount - 1);
			return true;
		  } else if(detector.getCurrentSpan() < detector.getPreviousSpan()) {
			changeSpanCountRvMedia(spanCount + 1);
			return true;
		  }
		}

		return false;
	  }

	  @Override
	  public void onScaleEnd(ScaleGestureDetector detector) {
		super.onScaleEnd(detector);
		swipeRefreshLayout.setEnabled(true);
	  }
	});

	rvMedia.setOnTouchListener(new View.OnTouchListener() {
	  @Override
	  public boolean onTouch(View v, MotionEvent event) {
		mScaleGestureDetector.onTouchEvent(event);
		return false;
	  }
	});


	/**** SWIPE TO REFRESH ****/
	swipeRefreshLayout = (SwipeRefreshLayout) findViewById(org.horaapps.leafpic.R.id.swipeRefreshLayout);
	swipeRefreshLayout.setColorSchemeColors(getAccentColor());
	swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getBackgroundColor());
	swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
	  @Override
	  public void onRefresh() {
		if (albumsMode) {
		  getAlbums().clearSelectedAlbums();
		  new PrepareAlbumTask().execute();
		} else {
		  getAlbum().clearSelectedPhotos();
		  new PreparePhotosTask().execute();
		}
	  }
	});

	/**** DRAWER ****/
	mDrawerLayout = (DrawerLayout) findViewById(org.horaapps.leafpic.R.id.drawer_layout);
	mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
																	 mDrawerLayout, toolbar, org.horaapps.leafpic.R.string.drawer_open, org.horaapps.leafpic.R.string.drawer_close) {
	  public void onDrawerClosed(View view) {
		//Put your code here
		// materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
	  }

	  public void onDrawerOpened(View drawerView) {
		//Put your code here
		//materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
	  }
	});

	/**** FAB ***/
	fabCamera = (FloatingActionButton) findViewById(org.horaapps.leafpic.R.id.fab_camera);
	fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
	fabCamera.animate().translationY(-Measure.getNavBarHeight(MainActivity.this)).setInterpolator(new DecelerateInterpolator(2)).start();
	fabCamera.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		if (!albumsMode && getAlbum().areFiltersActive()) {
		  getAlbum().filterMedias(ImageFileFilter.FILTER_ALL);
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		  checkNothing();
		  toolbar.getMenu().findItem(org.horaapps.leafpic.R.id.all_media_filter).setChecked(true);
		  fabCamera.setImageDrawable(new IconicsDrawable(MainActivity.this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
		} else startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
	  }
	});

	//region TESTING
	fabCamera.setOnLongClickListener(new View.OnLongClickListener() {
	  @Override
	  public boolean onLongClick(View v) {

		// NOTE: this is used to acquire write permission on sd with api 21
		// TODO call this one when unable to write on sd
		requestSdCardPermissions();
		return false;
	  }
	});
	//endregion


	int statusBarHeight = Measure.getStatusBarHeight(getResources()),
			navBarHeight = Measure.getNavBarHeight(MainActivity.this);
	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
	toolbar.animate().translationY(statusBarHeight).setInterpolator(new DecelerateInterpolator()).start();

	swipeRefreshLayout.animate().translationY(statusBarHeight).setInterpolator(new DecelerateInterpolator()).start();

	//rvAlbums.setPadding(0, 0, 0, statusBarHeight + navBarHeight);
	//rvMedia.setPadding(0, 0, 0, statusBarHeight + navBarHeight);

	relativeLayoutMainContent=(RelativeLayout) findViewById(org.horaapps.leafpic.R.id.rl_main_content);
	relativeLayoutMainContent.setPadding(0, 0, 0, statusBarHeight + navBarHeight);

	/**** SCROLLBAR ****/

	//touchScrollBar = (TouchScrollBar) findViewById(R.id.touchScrollBar);
	//touchScrollBar.setHandleColour(getAccentColor());
	//touchScrollBar.setHandleOffColour(getPrimaryColor());
	//touchScrollBar.setBarColour((ColorPalette.getTransparentColor(getInvertedBackgroundColor(),160)));
	//touchScrollBar.setHideDuration(1500);
	setRecentApp(getString(org.horaapps.leafpic.R.string.app_name));

	Display aa = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

	if (aa.getRotation() == Surface.ROTATION_90) {
	  Configuration configuration = new Configuration();
	  configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
	  onConfigurationChanged(configuration);
	}
  }

  private void changeSpanCountRvMedia(int spanCount) {
	if (spanCount > 0 && spanCount < 9) {
	  rvMedia.removeItemDecoration(rvMediaDecoration);
	  rvMediaDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getApplicationContext()), true);
	  rvMedia.setLayoutManager(new GridLayoutManager(getApplicationContext(), spanCount));
	  rvMedia.addItemDecoration(rvMediaDecoration);

	  SP.putInt("span_count", spanCount);
	}
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (!albumsMode) {
	  int spanCount = ((GridLayoutManager) rvMedia.getLayoutManager()).getSpanCount();
	  if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
		changeSpanCountRvMedia(spanCount - 1);
		return true;
	  } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
		changeSpanCountRvMedia(spanCount + 1);
		return true;
	  }
	}
	return super.onKeyDown(keyCode, event);  }

  //region TESTING

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  @Override
  public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
	if (resultCode == RESULT_OK) {
	  if (requestCode == REQUEST_CODE_SD_CARD_PERMISSIONS) {
		Uri treeUri = resultData.getData();
		// Persist URI in shared preference so that you can use it later.
		ContentHelper.setSharedPreferenceUri(getApplicationContext(), org.horaapps.leafpic.R.string
																			 .preference_internal_uri_extsdcard_photos, treeUri);

		final int takeFlags = resultData.getFlags()
									  & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
		Toast.makeText(this, org.horaapps.leafpic.R.string.got_oermission_wr_sdcard, Toast.LENGTH_SHORT).show();
	  }
	}
  }
  //endregion

  private void requestSdCardPermissions() {
	final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

	AlertDialogsHelper.getTextDialog(MainActivity.this, dialogBuilder,
			org.horaapps.leafpic.R.string.sd_card_write_permission_title, org.horaapps.leafpic.R.string.sd_card_permissions_message);

	dialogBuilder.setPositiveButton(org.horaapps.leafpic.R.string.ok_action, new DialogInterface.OnClickListener() {
	  @Override
	  public void onClick(DialogInterface dialogInterface, int i) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
		  startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_SD_CARD_PERMISSIONS);
	  }
	});
	dialogBuilder.show();
  }

  @Override
  public void setNavBarColor() {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  if (isNavigationBarColored())
		super.setNavBarColor();
	  else
		getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(
				ContextCompat.getColor(getApplicationContext(), org.horaapps.leafpic.R.color.md_black_1000), 110));
	}
  }

  //region UI/GRAPHIC
  private void setupUI() {
	//TODO: MUST BE FIXED
	toolbar.setPopupTheme(getPopupToolbarStyle());
	toolbar.setBackgroundColor(getPrimaryColor());

	/**** SWIPE TO REFRESH ****/
	swipeRefreshLayout.setColorSchemeColors(getAccentColor());
	swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getBackgroundColor());

	setStatusBarColor();
	setNavBarColor();

	fabCamera.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
	setDrawerTheme();
	rvAlbums.setBackgroundColor(getBackgroundColor());
	rvMedia.setBackgroundColor(getBackgroundColor());
	mediaAdapter.updatePlaceholder(getApplicationContext());
	albumsAdapter.updateTheme();
	/**** DRAWER ****/
	setScrollViewColor((ScrollView) findViewById(org.horaapps.leafpic.R.id.drawer_scrollbar));

	/**** recyclers drawable *****/
	Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), org.horaapps.leafpic.R.drawable.ic_scrollbar);
	drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));
  }

  private void setDrawerTheme() {

	findViewById(org.horaapps.leafpic.R.id.Drawer_Header).setBackgroundColor(getPrimaryColor());
	findViewById(org.horaapps.leafpic.R.id.Drawer_Body).setBackgroundColor(getDrawerBackground());
	findViewById(org.horaapps.leafpic.R.id.drawer_scrollbar).setBackgroundColor(getDrawerBackground());
	findViewById(org.horaapps.leafpic.R.id.Drawer_Body_Divider).setBackgroundColor(getIconColor());

	/** TEXT VIEWS **/
	int color = getTextColor();
	((TextView) findViewById(org.horaapps.leafpic.R.id.Drawer_Default_Item)).setTextColor(color);
	((TextView) findViewById(org.horaapps.leafpic.R.id.Drawer_Setting_Item)).setTextColor(color);
	((TextView) findViewById(org.horaapps.leafpic.R.id.Drawer_Donate_Item)).setTextColor(color);
	((TextView) findViewById(org.horaapps.leafpic.R.id.Drawer_wallpapers_Item)).setTextColor(color);
	((TextView) findViewById(org.horaapps.leafpic.R.id.Drawer_About_Item)).setTextColor(color);
	((TextView) findViewById(org.horaapps.leafpic.R.id.Drawer_hidden_Item)).setTextColor(color);

	/** ICONS **/
	color = getIconColor();
	((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.Drawer_Default_Icon)).setColor(color);
	((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.Drawer_Donate_Icon)).setColor(color);
	((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.Drawer_Setting_Icon)).setColor(color);
	((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.Drawer_wallpapers_Icon)).setColor(color);
	((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.Drawer_About_Icon)).setColor(color);
	((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.Drawer_hidden_Icon)).setColor(color);

	/** CLICK LISTENERS **/
	findViewById(org.horaapps.leafpic.R.id.ll_drawer_Donate).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Intent intent = new Intent(MainActivity.this, DonateActivity.class);
		startActivity(intent);
	  }
	});
	findViewById(org.horaapps.leafpic.R.id.ll_drawer_Setting).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
	  }
	});

	findViewById(org.horaapps.leafpic.R.id.ll_drawer_About).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Intent intent = new Intent(MainActivity.this, AboutActivity.class);
		startActivity(intent);
	  }
	});

	findViewById(org.horaapps.leafpic.R.id.ll_drawer_Default).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		hidden = false;
		mDrawerLayout.closeDrawer(GravityCompat.START);
		new PrepareAlbumTask().execute();
	  }
	});
	findViewById(org.horaapps.leafpic.R.id.ll_drawer_hidden).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		if (securityObj.isActiveSecurity() && securityObj.isPasswordOnHidden()){

		  AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder (MainActivity.this, getDialogStyle());
		  final EditText editTextPassword = securityObj.getInsertPasswordDialog(MainActivity.this, passwordDialogBuilder);
		  passwordDialogBuilder.setPositiveButton(getString(org.horaapps.leafpic.R.string.ok_action), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		  });

		  passwordDialogBuilder.setNegativeButton(getString(org.horaapps.leafpic.R.string.cancel), null);

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
				Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.wrong_password, Toast.LENGTH_SHORT).show();
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

	findViewById(org.horaapps.leafpic.R.id.ll_drawer_Wallpapers).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
	  }
	});
  }
  //endregion


  private void updateSelectedStuff() {
	int c;
	try {
	  if (albumsMode) {
		if ((c = getAlbums().getSelectedCount()) != 0) {
		  toolbar.setTitle(c + "/" + getAlbums().dispAlbums.size());
		  toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_check));
		  toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			  editMode = false;
			  getAlbums().clearSelectedAlbums();
			  albumsAdapter.notifyDataSetChanged();
			  invalidateOptionsMenu();
			}
		  });
		  toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			  if (getAlbums().getSelectedCount() == getAlbums().dispAlbums.size())
				getAlbums().clearSelectedAlbums();
			  else getAlbums().selectAllAlbums();
			  albumsAdapter.notifyDataSetChanged();
			  invalidateOptionsMenu();
			}
		  });
		} else {
		  toolbar.setTitle(getString(org.horaapps.leafpic.R.string.app_name));
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
		if ((c = getAlbum().getSelectedCount()) != 0) {
		  toolbar.setTitle(c + "/" + getAlbum().getMedia().size());
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
			  if (getAlbum().getSelectedCount() == getAlbum().getMedia().size())
				getAlbum().clearSelectedPhotos();
			  else getAlbum().selectAllPhotos();
			  mediaAdapter.notifyDataSetChanged();
			  invalidateOptionsMenu();
			}
		  });
		} else {
		  toolbar.setTitle(getAlbum().getName());
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
	editMode = false;
	if (albumsMode) {
	  getAlbums().clearSelectedAlbums();
	  albumsAdapter.notifyDataSetChanged();
	} else {
	  getAlbum().clearSelectedPhotos();
	  mediaAdapter.notifyDataSetChanged();
	}
	invalidateOptionsMenu();
  }

  private void checkNothing() {
	TextView a = (TextView) findViewById(org.horaapps.leafpic.R.id.nothing_to_show);
	a.setTextColor(getTextColor());
	a.setVisibility((albumsMode && getAlbums().dispAlbums.size() == 0) || (!albumsMode && getAlbum().getMedia().size() == 0) ? View.VISIBLE : View.GONE);
  }

  //region MENU
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(org.horaapps.leafpic.R.menu.menu_albums, menu);

	if (albumsMode) {
	  menu.findItem(org.horaapps.leafpic.R.id.select_all).setTitle(
			  getString(getAlbums().getSelectedCount() == albumsAdapter.getItemCount()
								? org.horaapps.leafpic.R.string.clear_selected
								: org.horaapps.leafpic.R.string.select_all));
	  menu.findItem(org.horaapps.leafpic.R.id.ascending_sort_action).setChecked(getAlbums().isAscending());
	  switch (getAlbums().getColumnSortingMode()) {
		case AlbumSettings.SORT_BY_NAME:  menu.findItem(org.horaapps.leafpic.R.id.name_sort_action).setChecked(true); break;
		case AlbumSettings.SORT_BY_SIZE:  menu.findItem(org.horaapps.leafpic.R.id.size_sort_action).setChecked(true); break;
		case AlbumSettings.SORT_BY_DATE:
		default:
		  menu.findItem(org.horaapps.leafpic.R.id.date_taken_sort_action).setChecked(true);
		  break;
	  }

	} else {
	  menu.findItem(org.horaapps.leafpic.R.id.select_all).setTitle(getString(
			  getAlbum().getSelectedCount() == mediaAdapter.getItemCount()
					  ? org.horaapps.leafpic.R.string.clear_selected
					  : org.horaapps.leafpic.R.string.select_all));
	  menu.findItem(org.horaapps.leafpic.R.id.ascending_sort_action).setChecked(getAlbum().settings.ascending);
	  switch (getAlbum().settings.columnSortingMode) {
		case AlbumSettings.SORT_BY_NAME:  menu.findItem(org.horaapps.leafpic.R.id.name_sort_action).setChecked(true); break;
		case AlbumSettings.SORT_BY_SIZE:  menu.findItem(org.horaapps.leafpic.R.id.size_sort_action).setChecked(true); break;
		case AlbumSettings.SORT_BY_TYPE:  menu.findItem(org.horaapps.leafpic.R.id.type_sort_action).setChecked(true); break;
		case AlbumSettings.SORT_BY_DATE:
		default:
		  menu.findItem(org.horaapps.leafpic.R.id.date_taken_sort_action).setChecked(true);
		  break;
	  }
	}
	menu.findItem(org.horaapps.leafpic.R.id.hideAlbumButton).setTitle(hidden ? getString(org.horaapps.leafpic.R.string.unhide) : getString(org.horaapps.leafpic.R.string.hide));
	menu.findItem(org.horaapps.leafpic.R.id.search_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_search));
	menu.findItem(org.horaapps.leafpic.R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));
	menu.findItem(org.horaapps.leafpic.R.id.sort_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_sort));
	menu.findItem(org.horaapps.leafpic.R.id.filter_menu).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_filter_list));
	menu.findItem(org.horaapps.leafpic.R.id.sharePhotos).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
	menu.findItem(org.horaapps.leafpic.R.id.delete_action).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_delete));

	final MenuItem searchItem = menu.findItem(org.horaapps.leafpic.R.id.search_action);
	final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	searchView.setQueryHint(getString(org.horaapps.leafpic.R.string.coming_soon));

	return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
	if (albumsMode) {
	  editMode = getAlbums().getSelectedCount() != 0;
	  menu.setGroupVisible(org.horaapps.leafpic.R.id.album_options_menu, editMode);
	  menu.setGroupVisible(org.horaapps.leafpic.R.id.photos_option_men, false);
	} else {
	  editMode = getAlbum().areMediaSelected();
	  menu.setGroupVisible(org.horaapps.leafpic.R.id.photos_option_men, editMode);
	  menu.setGroupVisible(org.horaapps.leafpic.R.id.album_options_menu, !editMode);
	}

	togglePrimaryToolbarOptions(menu);
	updateSelectedStuff();

	menu.findItem(org.horaapps.leafpic.R.id.select_all).setVisible(editMode);
	menu.findItem(org.horaapps.leafpic.R.id.installShortcut).setVisible(albumsMode && editMode);
	menu.findItem(org.horaapps.leafpic.R.id.type_sort_action).setVisible(!albumsMode);
	menu.findItem(org.horaapps.leafpic.R.id.delete_action).setVisible(!albumsMode || editMode);
	menu.findItem(org.horaapps.leafpic.R.id.setAsAlbumPreview).setVisible(!albumsMode && getAlbum().getSelectedCount() == 1);
	menu.findItem(org.horaapps.leafpic.R.id.clear_album_preview).setVisible(!albumsMode && getAlbum().hasCustomCover());
	menu.findItem(org.horaapps.leafpic.R.id.renameAlbum).setVisible((albumsMode && getAlbums().getSelectedCount() == 1) || (!albumsMode && !editMode));
	menu.findItem(org.horaapps.leafpic.R.id.affixPhoto).setVisible(!albumsMode && getAlbum().getSelectedCount() > 1);
	return super.onPrepareOptionsMenu(menu);
  }

  private void togglePrimaryToolbarOptions(final Menu menu) {
	menu.setGroupVisible(org.horaapps.leafpic.R.id.general_action, !editMode);

	if (!editMode) {
	  menu.findItem(org.horaapps.leafpic.R.id.filter_menu).setVisible(!albumsMode);
	  menu.findItem(org.horaapps.leafpic.R.id.search_action).setVisible(albumsMode);
	}
  }

  //endregion

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

	switch (item.getItemId()) {

	  case org.horaapps.leafpic.R.id.select_all:
		if (albumsMode) {
		  if (getAlbums().getSelectedCount() == albumsAdapter.getItemCount()) {
			editMode = false;
			getAlbums().clearSelectedAlbums();
		  } else getAlbums().selectAllAlbums();
		  albumsAdapter.notifyDataSetChanged();
		} else {
		  if (getAlbum().getSelectedCount() == mediaAdapter.getItemCount()) {
			editMode = false;
			getAlbum().clearSelectedPhotos();
		  } else getAlbum().selectAllPhotos();
		  mediaAdapter.notifyDataSetChanged();
		}
		invalidateOptionsMenu();
		return true;

	  case org.horaapps.leafpic.R.id.settings:
		startActivity(new Intent(MainActivity.this, SettingsActivity.class));
		return true;

	  case org.horaapps.leafpic.R.id.installShortcut:
		getAlbums().installShortcutForSelectedAlbums(this.getApplicationContext());
		finishEditMode();
		return true;

	  case org.horaapps.leafpic.R.id.hideAlbumButton:
		final AlertDialog.Builder hideDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

		AlertDialogsHelper.getTextDialog(MainActivity.this,hideDialogBuilder,
				hidden ? org.horaapps.leafpic.R.string.unhide : org.horaapps.leafpic.R.string.hide,
				hidden ? org.horaapps.leafpic.R.string.unhide_album_message : org.horaapps.leafpic.R.string.hide_album_message);

		hideDialogBuilder.setPositiveButton(getString(hidden ? org.horaapps.leafpic.R.string.unhide : org.horaapps.leafpic.R.string.hide), new DialogInterface.OnClickListener() {
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
		  hideDialogBuilder.setNeutralButton(this.getString(org.horaapps.leafpic.R.string.exclude), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			  if (albumsMode) {
				getAlbums().excludeSelectedAlbums(getApplicationContext());
				albumsAdapter.notifyDataSetChanged();
				invalidateOptionsMenu();
			  } else {
				customAlbumsHandler.excludeAlbum(getAlbum().getPath(), getAlbum().getId());
				displayAlbums(true);
			  }
			}
		  });
		}
		hideDialogBuilder.setNegativeButton(this.getString(org.horaapps.leafpic.R.string.cancel), null);
		hideDialogBuilder.show();
		return true;

	  case org.horaapps.leafpic.R.id.delete_action:
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

			//Toast.makeText(MainActivity.this, ""+result, Toast.LENGTH_SHORT).show();
			invalidateOptionsMenu();
			checkNothing();
			swipeRefreshLayout.setRefreshing(false);
		  }
		}

		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
		AlertDialogsHelper.getTextDialog(this, deleteDialog, org.horaapps.leafpic.R.string.delete, albumsMode || (!albumsMode && !editMode) ? org.horaapps.leafpic.R.string.delete_album_message : org.horaapps.leafpic.R.string.delete_photos_message);

		deleteDialog.setNegativeButton(this.getString(org.horaapps.leafpic.R.string.cancel), null);
		deleteDialog.setPositiveButton(this.getString(org.horaapps.leafpic.R.string.delete), new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int id) {
			if (securityObj.isActiveSecurity() && securityObj.isPasswordOnDelete()) {
			  AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
			  final EditText editTextPassword  = securityObj.getInsertPasswordDialog(MainActivity.this,passwordDialogBuilder);
			  passwordDialogBuilder.setNegativeButton(getString(org.horaapps.leafpic.R.string.cancel), null);

			  passwordDialogBuilder.setPositiveButton(getString(org.horaapps.leafpic.R.string.ok_action), new DialogInterface.OnClickListener() {
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
					Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.wrong_password, Toast.LENGTH_SHORT).show();
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
	  case org.horaapps.leafpic.R.id.excludeAlbumButton:

		final AlertDialog.Builder excludeDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

		final View excludeDialogLayout = getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_exclude, null);
		TextView textViewExcludeTitle = (TextView) excludeDialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_title);
		TextView textViewExcludeMessage = (TextView) excludeDialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_message);
		final Spinner spinnerParents = (Spinner) excludeDialogLayout.findViewById(org.horaapps.leafpic.R.id.parents_folder);

		spinnerParents.getBackground().setColorFilter(getIconColor(), PorterDuff.Mode.SRC_ATOP);

		((CardView) excludeDialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(getCardBackgroundColor());
		textViewExcludeTitle.setBackgroundColor(getPrimaryColor());
		textViewExcludeTitle.setText(getString(org.horaapps.leafpic.R.string.exclude));

		if((albumsMode && getAlbums().getSelectedCount() > 1) || getAlbums().isContentFromMediaStore()) {
		  textViewExcludeMessage.setText(org.horaapps.leafpic.R.string.exclude_albums_message);
		  spinnerParents.setVisibility(View.GONE);
		} else {
		  textViewExcludeMessage.setText(org.horaapps.leafpic.R.string.exclude_album_message);
		  spinnerParents.setAdapter(getSpinnerAdapter(albumsMode ? getAlbums().getSelectedAlbum(0).getParentsFolders() : getAlbum().getParentsFolders()));
		}

		textViewExcludeMessage.setTextColor(getTextColor());
		excludeDialogBuilder.setView(excludeDialogLayout);

		excludeDialogBuilder.setPositiveButton(this.getString(org.horaapps.leafpic.R.string.exclude), new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int id) {

			if ((albumsMode && getAlbums().getSelectedCount() > 1) || getAlbums().isContentFromMediaStore()) {
			  getAlbums().excludeSelectedAlbums(getApplicationContext());
			  albumsAdapter.notifyDataSetChanged();
			  invalidateOptionsMenu();
			} else {
			  StringUtils.showToast(getApplicationContext(), spinnerParents.getSelectedItem().toString());
			  // TODO: 24/07/16 fix for media store
			  customAlbumsHandler.excludeAlbum(spinnerParents.getSelectedItem().toString(),getAlbum().getId());
			  displayAlbums(true);
			}
		  }
		});
		excludeDialogBuilder.setNegativeButton(this.getString(org.horaapps.leafpic.R.string.cancel), null);
		excludeDialogBuilder.show();
		return true;

	  case org.horaapps.leafpic.R.id.sharePhotos:
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND_MULTIPLE);
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(org.horaapps.leafpic.R.string.sent_to_action));

		ArrayList<Uri> files = new ArrayList<Uri>();
		for (Media f : getAlbum().selectedMedias)
		  files.add(f.getUri());

		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
		intent.setType(StringUtils.getGenericMIME(getAlbum().selectedMedias.get(0).getMIME()));
		finishEditMode();
		startActivity(intent);
		return true;

	  case org.horaapps.leafpic.R.id.all_media_filter:
		if (!albumsMode) {
		  getAlbum().filterMedias(ImageFileFilter.FILTER_ALL);
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		  item.setChecked(true);
		  checkNothing();
		  //TODO improve
		  fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
		}
		return true;

	  case org.horaapps.leafpic.R.id.video_media_filter:
		if (!albumsMode) {
		  getAlbum().filterMedias(ImageFileFilter.FILTER_VIDEO);
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		  item.setChecked(true);
		  checkNothing();
		  fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
		}
		return true;

	  case org.horaapps.leafpic.R.id.image_media_filter:
		if (!albumsMode) {
		  getAlbum().filterMedias(ImageFileFilter.FILTER_IMAGES);
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		  item.setChecked(true);
		  checkNothing();
		  fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
		}
		return true;

	  case org.horaapps.leafpic.R.id.gifs_media_filter:
		if (!albumsMode) {
		  getAlbum().filterMedias(ImageFileFilter.FILTER_GIFS);
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		  item.setChecked(true);
		  checkNothing();
		  fabCamera.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE));
		}
		return true;

	  case org.horaapps.leafpic.R.id.name_sort_action:
		if (albumsMode) {
		  getAlbums().setDefaultSortingMode(AlbumSettings.SORT_BY_NAME);
		  getAlbums().sortAlbums(getApplicationContext());
		  albumsAdapter.swapDataSet(getAlbums().dispAlbums);
		} else {
		  getAlbum().setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_NAME);
		  getAlbum().sortPhotos();
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		}
		item.setChecked(true);
		return true;

	  case org.horaapps.leafpic.R.id.date_taken_sort_action:
		if (albumsMode) {
		  getAlbums().setDefaultSortingMode(AlbumSettings.SORT_BY_DATE);
		  getAlbums().sortAlbums(getApplicationContext());
		  albumsAdapter.swapDataSet(getAlbums().dispAlbums);
		} else {
		  getAlbum().setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_DATE);
		  getAlbum().sortPhotos();
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		}
		item.setChecked(true);
		return true;

	  case org.horaapps.leafpic.R.id.size_sort_action:
		if (albumsMode) {
		  getAlbums().setDefaultSortingMode(AlbumSettings.SORT_BY_SIZE);
		  getAlbums().sortAlbums(getApplicationContext());
		  albumsAdapter.swapDataSet(getAlbums().dispAlbums);
		} else {
		  getAlbum().setDefaultSortingMode(getApplicationContext(),AlbumSettings.SORT_BY_SIZE);
		  getAlbum().sortPhotos();
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		}
		item.setChecked(true);
		return true;

	  case org.horaapps.leafpic.R.id.type_sort_action:
		if (!albumsMode) {
		  getAlbum().setDefaultSortingMode(getApplicationContext(), AlbumSettings.SORT_BY_TYPE);
		  getAlbum().sortPhotos();
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		  item.setChecked(true);
		}

		return true;

	  case org.horaapps.leafpic.R.id.ascending_sort_action:
		if (albumsMode) {
		  getAlbums().setDefaultSortingAscending(!item.isChecked());
		  getAlbums().sortAlbums(getApplicationContext());
		  albumsAdapter.swapDataSet(getAlbums().dispAlbums);
		} else {
		  getAlbum().setDefaultSortingAscending(getApplicationContext(), !item.isChecked());
		  getAlbum().sortPhotos();
		  mediaAdapter.swapDataSet(getAlbum().getMedia());
		}
		item.setChecked(!item.isChecked());
		return true;

	  //region Affix
	  case  org.horaapps.leafpic.R.id.affixPhoto:
		// TODO: 03/08/16 move this away from this activity

		final AlertDialog.Builder AffixDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
		final View dialogLayout = getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_affix, null);

		dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_title).setBackgroundColor(getPrimaryColor());
		((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_card)).setCardBackgroundColor(getCardBackgroundColor());

		//ITEMS
		final SwitchCompat swVertical = (SwitchCompat) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_vertical_switch);
		final SwitchCompat swSaveHere = (SwitchCompat) dialogLayout.findViewById(org.horaapps.leafpic.R.id.save_here_switch);

		final RadioGroup radioFormatGroup = (RadioGroup) dialogLayout.findViewById(org.horaapps.leafpic.R.id.radio_format);
		final RadioButton radio_jpg = (RadioButton) dialogLayout.findViewById(org.horaapps.leafpic.R.id.radio_jpeg);
		final RadioButton radio_png = (RadioButton) dialogLayout.findViewById(org.horaapps.leafpic.R.id.radio_png);
		final RadioButton radio_webp = (RadioButton) dialogLayout.findViewById(org.horaapps.leafpic.R.id.radio_webp);

		final TextView txtQuality = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_quality_title);
		final SeekBar seekQuality = (SeekBar) dialogLayout.findViewById(org.horaapps.leafpic.R.id.seek_bar_quality);

		final ScrollView scrollView = (ScrollView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_scrollView);
		setScrollViewColor(scrollView);

		/** TextViews **/
		int color = getTextColor();
		((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_vertical_title)).setTextColor(color);
		((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.compression_settings_title)).setTextColor(color);
		((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.save_here_title)).setTextColor(color);

		/** Sub TextViews **/
		color = getTextColor();
		((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.save_here_sub)).setTextColor(color);
		((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_vertical_sub)).setTextColor(color);
		((TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_format_sub)).setTextColor(color);
		txtQuality.setTextColor(color);

		/** Icons **/
		color = getIconColor();
		((IconicsImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_quality_icon)).setColor(color);
		((IconicsImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_format_icon)).setColor(color);
		((IconicsImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.affix_vertical_icon)).setColor(color);
		((IconicsImageView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.save_here_icon)).setColor(color);

		seekQuality.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
		seekQuality.getThumb().setColorFilter(new PorterDuffColorFilter(getAccentColor(),PorterDuff.Mode.SRC_IN));

		seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		  @Override
		  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			txtQuality.setText(Html.fromHtml(
					String.format(Locale.getDefault(), "%s <b>%d</b>", getString(org.horaapps.leafpic.R.string.quality), progress)));
		  }
		  @Override
		  public void onStartTrackingTouch(SeekBar seekBar) {

		  }
		  @Override
		  public void onStopTrackingTouch(SeekBar seekBar) {

		  }
		});
		seekQuality.setProgress(90); //DEFAULT

		updateRadioButtonColor(radio_jpg);
		updateRadioButtonColor(radio_png);
		updateRadioButtonColor(radio_webp);

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
		  private AlertDialog dialog;
		  @Override
		  protected void onPreExecute() {
			AlertDialog.Builder progressDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

			dialog = AlertDialogsHelper.getProgressDialog(MainActivity.this, progressDialog,
					getString(org.horaapps.leafpic.R.string.affix), getString(org.horaapps.leafpic.R.string.affix_text));
			dialog.show();
			super.onPreExecute();
		  }

		  @Override
		  protected Void doInBackground(String... arg0) {
			ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
			for (int i=0;i<getAlbum().getSelectedCount();i++){
			  if(!getAlbum().selectedMedias.get(i).isVideo())
				bitmapArray.add(getAlbum().selectedMedias.get(i).getBitmap());
			}

			if (bitmapArray.size() > 1) {
			  //TODO: MUST FIX
			  Bitmap.CompressFormat compressFormat;
			  switch (radioFormatGroup.getCheckedRadioButtonId()) {
				case org.horaapps.leafpic.R.id.radio_jpeg: default:
				  compressFormat = Bitmap.CompressFormat.JPEG; break;
				case org.horaapps.leafpic.R.id.radio_png:
				  compressFormat = Bitmap.CompressFormat.PNG; break;
				case org.horaapps.leafpic.R.id.radio_webp:
				  compressFormat = Bitmap.CompressFormat.WEBP; break;
			  }

			  AffixOptions options = new AffixOptions(
															 swSaveHere.isChecked() ? getAlbum().getPath() : AffixMedia.getDefaultDirectoryPath(),
															 compressFormat,
															 seekQuality.getProgress(),
															 swVertical.isChecked());

			  AffixMedia.AffixBitmapList(getApplicationContext(), bitmapArray, options);

			} else {
			  runOnUiThread(new Runnable(){
				@Override
				public void run(){ Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.affix_error,Toast.LENGTH_SHORT).show(); }
			  });
			}
			return null;
		  }
		  @Override
		  protected void onPostExecute(Void result) {
			editMode = false;
			getAlbum().clearSelectedPhotos();
			dialog.dismiss();
			invalidateOptionsMenu();
			mediaAdapter.notifyDataSetChanged();
			//new PreparePhotosTask().execute();
		  }
		}
		//Dialog Buttons
		AffixDialog.setView(dialogLayout);
		AffixDialog.setPositiveButton(this.getString(org.horaapps.leafpic.R.string.ok_action), new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int id) {new affixMedia().execute();}});
		AffixDialog.setNegativeButton(this.getString(org.horaapps.leafpic.R.string.cancel), new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int id) {}});
		AffixDialog.show();
		return true;
	  //endregion

	  case org.horaapps.leafpic.R.id.action_move:

		bottomSheetDialogFragment = new SelectAlbumBottomSheet();
		bottomSheetDialogFragment.setTitle(getString(org.horaapps.leafpic.R.string.move_to));
		bottomSheetDialogFragment.setSelectAlbumInterface(new SelectAlbumBottomSheet.SelectAlbumInterface() {
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
			bottomSheetDialogFragment.dismiss();
		  }
		});
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		return true;

	  case org.horaapps.leafpic.R.id.action_copy:
		bottomSheetDialogFragment = new SelectAlbumBottomSheet();
		bottomSheetDialogFragment.setTitle(getString(org.horaapps.leafpic.R.string.copy_to));
		bottomSheetDialogFragment.setSelectAlbumInterface(new SelectAlbumBottomSheet.SelectAlbumInterface() {
		  @Override
		  public void folderSelected(String path) {
			boolean success = getAlbum().copySelectedPhotos(getApplicationContext(), path);
			finishEditMode();
			bottomSheetDialogFragment.dismiss();
			if (!success)
			  requestSdCardPermissions();
		  }
		});
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		return true;

	  case org.horaapps.leafpic.R.id.renameAlbum:
		AlertDialog.Builder renameDialogBuilder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
		final EditText editTextNewName = new EditText(getApplicationContext());
		editTextNewName.setText(albumsMode ? getAlbums().getSelectedAlbum(0).getName() : getAlbum().getName());

		AlertDialogsHelper.getInsertTextDialog(MainActivity.this, renameDialogBuilder,
				editTextNewName, org.horaapps.leafpic.R.string.rename_album);

		renameDialogBuilder.setNegativeButton(getString(org.horaapps.leafpic.R.string.cancel), null);

		renameDialogBuilder.setPositiveButton(getString(org.horaapps.leafpic.R.string.ok_action), new DialogInterface.OnClickListener() {
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
			  StringUtils.showToast(getApplicationContext(), getString(org.horaapps.leafpic.R.string.insert_something));
			  editTextNewName.requestFocus();
			}
		  }});
		return true;

	  case org.horaapps.leafpic.R.id.clear_album_preview:
		if (!albumsMode) {
		  CustomAlbumsHandler as = new CustomAlbumsHandler(getApplicationContext());
		  as.clearAlbumPreview(getAlbum().getPath(), getAlbum().getId());
		  getAlbum().setSettings(getApplicationContext());
		}
		return true;

	  case org.horaapps.leafpic.R.id.setAsAlbumPreview:
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

  private void toggleRecyclersVisibilty(boolean albumsMode){
	rvAlbums.setVisibility(albumsMode ? View.VISIBLE : View.GONE);
	rvMedia.setVisibility(albumsMode ? View.GONE : View.VISIBLE);
	//touchScrollBar.setScrollBarHidden(albumsMode);

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
		setRecentApp(getString(org.horaapps.leafpic.R.string.app_name));
	  }
	}
  }

  private class PrepareAlbumTask extends AsyncTask<Void, Integer, Void> {

	@Override
	protected void onPreExecute() {
	  swipeRefreshLayout.setRefreshing(true);
	  toggleRecyclersVisibilty(true);
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
	  toggleRecyclersVisibilty(false);
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
	  checkNothing();
	  swipeRefreshLayout.setRefreshing(false);
	}
  }

  private class MovePhotos extends AsyncTask<String, Void, Boolean> {

	@Override
	protected void onPreExecute() {
	  swipeRefreshLayout.setRefreshing(true);
	  super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
	  boolean success = true;
	  try
	  {
		for (int i = 0; i < getAlbum().selectedMedias.size(); i++) {
		  File from = new File(getAlbum().selectedMedias.get(i).getPath());
		  File to = new File(StringUtils.getPhotoPathMoved(getAlbum().selectedMedias.get(i).getPath(), arg0[0]));

		  if (ContentHelper.moveFile(getApplicationContext(), from, to)) {
			MediaScannerConnection.scanFile(getApplicationContext(),
					new String[]{ to.getAbsolutePath(), from.getAbsolutePath() }, null, null);
			getAlbum().getMedia().remove(getAlbum().selectedMedias.get(i));
		  } else success = false;
		}
	  } catch (Exception e) { e.printStackTrace(); }
	  return success;
	}

	@Override
	protected void onPostExecute(Boolean result) {
	  if (result) {
		if (getAlbum().getMedia().size() == 0) {
		  getAlbums().removeCurrentAlbum();
		  albumsAdapter.notifyDataSetChanged();
		  displayAlbums();
		}
	  } else requestSdCardPermissions();

	  mediaAdapter.swapDataSet(getAlbum().getMedia());
	  finishEditMode();
	  invalidateOptionsMenu();
	  swipeRefreshLayout.setRefreshing(false);
	}
  }
}
