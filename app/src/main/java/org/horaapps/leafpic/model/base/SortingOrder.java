package org.horaapps.leafpic.model.base;

/**
 * Created by dnld on 18/08/16.
 */

public enum SortingOrder {
  ASCENDING (0), DESCENDING (1);

  int value;

  SortingOrder(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static SortingOrder fromValue(int value) {
    switch (value) {
      case 0: return ASCENDING;
      case 1: default: return DESCENDING;
    }
  }
}
