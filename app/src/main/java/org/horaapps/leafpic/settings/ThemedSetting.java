package org.horaapps.leafpic.settings;

import org.horaapps.leafpic.util.PreferenceUtil;

import horaapps.org.liz.ThemedActivity;

/**
 * Created by dnld on 12/9/16.
 */

class ThemedSetting {

    private ThemedActivity activity;
    private PreferenceUtil SP;

    ThemedSetting(ThemedActivity activity, PreferenceUtil SP) {
        this.activity = activity;
        this.SP = SP;
    }

    public ThemedSetting(PreferenceUtil SP) {
        this.SP = SP;
    }

    public ThemedActivity getActivity() {
        return activity;
    }

    public PreferenceUtil getSP() {
        return SP;
    }
}
