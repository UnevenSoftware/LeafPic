package org.horaapps.leafpic.fragments;

import android.support.v4.app.Fragment;

import org.horaapps.leafpic.util.Themeable;

/**
 * Created by dnld on 4/3/17.
 */

public abstract class BaseFragment extends Fragment implements IFragment, Themeable {
    public boolean onBackPressed(){
        if (editMode()){
            clearSelected();
            return true;
        }
        return false;
    }
}
