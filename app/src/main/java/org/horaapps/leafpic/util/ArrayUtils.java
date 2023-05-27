package org.horaapps.leafpic.util;

import android.support.annotation.NonNull;

/**
 * All kinds of Array helpers belong here
 */
public final class ArrayUtils {

    /**
     * Find the index of an element in an array.
     * Performs a linear search across the array.
     *
     * @param array   The array to search
     * @param element The element to find
     * @return The position of element in array, else -1 if not found.
     */
    public static <T> int getIndex(@NonNull T[] array, @NonNull T element) {
        for (int pos = 0; pos < array.length; pos++) {
            if (array[pos].equals(element))
                return pos;
        }
        return -1;
    }
}
