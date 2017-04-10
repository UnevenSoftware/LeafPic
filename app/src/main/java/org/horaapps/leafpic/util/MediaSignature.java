package org.horaapps.leafpic.util;

import com.bumptech.glide.signature.StringSignature;

import org.horaapps.leafpic.data.Media;

/**
 * Created by dnld on 21/08/16.
 */

public class MediaSignature extends StringSignature {

  private MediaSignature(String path, long lastModified, int orientation) {
    super(path + lastModified + orientation);
  }

  public MediaSignature(Media media) {
    this(media.getPath(), media.getDateModified(), media.getOrientation());
  }
}
