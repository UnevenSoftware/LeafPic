package org.horaapps.leafpic.data.base;

import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.util.NumericComparator;

import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */

public class MediaComparators {

    public static Comparator<Media> getComparator(SortingMode sortingMode, SortingOrder sortingOrder) {
        switch (sortingMode) {
            case NAME:
                return getNameComparator(sortingOrder);
            case DATE: default:
                return getDateComparator(sortingOrder);
            case SIZE:
                return getSizeComparator(sortingOrder);
            case TYPE:
                return getTypeComparator(sortingOrder);
            case NUMERIC:
                return getNumericComparator(sortingOrder);
        }
    }

    private static Comparator<Media> getDateComparator(final SortingOrder sortingOrder){
        return new Comparator<Media>(){
            public int compare(Media f1, Media f2) {
                switch (sortingOrder){
                    case ASCENDING:
                        return f1.getDateModified().compareTo(f2.getDateModified());
                    case DESCENDING: default:
                        return f2.getDateModified().compareTo(f1.getDateModified());
                }

            }
        };
    }

    private static Comparator<Media> getNameComparator(final SortingOrder sortingOrder) {
        return new Comparator<Media>() {
            public int compare(Media f1, Media f2) {
                switch (sortingOrder){
                    case ASCENDING:
                        return f1.getPath().compareTo(f2.getPath());
                    case DESCENDING: default:
                        return f2.getPath().compareTo(f1.getPath());
                }
            }
        };
    }

    private static Comparator<Media> getSizeComparator(final SortingOrder sortingOrder) {
        return new Comparator<Media>() {
            public int compare(Media f1, Media f2) {
                switch (sortingOrder){
                    case ASCENDING:
                        return Long.compare(f1.getSize(), f2.getSize());
                    case DESCENDING: default:
                        return Long.compare(f2.getSize(), f1.getSize());
                }
            }
        };
    }

    private static Comparator<Media> getTypeComparator(final SortingOrder sortingOrder) {
        return new Comparator<Media>() {
            public int compare(Media f1, Media f2) {
                switch (sortingOrder){
                    case ASCENDING:
                        return f1.getMimeType().compareTo(f2.getMimeType());
                    case DESCENDING: default:
                        return f2.getMimeType().compareTo(f1.getMimeType());
                }
            }
        };
    }

    private static Comparator<Media> getNumericComparator(final SortingOrder sortingOrder) {
        return new Comparator<Media>() {
            public int compare(Media f1, Media f2) {
                switch (sortingOrder) {
                    case ASCENDING:
                        return NumericComparator.filevercmp(f1.getPath(), f2.getPath());
                    case DESCENDING: default:
                        return NumericComparator.filevercmp(f2.getPath(), f1.getPath());
                }
            }
        };
    }
}
