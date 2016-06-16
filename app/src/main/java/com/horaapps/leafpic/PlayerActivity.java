/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.horaapps.leafpic;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;

import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.Util;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.player.DemoPlayer;
import com.horaapps.leafpic.player.ExtractorRendererBuilder;
import com.horaapps.leafpic.player.HlsRendererBuilder;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.Measure;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;


public class PlayerActivity extends ThemedActivity implements SurfaceHolder.Callback,
    DemoPlayer.Listener, DemoPlayer.Id3MetadataListener, AudioCapabilitiesReceiver.Listener {

  // For use within demo app code.
  private static final String CONTENT_TYPE_EXTRA = "content_type";

  // For use when launching the demo app using adb.
  private static final String CONTENT_EXT_EXTRA = "type";

  private static final String TAG = "PlayerActivity";

  private static final CookieManager defaultCookieManager;
  static {
    defaultCookieManager = new CookieManager();
    defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  private MediaController mediaController;
  private View mediController_anchor;
  private View shutterView;
  private AspectRatioFrameLayout videoFrame;
  private SurfaceView surfaceView;

  private DemoPlayer player;
  private boolean playerNeedsPrepare;
  private long playerPosition;

  private Uri contentUri;
  private int contentType;

  private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
  private Toolbar toolbar;

  private boolean fullscreen = false;

  // Activity lifecycle

  private void initUI(){

    toolbar.setBackgroundColor(
            isApplyThemeOnImgAct()
            ? ColorPalette.getTransparentColor (getPrimaryColor(), getTransparency())
            : ColorPalette.getTransparentColor(getDefaultThemeToolbarColor3th(), 175));

    setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
    getSupportActionBar().setDisplayShowTitleEnabled(false);


    getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);

    getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
            (new View.OnSystemUiVisibilityChangeListener() {
              @Override
              public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) showControls();
                else hideControls();
              }
            });
    toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator())
            .setDuration(0).start();
    mediController_anchor.setPadding(0,0,0,Measure.getNavBarHeight(PlayerActivity.this));

    setStatusBarColor();
    setNavBarColor();
    setRecentApp(getString(R.string.app_name));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_video, menu);

    menu.findItem(R.id.action_share).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
    menu.findItem(R.id.rotate_layout).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_screen_rotation));
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.action_share:
        Intent share = new Intent(Intent.ACTION_SEND);
        Media m = new Media(ContentHelper.getPath(getApplicationContext() ,getIntent().getData()));
        share.setType(m.getMIME());
        share.putExtra(Intent.EXTRA_STREAM, getIntent().getData());
        startActivity(Intent.createChooser(share, getString(R.string.send_to)));
        return true;

      case R.id.action_settings:
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        return true;

      case R.id.rotate_layout:
        int rotation = (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        showControls();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void setNavBarColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (isNavigationBarColored())
        getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
      else
        getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), getTransparency()));
    }
  }


  @Override
  protected void setStatusBarColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (isTranslucentStatusBar() && isTransparencyZero())
        getWindow().setStatusBarColor(ColorPalette.getOscuredColor(getPrimaryColor()));
      else
        getWindow().setStatusBarColor(ColorPalette.getTransparentColor(getPrimaryColor(), getTransparency()));
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    FrameLayout root = (FrameLayout) findViewById(R.id.root);
    root.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          toggleControlsVisibility();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          //view.performClick();
        }
        return true;
      }
    });

    root.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        return !(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_MENU) && mediaController.dispatchKeyEvent(event);
      }
    });

    root.setBackgroundColor(R.color.md_black_1000);

    shutterView = findViewById(R.id.shutter);

    videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
    surfaceView = (SurfaceView) findViewById(R.id.surface_view);
    surfaceView.getHolder().addCallback(this);

    mediaController = new KeyCompatibleMediaController(this);

    mediController_anchor = findViewById(R.id.media_player_anchor);
    mediaController.setAnchorView(mediController_anchor);
    mediaController.setPaddingRelative(0,0,0,Measure.getNavBarHeight(PlayerActivity.this));
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    initUI();

    CookieHandler currentHandler = CookieHandler.getDefault();
    if (currentHandler != defaultCookieManager)
      CookieHandler.setDefault(defaultCookieManager);


    audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
    audioCapabilitiesReceiver.register();
  }

  @Override
  public void onNewIntent(Intent intent) {
    releasePlayer();
    playerPosition = 0;
    setIntent(intent);
  }

  @Override
  public void onResume() {
    super.onResume();
    Intent intent = getIntent();
    contentUri = intent.getData();
    contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA,
            inferContentType(contentUri, intent.getStringExtra(CONTENT_EXT_EXTRA)));

    if (player == null) {
      if (!maybeRequestPermission()) {
        preparePlayer(true);
      }
    } else
      player.setBackgrounded(false);

  }

  @Override
  public void onPause() {
    super.onPause();
    releasePlayer();
    shutterView.setVisibility(View.VISIBLE);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    audioCapabilitiesReceiver.unregister();
    releasePlayer();
  }

  @Override
  public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
    if (player == null) {
      return;
    }
    boolean backgrounded = player.getBackgrounded();
    boolean playWhenReady = player.getPlayWhenReady();
    releasePlayer();
    preparePlayer(playWhenReady);
    player.setBackgrounded(backgrounded);
  }

  // Permission request listener method

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      preparePlayer(true);
    } else {
      Toast.makeText(getApplicationContext(), R.string.storage_permission_denied,
          Toast.LENGTH_LONG).show();
      finish();
    }
  }

  // Permission management methods

  /**
   * Checks whether it is necessary to ask for permission to read storage. If necessary, it also
   * requests permission.
   *
   * @return true if a permission request is made. False if it is not necessary.
   */
  @TargetApi(23)
  private boolean maybeRequestPermission() {
    if (requiresPermission(contentUri)) {
      requestPermissions(new String[] {permission.READ_EXTERNAL_STORAGE}, 0);
      return true;
    } else {
      return false;
    }
  }

  @TargetApi(23)
  private boolean requiresPermission(Uri uri) {
    return Util.SDK_INT >= 23
        && Util.isLocalFileUri(uri)
        && checkSelfPermission(permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED;
  }

  // Internal methods

  private DemoPlayer.RendererBuilder getRendererBuilder() {
    String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
    switch (contentType) {
      /*case Util.TYPE_SS:
        return new SmoothStreamingRendererBuilder(this, userAgent, contentUri.toString(),
            new SmoothStreamingTestMediaDrmCallback());
      case Util.TYPE_DASH:
        return new DashRendererBuilder(this, userAgent, contentUri.toString(),
            new WidevineTestMediaDrmCallback(contentId, provider));*/
      case Util.TYPE_HLS:
        return new HlsRendererBuilder(this, userAgent, contentUri.toString());
      case Util.TYPE_OTHER:
        return new ExtractorRendererBuilder(this, userAgent, contentUri);
      default:
        throw new IllegalStateException("Unsupported type: " + contentType);
    }
  }

  private void preparePlayer(boolean playWhenReady) {
    if (player == null) {
      player = new DemoPlayer(getRendererBuilder());
      player.addListener(this);
      //player.setCaptionListener(this);
      player.setMetadataListener(this);
      player.seekTo(playerPosition);
      playerNeedsPrepare = true;
      mediaController.setMediaPlayer(player.getPlayerControl());
      mediaController.setEnabled(true);
    }
    if (playerNeedsPrepare) {
      player.prepare();
      playerNeedsPrepare = false;
    }
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(playWhenReady);
  }

  private void releasePlayer() {
    if (player != null) {
      playerPosition = player.getCurrentPosition();
      player.release();
      player = null;
    }
  }

  // DemoPlayer.Listener implementation

  @Override
  public void onStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      showControls();
    }

    String text = "playWhenReady=" + playWhenReady + ", playbackState=";
    switch(playbackState) {
      case ExoPlayer.STATE_BUFFERING:
        text += "buffering";
        break;
      case ExoPlayer.STATE_ENDED:
        text += "ended";
        break;
      case ExoPlayer.STATE_IDLE:
        text += "idle";
        break;
      case ExoPlayer.STATE_PREPARING:
        text += "preparing";
        break;
      case ExoPlayer.STATE_READY:
        text += "ready";
        break;
      default:
        text += "unknown";
        break;
    }
    Log.d(TAG, "onStateChanged: "+text);
    //updateButtonVisibilities();
  }

  @Override
  public void onError(Exception e) {
    String errorString = null;
    if (e instanceof UnsupportedDrmException) {
      // Special case DRM failures.
      UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
      errorString = getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
          : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
          ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
    } else if (e instanceof ExoPlaybackException
        && e.getCause() instanceof DecoderInitializationException) {
      // Special case for decoder initialization failures.
      DecoderInitializationException decoderInitializationException =
          (DecoderInitializationException) e.getCause();
      if (decoderInitializationException.decoderName == null) {
        if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
          errorString = getString(R.string.error_querying_decoders);
        } else if (decoderInitializationException.secureDecoderRequired) {
          errorString = getString(R.string.error_no_secure_decoder,
              decoderInitializationException.mimeType);
        } else {
          errorString = getString(R.string.error_no_decoder,
              decoderInitializationException.mimeType);
        }
      } else {
        errorString = getString(R.string.error_instantiating_decoder,
            decoderInitializationException.decoderName);
      }
    }
    if (errorString != null) {
      Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
    }
    playerNeedsPrepare = true;
    showControls();
  }

  @Override
  public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
      float pixelWidthAspectRatio) {
    shutterView.setVisibility(View.GONE);
    videoFrame.setAspectRatio(
        height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
  }

  private void toggleControlsVisibility()  {
      if (fullscreen) showControls();
      else hideControls();
  }

  private void hideControls() {
    mediaController.hide();
    toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator())
            .setDuration(200).start();

    getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
    fullscreen=true;
  }

  private void showControls() {
    int rotation = (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
    if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { //Landscape
      getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                      | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      mediaController.setPaddingRelative(0,0,0,0);
    } else {
      getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      mediaController.setPaddingRelative(0,0,0, Measure.getNavBarHeight(getApplicationContext()));
    }

    fullscreen = false;
    mediaController.show();
    toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
            .setDuration(240).start();

  }

  // DemoPlayer.MetadataListener implementation

  @Override
  public void onId3Metadata(List<Id3Frame> id3Frames) {
    for (Id3Frame id3Frame : id3Frames) {
      if (id3Frame instanceof TxxxFrame) {
        TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
        Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
            txxxFrame.description, txxxFrame.value));
      } else if (id3Frame instanceof PrivFrame) {
        PrivFrame privFrame = (PrivFrame) id3Frame;
        Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
      } else if (id3Frame instanceof GeobFrame) {
        GeobFrame geobFrame = (GeobFrame) id3Frame;
        Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
            geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
      } else {
        Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
      }
    }
  }

  // SurfaceHolder.Callback implementation

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (player != null) {
      player.setSurface(holder.getSurface());
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    // Do nothing.
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    if (player != null) {
      player.blockingClearSurface();
    }
  }

  /**
   * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
   * extension.
   *
   * @param uri The {@link Uri} of the media.
   * @param fileExtension An overriding file extension.
   * @return The inferred type.
   */
  private static int inferContentType(Uri uri, String fileExtension) {
    String lastPathSegment = !TextUtils.isEmpty(fileExtension) ? "." + fileExtension
        : uri.getLastPathSegment();
    return Util.inferContentType(lastPathSegment);
  }

  private static final class KeyCompatibleMediaController extends MediaController {

    private MediaPlayerControl playerControl;

    public KeyCompatibleMediaController(Context context) {
      super(context);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl playerControl) {
      super.setMediaPlayer(playerControl);
      this.playerControl = playerControl;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
      int keyCode = event.getKeyCode();
      if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
          show();
        }
        return true;
      } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
          show();
        }
        return true;
      }
      return super.dispatchKeyEvent(event);
    }
  }

}
