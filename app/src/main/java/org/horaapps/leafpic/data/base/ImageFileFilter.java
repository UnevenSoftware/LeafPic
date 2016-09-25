package org.horaapps.leafpic.data.base;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by dnld on 24/04/16.
 */
public class ImageFileFilter implements FilenameFilter {

    private HashSet<String> extensions;
    private static String[] imagesExtensions =
            new String[] { "jpg", "png", "jpe", "jpeg", "bmp", "webp" };
    private static String[] videoExtensions = new String[] { "mp4", "mkv", "webm", "avi" };
    private static String[] gifsExtensions = new String[] { "gif"} ;

    private ImageFileFilter(FilterMode filter) {
        extensions = new HashSet<String>();
        switch (filter) {
            case IMAGES: extensions.addAll(Arrays.asList(imagesExtensions)); break;
            case VIDEO: extensions.addAll(Arrays.asList(videoExtensions)); break;
            case GIF: extensions.addAll(Arrays.asList(gifsExtensions)); break;
            case NO_VIDEO:
                extensions.addAll(Arrays.asList(imagesExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            case ALL: default:
                extensions.addAll(Arrays.asList(imagesExtensions));
                extensions.addAll(Arrays.asList(videoExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
        }
    }

    public ImageFileFilter(boolean includeVideo) {
        this(includeVideo ? FilterMode.ALL : FilterMode.NO_VIDEO);
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