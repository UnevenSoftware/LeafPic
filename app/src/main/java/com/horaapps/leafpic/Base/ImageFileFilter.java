package com.horaapps.leafpic.Base;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Created by dnld on 24/04/16.
 */
public class ImageFileFilter implements FilenameFilter
{

    public final static int FILTER_ALL = 0;
    public final static int FILTER_IMAGES = 1;
    public final static int FILTER_GIFS = 2;
    public final static int FILTER_VIDEO = 3;

    public static String[] allExtensions =
            new String[] { "jpg", "png", "jpe", "gif", "jpeg", "bmp", "webp", "mp4", "mkv", "webm", "avi" };
    public static String[] imagesExtensions =
            new String[] { "jpg", "png", "jpe", "jpeg", "bmp", "webp" };
    public static String[] videoExtensions = new String[] { "mp4", "mkv", "webm", "avi" };
    public static String[] gifsExtensions = new String[] { "gif"} ;

    private String[] okFileExtensions = allExtensions;

    public ImageFileFilter(int filter) {
        switch (filter) {
            case FILTER_IMAGES:
                okFileExtensions = imagesExtensions;
                break;
            case FILTER_VIDEO:
                okFileExtensions = videoExtensions;
                break;
            case FILTER_GIFS:
                okFileExtensions = gifsExtensions;
                break;
            case FILTER_ALL:
                 default:
                okFileExtensions = allExtensions;
                break;
        }
    }

    public ImageFileFilter() {
        okFileExtensions = allExtensions;
    }

    @Override
    public boolean accept(File dir, String filename) {
        for (String extension : okFileExtensions)
            if (filename.endsWith(extension))
                return true;
        return false;
    }
}