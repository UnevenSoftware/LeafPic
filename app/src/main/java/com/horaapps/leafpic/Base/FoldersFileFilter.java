package com.horaapps.leafpic.Base;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by dnld on 24/04/16.
 */
public class FoldersFileFilter implements FileFilter {
    public boolean accept(File file)
    {
        return file.isDirectory();
    }
}