package org.horaapps.leafpic.new_way;

import android.database.Cursor;

import java.sql.SQLException;

/**
 * Created by dnld on 3/13/17.
 */

public interface CursorHandler<T> {
    T handle(Cursor cu) throws SQLException;
}
