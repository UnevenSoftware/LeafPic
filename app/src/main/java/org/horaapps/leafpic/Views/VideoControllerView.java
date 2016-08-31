package org.horaapps.leafpic.Views;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.util.PlayerControl;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.ThemeHelper;

import java.lang.ref.WeakReference;

public class VideoControllerView extends FrameLayout {
  private static final String TAG = "VideoControllerView";

  private PlayerControl  mPlayer;
  private Context mContext;
  private ViewGroup mAnchor;
  private View mRoot;
  private SeekBar mProgress;
  private TextView mEndTime;
  private TextView mCurrentTime;
  private boolean             mShowing;
  private boolean             mDragging;
  private static final int    sDefaultTimeout = 3000;
  private static final int    FADE_OUT = 1;
  private static final int    SHOW_PROGRESS = 2;
  private boolean             mUseFastForward;
  private boolean             mFromXml;
  private boolean             mListenersSet;
  private View.OnClickListener mNextListener, mPrevListener;
  private StringBuilder               mFormatBuilder;
  private IconicsImageView mPauseButton;
  private IconicsImageView         mFfwdButton;
  private IconicsImageView         mRewButton;
  private IconicsImageView         mNextButton;
  private IconicsImageView mPrevButton;
  private Handler mHandler = new MessageHandler(this);

  private ThemeHelper themeHelper;

  public VideoControllerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mRoot = null;
    mContext = context;
    mUseFastForward = true;
    mFromXml = true;

