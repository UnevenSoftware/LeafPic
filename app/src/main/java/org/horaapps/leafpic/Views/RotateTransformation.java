package org.horaapps.leafpic.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by dnld on 21/08/16.
 */

public class RotateTransformation extends BitmapTransformation {

  private float rotateRotationAngle = 0f;
  private boolean increment = false;

  public RotateTransformation(Context context, float rotateRotationAngle, boolean increment) {
    super(context);
    this.rotateRotationAngle = rotateRotationAngle;
    this.increment = increment;
  }

  @Override
  protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
    Matrix matrix = new Matrix();

    if (increment) {
      if (rotateRotationAngle > 0) matrix.postRotate(rotateRotationAngle);
      else matrix.preRotate(rotateRotationAngle * -1);
    } else
      matrix.setRotate(rotateRotationAngle);
    /*if (rotateRotationAngle > 0) matrix.postRotate(rotateRotationAngle);
    else matrix.preRotate(rotateRotationAngle * -1);*/

    return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
  }

  @Override
  public String getId() {
    return "rotate" + rotateRotationAngle;
  }
}