package org.horaapps.leafpic.fragments;

import org.horaapps.leafpic.activities.theme.Themed;
import org.horaapps.leafpic.activities.theme.ThemedFragment;

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
