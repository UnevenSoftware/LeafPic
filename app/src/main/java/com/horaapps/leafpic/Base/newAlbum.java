package com.horaapps.leafpic.Base;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 26/04/16.
 */
public class newAlbum {
    String name = null;
    String path = null;
    int count = -1;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        File folder = new File(getPath());
        return folder.list(new ImageFileFilter()).length;
    }

    public ArrayList<newMedia> media = new ArrayList<newMedia>();

    boolean selected = false;
    int current = -1;

    public newAlbum(String path, String name, int count) {
        this.path = path;
        this.name = name;
        this.count = count;
    }

    public newAlbum(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public void loadLastPhoto() {
        File last = MediaFolders.lastFileModified(new File(path));
        media = new ArrayList<newMedia>();
        media.add(new newMedia(last.getAbsolutePath(), last.lastModified()));
    }
}
