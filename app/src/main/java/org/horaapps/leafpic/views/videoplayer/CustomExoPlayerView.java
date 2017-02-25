package org.horaapps.leafpic.views.videoplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SubtitleView;

import org.horaapps.leafpic.R;

import java.util.List;

/**
 * Created by dnld on 12/6/16.
 */

public final class CustomExoPlayerView extends FrameLayout {

    private final View surfaceView;
    private final View shutterView;
    private final SubtitleView subtitleLayout;
    private final AspectRatioFrameLayout layout;
    private final CustomPlayBackController controller;
    private final ComponentListener componentListener;

    private SimpleExoPlayer player;
    private boolean useController = true;
    private int controllerShowTimeoutMs;

    public CustomExoPlayerView(Context context) {
        this(context, null);
    }

    public CustomExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        boolean useTextureView = false;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int rewindMs = CustomPlayBackController.DEFAULT_REWIND_MS;
        int fastForwardMs = CustomPlayBackController.DEFAULT_FAST_FORWARD_MS;
        int controllerShowTimeoutMs = CustomPlayBackController.DEFAULT_SHOW_TIMEOUT_MS;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView, 0, 0);
            try {
                useController = a.getBoolean(com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView_use_controller, useController);
                useTextureView = a.getBoolean(com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView_use_texture_view,
                        useTextureView);
                resizeMode = a.getInt(com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView_resize_mode,
                        AspectRatioFrameLayout.RESIZE_MODE_FIT);
                rewindMs = a.getInt(com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView_rewind_increment, rewindMs);
                fastForwardMs = a.getInt(com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView_fastforward_increment,
                        fastForwardMs);
                controllerShowTimeoutMs = a.getInt(com.google.android.exoplayer2.R.styleable.SimpleExoPlayerView_show_timeout,
                        controllerShowTimeoutMs);
            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(R.layout.exo_player, this);
        componentListener = new ComponentListener();
        layout = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        layout.setResizeMode(resizeMode);
        shutterView = findViewById(R.id.shutter);
        subtitleLayout = (SubtitleView) findViewById(R.id.subtitles);
        subtitleLayout.setUserDefaultStyle();
        subtitleLayout.setUserDefaultTextSize();

        controller = (CustomPlayBackController) findViewById(R.id.control);
        controller.hide();
        controller.setRewindIncrementMs(rewindMs);
        controller.setFastForwardIncrementMs(fastForwardMs);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;

        View view = useTextureView ? new TextureView(context) : new SurfaceView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        surfaceView = view;
        layout.addView(surfaceView, 0);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.setTextOutput(null);
            this.player.setVideoListener(null);
            this.player.removeListener(componentListener);
            this.player.setVideoSurface(null);
        }
        this.player = player;
        if (useController) {
            controller.setPlayer(player);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.setVideoListener(componentListener);
            player.addListener(componentListener);
            player.setTextOutput(componentListener);
            maybeShowController(false);
        } else {
            shutterView.setVisibility(VISIBLE);
            controller.hide();
        }
    }

    public void setResizeMode(int resizeMode) {
        layout.setResizeMode(resizeMode);
    }

    public boolean getUseController() {
        return useController;
    }

    public void setUseController(boolean useController) {
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player);
        } else {
            controller.hide();
            controller.setPlayer(null);
        }
    }

    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {this.controllerShowTimeoutMs = controllerShowTimeoutMs;}

    public void setControllerVisibilityListener(CustomPlayBackController.VisibilityListener listener) {controller.setVisibilityListener(listener);}

    public void setRewindIncrementMs(int rewindMs) {
        controller.setRewindIncrementMs(rewindMs);
    }

    public void setFastForwardIncrementMs(int fastForwardMs) {controller.setFastForwardIncrementMs(fastForwardMs);}

    public View getVideoSurfaceView() {
        return surfaceView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (controller.isVisible()) {
            controller.hide();
        } else {
            maybeShowController(true);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null)
            return false;
        maybeShowController(true);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return useController ? controller.dispatchKeyEvent(event) : super.dispatchKeyEvent(event);
    }

    private void maybeShowController(boolean isForced) {
        if (!useController || player == null) {
            return;
        }
        int playbackState = player.getPlaybackState();
        boolean showIndefinitely = playbackState == ExoPlayer.STATE_IDLE
                || playbackState == ExoPlayer.STATE_ENDED || !player.getPlayWhenReady();
        boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        if (isForced || showIndefinitely || wasShowingIndefinitely) {
            controller.show();
        }
    }

    private final class ComponentListener implements SimpleExoPlayer.VideoListener,
            TextRenderer.Output, ExoPlayer.EventListener {

        // TextRenderer.Output implementation
        @Override
        public void onCues(List<Cue> cues) {
            subtitleLayout.onCues(cues);
        }

        // SimpleExoPlayer.VideoListener implementation
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            layout.setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
        }

        @Override
        public void onRenderedFirstFrame() {
            shutterView.setVisibility(GONE);
        }

        @Override
        public void onVideoTracksDisabled() {
            shutterView.setVisibility(VISIBLE);
        }

        // ExoPlayer.EventListener implementation
        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            maybeShowController(false);
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.
        }
    }
}
