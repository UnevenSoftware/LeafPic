package com.horaapps.leafpic.Base;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by dnld on 24/04/16.
 */
public class ImageFileFilter implements FilenameFilter
{

    public final static int FILTER_ALL = 0;
    public final static int FILTER_IMAGES = 1;
    public final static int FILTER_GIFS = 2;
    public final static int FILTER_VIDEO = 3;
    public final static int FILTER_NO_VIDEO = 4;

    private HashSet<String> extensions;
    private static String[] imagesExtensions =
            new String[] { "jpg", "png", "jpe", "jpeg", "bmp", "webp" };
    private static String[] videoExtensions = new String[] { "mp4", "mkv", "webm", "avi" };
    private static String[] gifsExtensions = new String[] { "gif"} ;

    //private String[] okFileExtensions = allExtensions;

    private ImageFileFilter(int filter) {
        extensions = new HashSet<String>();
        switch (filter) {
            case FILTER_IMAGES:
                extensions.addAll(Arrays.asList(imagesExtensions));
                break;
            case FILTER_VIDEO:
                extensions.addAll(Arrays.asList(videoExtensions));
                break;
            case FILTER_GIFS:
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            case FILTER_NO_VIDEO:
                extensions.addAll(Arrays.asList(imagesExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            case FILTER_ALL:
                 default:
                    extensions.addAll(Arrays.asList(imagesExtensions));
                    extensions.addAll(Arrays.asList(videoExtensions));
                    extensions.addAll(Arrays.asList(gifsExtensions));
                    break;
        }
    }

    public ImageFileFilter(int filter, boolean includeVideo) {
        extensions = new HashSet<String>();
        switch (filter) {
            case FILTER_IMAGES:
                extensions.addAll(Arrays.asList(imagesExtensions));
                break;
            case FILTER_VIDEO:
                extensions.addAll(Arrays.asList(videoExtensions));
                break;
            case FILTER_GIFS:
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            case FILTER_NO_VIDEO:
                extensions.addAll(Arrays.asList(imagesExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            case FILTER_ALL:
            default:
                extensions.addAll(Arrays.asList(imagesExtensions));
                if (includeVideo)
                extensions.addAll(Arrays.asList(videoExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
        }
    }

    public ImageFileFilter(boolean includeVideo) {
        this(includeVideo ? FILTER_ALL : FILTER_NO_VIDEO);
    }

    public ImageFileFilter() {
       this(FILTER_ALL);
    }

    @Override
    public boolean accept(File dir, String filename) {
        if (new File(dir, filename).isFile()) {
            for (String extension : extensions)
                if (filename.toLowerCase().endsWith(extension))
                    return true;
        }

        return false;
    }
}