    Log.i(TAG, TAG);
  }

  private VideoControllerView(Context context, boolean useFastForward) {
    super(context);
    mContext = context;
    mUseFastForward = useFastForward;
    themeHelper = new ThemeHelper(context);

    Log.i(TAG, TAG);
  }

  public VideoControllerView(Context context) {
    this(context, true);

    Log.i(TAG, TAG);
  }

  @Override
  public void onFinishInflate() {
    if (mRoot != null)
      initControllerView(mRoot);
  }

  public void setMediaPlayer(PlayerControl player) {
    mPlayer = player;
    updatePausePlay();
    //updateFullScreen();
  }

  /**
   * Set the view that acts as the anchor for the control view.
   * This can for example be a VideoView, or your Activity's main view.
   * @param view The view to which to anchor the controller when it is visible.
   */
  public void setAnchorView(ViewGroup view) {
    mAnchor = view;

    FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                                                                               ViewGroup.LayoutParams.MATCH_PARENT,
                                                                               ViewGroup.LayoutParams.MATCH_PARENT
    );

    removeAllViews();
    View v = makeControllerView();
    addView(v, frameParams);
  }

  /**
   * Create the view that holds the widgets that control playback.
   * Derived classes can override this to create their own.
   * @return The controller view.
   * @hide This doesn't work as advertised
   */
  protected View makeControllerView() {
    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mRoot = inflate.inflate(R.layout.media_controller, null);

    initControllerView(mRoot);

    return mRoot;
  }

  private void initControllerView(View v) {
    mPauseButton = (IconicsImageView) v.findViewById(R.id.pause);
    if (mPauseButton != null) {
      mPauseButton.requestFocus();
      mPauseButton.setOnClickListener(mPauseListener);
    }

    /*mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
    if (mFullscreenButton != null) {
      mFullscreenButton.requestFocus();
      mFullscreenButton.setOnClickListener(mFullscreenListener);
    }*/

    mFfwdButton = (IconicsImageView) v.findViewById(R.id.ffwd);
    if (mFfwdButton != null) {
      mFfwdButton.setOnClickListener(mFfwdListener);
      if (!mFromXml) {
        mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
      }
    }

    mRewButton = (IconicsImageView) v.findViewById(R.id.rew);
    if (mRewButton != null) {
      mRewButton.setOnClickListener(mRewListener);
      if (!mFromXml) {
        mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
      }
    }

    // By default these are hidden. They will be enabled when setPrevNextListeners() is called
    mNextButton = (IconicsImageView) v.findViewById(R.id.next);
    if (mNextButton != null && !mFromXml && !mListenersSet) {
      mNextButton.setVisibility(View.GONE);
    }
    mPrevButton = (IconicsImageView) v.findViewById(R.id.prev);
    if (mPrevButton != null && !mFromXml && !mListenersSet) {
      mPrevButton.setVisibility(View.GONE);
    }

    mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
    if (mProgress != null) {
      mProgress.setOnSeekBarChangeListener(mSeekListener);
      mProgress.setMax(1000);
      themeHelper.themeSeekBar(mProgress);
    }



    mEndTime = (TextView) v.findViewById(R.id.time);
    mCurrentTime = (TextView) v.findViewById(R.id.time_current);
    mFormatBuilder = new StringBuilder();
    //mFormatter = new NumberPicker.Formatter(mFormatBuilder, Locale.getDefault());

    installPrevNextListeners();
  }

  /**
   * Show the controller on screen. It will go away
   * automatically after 3 seconds of inactivity.
   */
  public void show() {
    show(sDefaultTimeout);
  }

  /**
   * Disable pause or seek buttons if the stream cannot be paused or seeked.
   * This requires the control interface to be a MediaPlayerControlExt
   */
  private void disableUnsupportedButtons() {
    if (mPlayer == null) {
      return;
    }

    try {
      if (mPauseButton != null && !mPlayer.canPause()) {
        mPauseButton.setEnabled(false);
      }
      if (mRewButton != null && !mPlayer.canSeekBackward()) {
        mRewButton.setEnabled(false);
      }
      if (mFfwdButton != null && !mPlayer.canSeekForward()) {
        mFfwdButton.setEnabled(false);
      }
    } catch (IncompatibleClassChangeError ex) {
      // We were given an old version of the interface, that doesn't have
      // the canPause/canSeekXYZ methods. This is OK, it just means we
      // assume the media can be paused and seeked, and so we don't disable
      // the buttons.
    }
  }

  /**
   * Show the controller on screen. It will go away
   * automatically after 'timeout' milliseconds of inactivity.
   * @param timeout The timeout in milliseconds. Use 0 to show
   * the controller until hide() is called.
   */
  public void show(int timeout) {
    if (!mShowing && mAnchor != null) {
      setProgress();
      if (mPauseButton != null) {
        mPauseButton.requestFocus();
      }
      disableUnsupportedButtons();

      FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                                                                         ViewGroup.LayoutParams.MATCH_PARENT,
                                                                         ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                         Gravity.BOTTOM
      );

      mAnchor.addView(this, tlp);
      mShowing = true;
    }
    updatePausePlay();
    //updateFullScreen();

    // cause the progress bar to be updated even if mShowing
    // was already true.  This happens, for example, if we're
    // paused with the progress bar showing the user hits play.
    mHandler.sendEmptyMessage(SHOW_PROGRESS);

    Message msg = mHandler.obtainMessage(FADE_OUT);
    if (timeout != 0) {
      mHandler.removeMessages(FADE_OUT);
      mHandler.sendMessageDelayed(msg, timeout);
    }
  }

  public boolean isShowing() {
    return mShowing;
  }

  /**
   * Remove the controller from the screen.
   */
  public void hide() {
    if (mAnchor == null) {
      return;
    }

    try {
      mAnchor.removeView(this);
      mHandler.removeMessages(SHOW_PROGRESS);
    } catch (IllegalArgumentException ex) {
      Log.w("MediaController", "already removed");
    }
    mShowing = false;
  }

  private String stringForTime(int timeMs) {
    int totalSeconds = timeMs / 1000;

    int seconds = totalSeconds % 60;
    int minutes = (totalSeconds / 60) % 60;
    int hours   = totalSeconds / 3600;

    mFormatBuilder.setLength(0);
    if (hours > 0) {
      return String.format("%d:%02d:%02d", hours, minutes, seconds);
    } else {
      return String.format("%02d:%02d", minutes, seconds);
    }
  }

  private int setProgress() {
    if (mPlayer == null || mDragging) {
      return 0;
    }

    int position = mPlayer.getCurrentPosition();
    int duration = mPlayer.getDuration();
    if (mProgress != null) {
      if (duration > 0) {
        // use long to avoid overflow
        long pos = 1000L * position / duration;
        mProgress.setProgress( (int) pos);
      }
      int percent = mPlayer.getBufferPercentage();
      mProgress.setSecondaryProgress(percent * 10);
    }

    if (mEndTime != null)
      mEndTime.setText(stringForTime(duration));
    if (mCurrentTime != null)
      mCurrentTime.setText(stringForTime(position));

    return position;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    show(sDefaultTimeout);
    return true;
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {
    show(sDefaultTimeout);
    return false;
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (mPlayer == null) {
      return true;
    }

    int keyCode = event.getKeyCode();
    final boolean uniqueDown = event.getRepeatCount() == 0
                                       && event.getAction() == KeyEvent.ACTION_DOWN;
    if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
      if (uniqueDown) {
        doPauseResume();
        show(sDefaultTimeout);
        if (mPauseButton != null) {
          mPauseButton.requestFocus();
        }
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
      if (uniqueDown && !mPlayer.isPlaying()) {
        mPlayer.start();
        updatePausePlay();
        show(sDefaultTimeout);
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                       || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
      if (uniqueDown && mPlayer.isPlaying()) {
        mPlayer.pause();
        updatePausePlay();
        show(sDefaultTimeout);
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                       || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                       || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
      // don't show the controls for volume adjustment
      return super.dispatchKeyEvent(event);
    } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
      if (uniqueDown) {
        hide();
      }
      return true;
    }

    show(sDefaultTimeout);
    return super.dispatchKeyEvent(event);
  }

  private View.OnClickListener mPauseListener = new View.OnClickListener() {
    public void onClick(View v) {
      doPauseResume();
      show(sDefaultTimeout);
    }
  };

  /*private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
    public void onClick(View v) {
      doToggleFullscreen();
      show(sDefaultTimeout);
    }
  };*/

  public void updatePausePlay() {
    if (mRoot == null || mPauseButton == null || mPlayer == null) {
      return;
    }
    mPauseButton.setIcon(mPlayer.isPlaying() ? GoogleMaterial.Icon.gmd_pause_circle_outline : GoogleMaterial.Icon.gmd_play_circle_outline);



    /*if (mPlayer.isPlaying()) {
      //ThemeHelper.getIcon(getContext(), GoogleMaterial.Icon.gmd_pause);
      mPauseButton.setImageDrawable(ThemeHelper.getIcon(getContext(), GoogleMaterial.Icon.gmd_pause));
      //mPauseButton.setImageResource(R.drawable.ic_media_pause);
    } else {
      mPauseButton.setImageResource(R.drawable.ic_media_play);
    }*/
  }

  /*public void updateFullScreen() {
    if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
      return;
    }


    mPauseButton.setImageDrawable(ThemeHelper.getIcon(getContext(),
            mPlayer.isFullScreen() ? GoogleMaterial.Icon.gmd_fullscreen_exit : GoogleMaterial.Icon.gmd_fullscreen));
    *//*//*/
    /*if (mPlayer.isFullScreen()) {
      //mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
    }
    else {
      //mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
    }*//**//*
  }*/

  private void doPauseResume() {
    if (mPlayer == null) {
      return;
    }

    if (mPlayer.isPlaying()) {
      mPlayer.pause();
    } else {
      mPlayer.start();
    }
    updatePausePlay();
  }

  /*private void doToggleFullscreen() {
    if (mPlayer == null) {
      return;
    }

    mPlayer.toggleFullScreen();
  }*/

  // There are two scenarios that can trigger the seekbar listener to trigger:
  //
  // The first is the user using the touchpad to adjust the posititon of the
  // seekbar's thumb. In this case onStartTrackingTouch is called followed by
  // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
  // We're setting the field "mDragging" to true for the duration of the dragging
  // session to avoid jumps in the position in case of ongoing playback.
  //
  // The second scenario involves the user operating the scroll ball, in this
  // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
  // we will simply apply the updated position without suspending regular updates.
  private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
    public void onStartTrackingTouch(SeekBar bar) {
      show(3600000);

      mDragging = true;

      // By removing these pending progress messages we make sure
      // that a) we won't update the progress while the user adjusts
      // the seekbar and b) once the user is done dragging the thumb
      // we will post one of these messages to the queue again and
      // this ensures that there will be exactly one message queued up.
      mHandler.removeMessages(SHOW_PROGRESS);
    }

    public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
      if (mPlayer == null) {
        return;
      }

      if (!fromuser) {
        // We're not interested in programmatically generated changes to
        // the progress bar's position.
        return;
      }

      long duration = mPlayer.getDuration();
      long newposition = (duration * progress) / 1000L;
      mPlayer.seekTo( (int) newposition);
      if (mCurrentTime != null)
        mCurrentTime.setText(stringForTime( (int) newposition));
    }

    public void onStopTrackingTouch(SeekBar bar) {
      mDragging = false;
      setProgress();
      updatePausePlay();
      show(sDefaultTimeout);

      // Ensure that progress is properly updated in the future,
      // the call to show() does not guarantee this because it is a
      // no-op if we are already showing.
      mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }
  };

  @Override
  public void setEnabled(boolean enabled) {
    if (mPauseButton != null) {
      mPauseButton.setEnabled(enabled);
    }
    if (mFfwdButton != null) {
      mFfwdButton.setEnabled(enabled);
    }
    if (mRewButton != null) {
      mRewButton.setEnabled(enabled);
    }
    if (mNextButton != null) {
      mNextButton.setEnabled(enabled && mNextListener != null);
    }
    if (mPrevButton != null) {
      mPrevButton.setEnabled(enabled && mPrevListener != null);
    }
    if (mProgress != null) {
      mProgress.setEnabled(enabled);
    }
    disableUnsupportedButtons();
    super.setEnabled(enabled);
  }

  @Override
  public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(VideoControllerView.class.getName());
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(VideoControllerView.class.getName());
  }

  private View.OnClickListener mRewListener = new View.OnClickListener() {
    public void onClick(View v) {
      if (mPlayer == null) {
        return;
      }

      int pos = mPlayer.getCurrentPosition();
      pos -= 5000; // milliseconds
      mPlayer.seekTo(pos);
      setProgress();

      show(sDefaultTimeout);
    }
  };

  private View.OnClickListener mFfwdListener = new View.OnClickListener() {
    public void onClick(View v) {
      if (mPlayer == null) {
        return;
      }

      int pos = mPlayer.getCurrentPosition();
      pos += 15000; // milliseconds
      mPlayer.seekTo(pos);
      setProgress();

      show(sDefaultTimeout);
    }
  };

  private void installPrevNextListeners() {
    if (mNextButton != null) {
      mNextButton.setOnClickListener(mNextListener);
      mNextButton.setEnabled(mNextListener != null);
    }

    if (mPrevButton != null) {
      mPrevButton.setOnClickListener(mPrevListener);
      mPrevButton.setEnabled(mPrevListener != null);
    }
  }

  public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
    mNextListener = next;
    mPrevListener = prev;
    mListenersSet = true;

    if (mRoot != null) {
      installPrevNextListeners();

      if (mNextButton != null && !mFromXml) {
        mNextButton.setVisibility(View.VISIBLE);
      }
      if (mPrevButton != null && !mFromXml) {
        mPrevButton.setVisibility(View.VISIBLE);
      }
    }
  }

  public interface MediaPlayerControl {
    void    start();
    void    pause();
    int     getDuration();
    int     getCurrentPosition();
    void    seekTo(int pos);
    boolean isPlaying();
    int     getBufferPercentage();
    boolean canPause();
    boolean canSeekBackward();
    boolean canSeekForward();
    boolean isFullScreen();
    void    toggleFullScreen();
  }

  private static class MessageHandler extends Handler {
    private final WeakReference<VideoControllerView> mView;

    MessageHandler(VideoControllerView view) {
      mView = new WeakReference<VideoControllerView>(view);
    }
    @Override
    public void handleMessage(Message msg) {
      VideoControllerView view = mView.get();
      if (view == null || view.mPlayer == null) {
        return;
      }

      int pos;
      switch (msg.what) {
        case FADE_OUT:
          view.hide();
          break;
        case SHOW_PROGRESS:
          pos = view.setProgress();
          if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
            msg = obtainMessage(SHOW_PROGRESS);
            sendMessageDelayed(msg, 1000 - (pos % 1000));
          }
          break;
      }
    }
  }
}