package org.horaapps.leafpic.fragments;


import org.horaapps.leafpic.R;
import org.horaapps.liz.Themed;
import org.horaapps.liz.ThemedFragment;

/**
 * Created by dnld on 4/3/17.
 */

public abstract class BaseFragment extends ThemedFragment implements IFragment, Themed {

    private EditModeListener editModeListener;
    private NothingToShowListener nothingToShowListener;

    public String getTitle() {
        return getString(R.string.app_name);
    }

    public boolean onBackPressed() {
        if (editMode()){
            clearSelected();
            editModeListener.changedEditMode(false, 0, 0, null, getTitle());
            return true;
        }
        return false;
    }

    public EditModeListener getEditModeListener() {
        return editModeListener;
    }

    public void setEditModeListener(EditModeListener editModeListener) {
        this.editModeListener = editModeListener;
    }

    public NothingToShowListener getNothingToShowListener() {
        return nothingToShowListener;
    }

    public void setNothingToShowListener(NothingToShowListener nothingToShowListener) {
        this.nothingToShowListener = nothingToShowListener;
    }
}
