package com.horaapps.leafpic.Base;

import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class AlbumsComparators {
    boolean ascending = true;

    public AlbumsComparators(boolean ascending){
        this.ascending = ascending;
    }

    public Comparator<Album> getDateComparator(){
        return new Comparator<Album>(){
            public int compare(Album f1, Album f2) {
                return ascending
                        ? Long.compare(f1.getMedia(0).getDateModified(),f2.getMedia(0).getDateModified())
                        : Long.compare(f2.getMedia(0).getDateModified(),f1.getMedia(0).getDateModified());

            }
        };
    }

    public Comparator<Album> getNameComparator() {
        return new Comparator<Album>() {
            public int compare(Album f1, Album f2) {
                return ascending
                        ? f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase())
                        : f2.getName().toLowerCase().compareTo(f1.getName().toLowerCase());
            }
        };
    }

    public Comparator<Album> getSizeComparator() {
        return new Comparator<Album>() {
            public int compare(Album f1, Album f2) {
                return ascending
                        ? f1.getCount() - f2.getCount()
                        : f2.getCount() - f1.getCount();
            }
        };
    }
}
