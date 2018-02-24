package org.horaapps.leafpic.util;

import android.util.Log;

import java.util.Date;

/**
 * Created by dnld on 24/02/18.
 */

public class Timer {
    String tag;
    long start, end;

    public Timer(String tag) {
        this.tag = tag;
    }

    public void start() {
        this.start = new Date().getTime();
    }

    public void stop() {
        this.end = new Date().getTime();

        Log.d("timer-" + tag, end - start + "");
    }
}
