package org.horaapps.leafpic.data.sort;

import android.provider.MediaStore;

/**
 * Created by dnld on 18/08/16.
 */

public enum SortingMode {
  NAME (0, MediaStore.MediaColumns.DISPLAY_NAME),
  DATE (1, MediaStore.MediaColumns.DATE_MODIFIED),
  SIZE(2, MediaStore.MediaColumns.SIZE),
  TYPE(3, MediaStore.MediaColumns.MIME_TYPE),
  NUMERIC(4, MediaStore.MediaColumns.DISPLAY_NAME);

  int value;
  String column;

  SortingMode(int value, String column) {
    this.value = value;
    this.column = column;
  }

  public String getColumn() {
    return column;
  }

  public int getValue() {
    return value;
  }

  public static SortingMode fromValue(int value) {
    switch (value) {
      case 0: return NAME;
      case 1: default: return DATE;
      case 2: return SIZE;
      case 3: return TYPE;
      case 4: return NUMERIC;
    }
  }
}
