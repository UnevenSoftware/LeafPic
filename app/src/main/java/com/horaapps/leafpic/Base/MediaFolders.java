package com.horaapps.leafpic.Base;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 24/04/16.
 */
public class MediaFolders {

    public static final String TAG = "MediaFolders";

    ArrayList<File> excludedfolders;
    ArrayList<newAlbum> includedFolders;
    AlbumsComapartors albumsComapartors;

    public MediaFolders(){
        excludedfolders = new ArrayList<File>();
        includedFolders = new ArrayList<newAlbum>();
        albumsComapartors = new AlbumsComapartors(true);

        excludedfolders.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android"));
        excludedfolders.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_MUSIC));
    }

    public void getMediaAlbums() {
        getAlbums(Environment.getExternalStorageDirectory());

        Collections.sort(includedFolders, albumsComapartors.getNameComapartor());
        for (newAlbum includedFolder : includedFolders) {
            Log.wtf(TAG,includedFolder.getPath());
            includedFolder.loadLastPhoto();

            try {
                Log.d(TAG, "getMediaAlbums: " + includedFolder.media.get(0).getThumnail());
            } catch (IOException e) {
                //e.printStackTrace();
            }
            Log.w(TAG,"count: "+includedFolder.getCount());
        }

    }

   public void getAlbums(File dir) {
        //checkFolderValidity(dir);
        if (!excludedfolders.contains(dir)) {
           File[] children = dir.listFiles(new FoldersFileFilter());
           for (File temp : children) {
               if (!temp.isHidden()) {
                   File nomedia = new File(temp, ".nomedia");
                   if (!nomedia.exists()) {
                       checkFolderValidity(temp);
                       getAlbums(temp);
                   }
               }
           }
       }
   }
    public void getHiddenAlbums(File dir) {
        //checkFolderValidity(dir);
        if (!excludedfolders.contains(dir)) {
            File[] children = dir.listFiles(new FoldersFileFilter());
            for (File temp : children) {
                File nomedia = new File(temp, ".nomedia");
                if (nomedia.exists()) {
                    checkFolderValidity(temp);
                    getAlbums(temp);
                }
            }
        }
    }

    public void checkFolderValidity(File dir){
        String[] medias = dir.list(new ImageFileFilter());
        if (medias.length > 0)
            includedFolders.add(new newAlbum(dir.getAbsolutePath(), dir.getName()));
        //Log.wtf(TAG, "valid: " + dir.getAbsolutePath());
    }

    public static File lastFileModified(File fl) {
        File[] files = fl.listFiles(new ImageFileFilter());
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }

    public void getImages(File dir){
        String[] children = dir.list(new ImageFileFilter());
        for (String child : children) {
            Log.wtf("file:", child);
        }
    }

}
