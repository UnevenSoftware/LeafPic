package org.horaapps.leafpic.data.sort;

/**
 * Created by dnld on 18/08/16.
 */

public enum SortingOrder {
    ASCENDING(1), DESCENDING(0);

    int value;

    SortingOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isAscending() {
        return value == ASCENDING.getValue();
    }

    public static SortingOrder fromValue(boolean value) {
        return value ? ASCENDING : DESCENDING;
    }

    public static SortingOrder fromValue(int value) {
        return value == 0 ? DESCENDING : ASCENDING;
    }
}
