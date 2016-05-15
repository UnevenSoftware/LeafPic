package com.horaapps.leafpic.Base;

import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class MediaComparators {
    boolean ascending = true;

    public MediaComparators(boolean ascending){
        this.ascending = ascending;
    }

    public Comparator<Media> getDateComparator(){
        return new Comparator<Media>(){
            public int compare(Media f1, Media f2) {
                return ascending
                        ? Long.compare(f1.getDateModified(), f2.getDateModified())
                        : Long.compare(f2.getDateModified(), f1.getDateModified());

            }
        };
    }

    public Comparator<Media> getNameComparator() {
        return new Comparator<Media>() {
            public int compare(Media f1, Media f2) {
                return ascending
                        ? f1.getPath().compareTo(f2.getPath())
                        : f2.getPath().compareTo(f1.getPath());
            }
        };
    }

    public Comparator<Media> getSizeComparator() {
        return new Comparator<Media>() {
            public int compare(Media f1, Media f2) {
                return ascending
                        ? Long.compare(f1.getSize(), f2.getSize())
                        : Long.compare(f2.getSize(), f1.getSize());
            }
        };
    }
}
