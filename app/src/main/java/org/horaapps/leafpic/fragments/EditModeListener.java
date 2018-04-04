package org.horaapps.leafpic.fragments;

import android.view.View;

import javax.annotation.Nullable;

/**
 * Created by dnld on 12/16/17.
 */

public interface EditModeListener {
    void changedEditMode(boolean editMode, int selected, int total, @Nullable View.OnClickListener listener, @Nullable String title);

    /**
     * Propagate the selected item count to listeners.
     *
     * @param count The number of items selected.
     * @param total The total number of items.
     */
    void onItemsSelected(int count, int total);
}
