package org.horaapps.leafpic.fragments;

import android.content.Context;

import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.items.ActionsListener;
import org.horaapps.liz.Themed;
import org.horaapps.liz.ThemedFragment;

import java.util.ArrayList;

/**
 * Base Fragment for abstraction logic.
 */
public abstract class BaseFragment extends ThemedFragment {

    private NothingToShowListener nothingToShowListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NothingToShowListener)
            nothingToShowListener = (NothingToShowListener) context;
    }

    public NothingToShowListener getNothingToShowListener() {
        return nothingToShowListener;
    }

    public void setNothingToShowListener(NothingToShowListener nothingToShowListener) {
        this.nothingToShowListener = nothingToShowListener;
    }
    public void updateMedia(ArrayList<Media> media) {

    }
}
