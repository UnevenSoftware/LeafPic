package org.horaapps.leafpic.fragments;


import org.horaapps.liz.Themed;
import org.horaapps.liz.ThemedFragment;

/**
 * Created by dnld on 4/3/17.
 */

public abstract class BaseFragment extends ThemedFragment implements IFragment, Themed {
    public boolean onBackPressed(){
        if (editMode()){
            clearSelected();
            return true;
        }
        return false;
    }
}
