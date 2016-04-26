package com.horaapps.leafpic.Base;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Created by dnld on 24/04/16.
 */
public class ImageFileFilter implements FilenameFilter
{
    private final String[] okFileExtensions =
            new String[] { "jpg", "png", "gif", "jpeg", "bmp", "mp4" };

    @Override
    public boolean accept(File dir, String filename) {
        for (String extension : okFileExtensions)
            if (filename.endsWith(extension))
                return true;
        return false;
    }
}