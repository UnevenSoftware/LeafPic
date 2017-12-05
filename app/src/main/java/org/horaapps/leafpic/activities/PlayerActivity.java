/*
 * Copyright (C) 2016 The Android Open Source Project
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
package org.horaapps.leafpic.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelections;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.views.videoplayer.CustomExoPlayerView;
import org.horaapps.leafpic.views.videoplayer.CustomPlayBackController;
import org.horaapps.leafpic.views.videoplayer.TrackSelectionHelper;
import org.horaapps.liz.ThemedActivity;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerActivity extends ThemedActivity implements  ExoPlayer.EventListener,
        TrackSelector.EventListener<MappedTrackInfo>, CustomPlayBackController.VisibilityListener {

    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private Timeline.Window window;
    private CustomExoPlayerView simpleExoPlayerView;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;
    private TrackSelectionHelper trackSelectionHelper;
    private boolean playerNeedsSource;

    private boolean shouldAutoPlay;
    private boolean isTimelineStatic;
    private int playerWindow,video, audio, text;
    private long playerPosition;
    Toolbar toolbar;

    View rootView;
    private boolean fullScreenMode;
    // Activity lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTheme();
        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        window = new Timeline.Window();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        setContentView(R.layout.activity_player);
        initUi();
        rootView = findViewById(R.id.root);

        simpleExoPlayerView = (CustomExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        isTimelineStatic = false;
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) initializePlayer();
    }

    private void initUi() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle(StringUtils.getName(getIntent().getData().getPath()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(org.horaapps.leafpic.R.menu.menu_video_player, menu);

        TrackSelections<MappedTrackInfo> trackSelections = trackSelector.getCurrentSelections();

        if (trackSelections != null) {
            int rendererCount = trackSelections.length;
            for (int i = 0; i < rendererCount; i++) {
                TrackGroupArray trackGroups = trackSelections.info.getTrackGroups(i);
                if (trackGroups.length != 0) {
                    switch (player.getRendererType(i)) {
                        case C.TRACK_TYPE_AUDIO:
                            menu.findItem(R.id.audio_stuff).setVisible(true);
                            audio = i;
                            break;
                        case C.TRACK_TYPE_VIDEO:
                            menu.findItem(R.id.video_stuff).setVisible(true);
                            video = i;              break;
                        case C.TRACK_TYPE_TEXT:
                            menu.findItem(R.id.text_stuff).setVisible(true);
                            text = i;
                            break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // TODO: 12/7/16 check them out
        menu.findItem(R.id.audio_stuff).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_audiotrack));
        menu.findItem(R.id.video_stuff).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_switch_video));
        menu.findItem(R.id.text_stuff).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_subtitles));
        menu.findItem(R.id.action_share).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_share));
        menu.findItem(R.id.rotate_layout).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_screen_lock_rotation));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    /*if (view == retryButton) {
      initializePlayer();
    } */

        switch (item.getItemId()) {
            case R.id.video_stuff:
                trackSelectionHelper.showSelectionDialog(this, getString(R.string.video),
                        trackSelector.getCurrentSelections().info, video);
                return true;
            case R.id.audio_stuff:
                trackSelectionHelper.showSelectionDialog(this, getString(R.string.audio),
                        trackSelector.getCurrentSelections().info, audio);
                return true;
            case R.id.text_stuff:
                trackSelectionHelper.showSelectionDialog(this, getString(R.string.subtitles),
                        trackSelector.getCurrentSelections().info, text);
                return true;

            case org.horaapps.leafpic.R.id.action_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(getIntent().getType());
                share.putExtra(Intent.EXTRA_STREAM, getIntent().getData());
                startActivity(Intent.createChooser(share, getString(org.horaapps.leafpic.R.string.send_to)));
                return true;

            case org.horaapps.leafpic.R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;

            case org.horaapps.leafpic.R.id.rotate_layout:
                int rotation = (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        /*showControls();*/
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }

    // Internal methods
    private void initializePlayer() {
        Intent intent = getIntent();
        if (player == null) {
            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
            UUID drmSchemeUuid = intent.hasExtra(DRM_SCHEME_UUID_EXTRA) ? UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA)) : null;
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
                String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES);
                Map<String, String> keyRequestProperties;
                if (keyRequestPropertiesArray == null || keyRequestPropertiesArray.length < 2) {
                    keyRequestProperties = null;
                } else {
                    keyRequestProperties = new HashMap<>();
                    for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                        keyRequestProperties.put(keyRequestPropertiesArray[i],
                                keyRequestPropertiesArray[i + 1]);
                    }
                }
                try { drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl, keyRequestProperties);
                } catch (UnsupportedDrmException e) {
                    int errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                    showToast(errorStringId);
                    return;
                }
            }

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            trackSelector.addListener(this);
            trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory, getThemeHelper());
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    drmSessionManager, preferExtensionDecoders);
            player.addListener(this);
            simpleExoPlayerView.setPlayer(player);
            if (isTimelineStatic) {
                if (playerPosition == C.TIME_UNSET) {
                    player.seekToDefaultPosition(playerWindow);
                } else {
                    player.seekTo(playerWindow, playerPosition);
                }
            }
            player.setPlayWhenReady(shouldAutoPlay);
            playerNeedsSource = true;
        }
        if (playerNeedsSource) {
            String action = intent.getAction();
            Uri uris[];
            String extensions[];
            if (intent.getData() != null && intent.getType() != null) {
                uris = new Uri[] { intent.getData() };
                extensions = new String[] { intent.getType()};
            } else {
                // TODO: 12/7/16 asdasd
                showToast(getString(R.string.unexpected_intent_action, action));
                return;
            }

            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            player.prepare(mediaSource, !isTimelineStatic, !isTimelineStatic);
            playerNeedsSource = false;
            supportInvalidateOptionsMenu();
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:return new SsMediaSource(uri, buildDataSourceFactory(false), new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_DASH:return new DashMediaSource(uri, buildDataSourceFactory(false), new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_HLS:return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
            case C.TYPE_OTHER:return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(), mainHandler, null);
            default: throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid, String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new StreamingDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, null);
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            Timeline timeline = player.getCurrentTimeline();
            if (timeline != null && timeline.getWindow(playerWindow, window).isSeekable)
                playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return new DefaultDataSourceFactory(this, useBandwidthMeter ? BANDWIDTH_METER : null,
                buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null));
    }

    HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "LeafPic"), bandwidthMeter);
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "LeafPic"), useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation
    @Override
    public void onLoadingChanged(boolean isLoading) { }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED)
            showControls();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPositionDiscontinuity() {}

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        isTimelineStatic = timeline != null && timeline.getWindowCount() > 0
                && !timeline.getWindow(timeline.getWindowCount() - 1, window).isDynamic;
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null)
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException)
                        errorString = getString(R.string.error_querying_decoders);
                    else if (decoderInitializationException.secureDecoderRequired)
                        errorString = getString(R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                    else
                        errorString = getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                else
                    errorString = getString(R.string.error_instantiating_decoder, decoderInitializationException.decoderName);
            }
        }
        if (errorString != null)
            showToast(errorString);
        playerNeedsSource = true;
        supportInvalidateOptionsMenu();
        showControls();
    }

    // MappingTrackSelector.EventListener implementation
    @Override
    public void onTrackSelectionsChanged(TrackSelections<? extends MappedTrackInfo> trackSelections) {
        supportInvalidateOptionsMenu();
        MappedTrackInfo trackInfo = trackSelections.info;
        if (trackInfo.hasOnlyUnplayableTracks(C.TRACK_TYPE_VIDEO))
            showToast(R.string.error_unsupported_video);
        if (trackInfo.hasOnlyUnplayableTracks(C.TRACK_TYPE_AUDIO))
            showToast(R.string.error_unsupported_audio);
    }

    //User controls
    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**** THEMING STUFF ****/

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.video_player));
        rootView.setBackgroundColor(getBackgroundColor());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getPrimaryColor());
        }
    }

    private void hideControls() {
        runOnUiThread(new Runnable() {
            public void run() {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator())
                        .setDuration(200).start();
                fullScreenMode = true;
                changeBackGroundColor();
            }
        });
    }

    private void showControls(){
        runOnUiThread(new Runnable() {
            public void run() {
                int rotation = (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { //Landscape
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    simpleExoPlayerView.setPaddingRelative(0, 0, 0, 0);
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    simpleExoPlayerView.setPaddingRelative(0, 0, 0, Measure.getNavBarHeight(getApplicationContext()));
                }
                toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
                        .setDuration(240).start();
                fullScreenMode = false;
                changeBackGroundColor();
            }
        });
    }

    @Override
    public void onVisibilityChange(int visibility) {
        if(visibility == View.GONE)
            hideControls();
        else if(visibility == View.VISIBLE)
            showControls();
    }

    private void changeBackGroundColor() {
        int colorTo;
        int colorFrom;
        if (fullScreenMode) {
            colorFrom = getBackgroundColor();
            colorTo = (ContextCompat.getColor(PlayerActivity.this, R.color.md_black_1000));
        } else {
            colorFrom = (ContextCompat.getColor(PlayerActivity.this, R.color.md_black_1000));
            colorTo = getBackgroundColor();
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(240);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                rootView.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }
}
