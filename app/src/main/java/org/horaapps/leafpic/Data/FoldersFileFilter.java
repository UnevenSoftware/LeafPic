package org.horaapps.leafpic.Data;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by dnld on 24/04/16.
 */
public class FoldersFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
}