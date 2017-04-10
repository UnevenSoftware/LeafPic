package org.horaapps.leafpic.data.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by dnld on 24/04/16.
 */
public class NotHiddenFoldersFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory() && !file.isHidden() && !new File(file, ".nomedia").exists();
    }
}