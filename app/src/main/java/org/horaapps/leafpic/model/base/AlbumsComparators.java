package org.horaapps.leafpic.model.base;

import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.util.NumericComparator;

import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class AlbumsComparators {

    public static Comparator<Album> getComparator(SortingMode sortingMode, SortingOrder sortingOrder) {
        switch (sortingMode) {
            case NAME:
                return getNameComparator(sortingOrder);
            case SIZE:
                return getSizeComparator(sortingOrder);
            case DATE: default:
                return getDateComparator(sortingOrder);
            case NUMERIC:
                return getNumericComparator(sortingOrder);
        }
    }

    private static Comparator<Album> getDateComparator(final SortingOrder sortingOrder){
        return new Comparator<Album>(){
            public int compare(Album a1, Album a2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        if (a1.isPinned() == a2.isPinned())
                            return a1.getMedia(0).getDateModified().compareTo(a2.getMedia(0).getDateModified());
                        return a1.isPinned() ? -1 : 1;

                    case DESCENDING: default:
                        if (a1.isPinned() == a2.isPinned())
                            return a2.getMedia(0).getDateModified().compareTo(a1.getMedia(0).getDateModified());
                        return a2.isPinned() ? 1 : -1;
                }
            }
        };
    }

    private static Comparator<Album> getNameComparator(final SortingOrder sortingOrder) {
        return new Comparator<Album>() {
            public int compare(Album a1, Album a2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        if (a1.isPinned() == a2.isPinned())
                            return a1.getName().toLowerCase().compareTo(a2.getName().toLowerCase());
                        return a1.isPinned() ? -1 : 1;

                    case DESCENDING: default:
                        if (a1.isPinned() == a2.isPinned())
                            return a2.getName().toLowerCase().compareTo(a1.getName().toLowerCase());
                        return a2.isPinned() ? 1 : -1;
                }
            }
        };
    }

    private static Comparator<Album> getSizeComparator(final SortingOrder sortingOrder) {
        return new Comparator<Album>() {
            public int compare(Album a1, Album a2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        if (a1.isPinned() == a2.isPinned())
                            return a1.getCount() - a2.getCount();
                        return a1.isPinned() ? -1 : 1;

                    case DESCENDING: default:
                        if (a1.isPinned() == a2.isPinned())
                            return a2.getCount() - a1.getCount() ;
                        return a2.isPinned() ? 1 : -1;
                }
            }
        };
    }

    private static Comparator<Album> getNumericComparator(final SortingOrder sortingOrder) {
        return new Comparator<Album>() {
            public int compare(Album a1, Album a2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        if (a1.isPinned() == a2.isPinned())
                            return NumericComparator.filevercmp(a1.getName().toLowerCase(), a2.getName().toLowerCase());
                        return a1.isPinned() ? -1 : 1;

                    case DESCENDING: default:
                        if (a1.isPinned() == a2.isPinned())
                            return NumericComparator.filevercmp(a2.getName().toLowerCase(), a1.getName().toLowerCase());
                        return a2.isPinned() ? 1 : -1;
                }
            }
        };
    }
}
