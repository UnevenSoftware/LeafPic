package org.horaapps.leafpic.data.base;

import org.horaapps.leafpic.data.Album;
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
            public int compare(Album f1, Album f2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        return f1.getMedia(0).getDateModified().compareTo(f2.getMedia(0).getDateModified());
                    case DESCENDING: default:
                        return f2.getMedia(0).getDateModified().compareTo(f1.getMedia(0).getDateModified());
                }
            }
        };
    }

    private static Comparator<Album> getNameComparator(final SortingOrder sortingOrder) {
        return new Comparator<Album>() {
            public int compare(Album f1, Album f2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());

                    case DESCENDING: default:
                        return f2.getName().toLowerCase().compareTo(f1.getName().toLowerCase());
                }
            }
        };
    }

    private static Comparator<Album> getSizeComparator(final SortingOrder sortingOrder) {
        return new Comparator<Album>() {
            public int compare(Album f1, Album f2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        return f1.getCount() - f2.getCount();

                    case DESCENDING: default:
                        return f2.getCount() - f1.getCount();
                }
            }
        };
    }

    private static Comparator<Album> getNumericComparator(final SortingOrder sortingOrder) {
        return new Comparator<Album>() {
            public int compare(Album f1, Album f2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        return NumericComparator.filevercmp(f1.getName().toLowerCase(), f2.getName().toLowerCase());

                    case DESCENDING: default:
                        return NumericComparator.filevercmp(f2.getName().toLowerCase(), f1.getName().toLowerCase());
                }
            }
        };
    }
}
