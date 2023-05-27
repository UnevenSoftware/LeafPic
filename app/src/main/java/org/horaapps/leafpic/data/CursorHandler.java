package org.horaapps.leafpic.data;

import android.database.Cursor;

/**
 * Created by dnld on 3/13/17.
 */
public interface CursorHandler<T> {

    T handle(Cursor cu);

    static String[] getProjection() {
        return new String[0];
    }
}
