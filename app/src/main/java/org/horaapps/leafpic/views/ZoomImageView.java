package org.horaapps.leafpic.views;

import android.animation.AnimatorSet;
import android.view.GestureDetector;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

/**
 * Created by dnld on 20/09/16.
 */

public class ZoomImageView {
  private boolean draggingDown = false;
  private float dragY;
  private float translationX;
  private float translationY;
  private float scale = 1;
  private float animateToX;
  private float animateToY;
  private float animateToScale;
  private float animationValue;
  private int currentRotation;
  private long animationStartTime;
  private AnimatorSet imageMoveAnimation;
  private AnimatorSet changeModeAnimation;
  private GestureDetector gestureDetector;
  private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);
  private float pinchStartDistance;
  private float pinchStartScale = 1;
  private float pinchCenterX;
  private float pinchCenterY;
  private float pinchStartX;
  private float pinchStartY;
  private float moveStartX;
  private float moveStartY;
  private float minX;
  private float maxX;
  private float minY;
  private float maxY;
  private boolean canZoom = true;
  private boolean changingPage = false;
  private boolean zooming = false;
  private boolean moving = false;
  private boolean doubleTap = false;
  private boolean invalidCoords = false;
  private boolean canDragDown = true;
  private boolean zoomAnimation = false;
  private boolean discardTap = false;
  private int switchImageAfterAnimation = 0;
  private VelocityTracker velocityTracker = null;
  private Scroller scroller = null;

  
}
