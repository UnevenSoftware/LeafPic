package com.horaapps.leafpic.Base;

import java.io.File;
import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class AlbumsComapartors {
    boolean ascending = true;

    public AlbumsComapartors(boolean ascending){
        this.ascending = ascending;
    }

    public void toggleAscending() {
        ascending =! ascending;
    }

    public Comparator<newAlbum> getDateComapartor(){
        return new Comparator<newAlbum>(){
            public int compare(newAlbum f1, newAlbum f2) {
                return 0;
                //return Long.compare(f1.lastModified(), f2.lastModified());
            }
        };
    }

    public Comparator<newAlbum> getNameComapartor() {
        return new Comparator<newAlbum>() {
            public int compare(newAlbum f1, newAlbum f2) {
                return ascending
                        ? f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase())
                        : f2.getName().toLowerCase().compareTo(f1.getName().toLowerCase());
            }
        };
    }
}
