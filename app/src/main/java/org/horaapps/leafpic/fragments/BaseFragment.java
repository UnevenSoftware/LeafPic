package org.horaapps.leafpic.fragments;

import android.content.Context;
import org.horaapps.liz.ThemedFragment;

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
}
