package org.horaapps.leafpic.settings;

import horaapps.org.liz.ThemedActivity;

/**
 * Created by dnld on 12/9/16.
 */

class ThemedSetting {

    private ThemedActivity activity;

    ThemedSetting(ThemedActivity activity) {
        this.activity = activity;
    }

    public ThemedSetting() {
    }

    public ThemedActivity getActivity() {
        return activity;
    }

}
