package com.horaapps.leafpic.Base;

import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class MediaComapartors {
    boolean ascending = true;

    public MediaComapartors(boolean ascending){
        this.ascending = ascending;
    }

    public Comparator<newMedia> getDateComapartor(){
        return new Comparator<newMedia>(){
            public int compare(newMedia f1, newMedia f2) {
                return ascending
                        ? Long.compare(f1.getDateModified(),f2.getDateModified())
                        : Long.compare(f2.getDateModified(),f1.getDateModified());

            }
        };
    }

    public Comparator<newMedia> getNameComapartor() {
        return new Comparator<newMedia>() {
            public int compare(newMedia f1, newMedia f2) {
                return ascending
                        ? f1.getPath().compareTo(f2.getPath())
                        : f2.getPath().compareTo(f1.getPath());
            }
        };
    }

    public Comparator<newMedia> getSizeComapartor() {
        return new Comparator<newMedia>() {
            public int compare(newMedia f1, newMedia f2) {
                return 0;
            }
        };
    }
}
