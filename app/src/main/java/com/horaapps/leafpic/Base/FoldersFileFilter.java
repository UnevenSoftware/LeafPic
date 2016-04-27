package com.horaapps.leafpic.Base;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by dnld on 24/04/16.
 */
public class FoldersFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        /*if (pathname.isDirectory()) {
            String[] medias = pathname.list(new ImageFileFilter());
            return medias.length > 0;
        }
        return false;*/
        return pathname.isDirectory();
    }
}