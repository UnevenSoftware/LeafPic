package com.horaapps.leafpic.Base.Providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.CustomAlbumsHandler;
import com.horaapps.leafpic.Base.FoldersFileFilter;
import com.horaapps.leafpic.Base.ImageFileFilter;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.utils.ContentHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by weconstudio on 07/07/16.
 */
public class AlbumsProvider {

    private HashSet<File> roots;
    private ArrayList<File> excludedfolders;
    private boolean includeVideo = true;
    private CustomAlbumsHandler customAlbumsHandler;

    private SharedPreferences SP;

    public AlbumsProvider(Context context) {
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        customAlbumsHandler = new CustomAlbumsHandler(context);
        roots = getRoots(context);
        excludedfolders = getExcludedFolders(context);
    }

    public ArrayList<Album> getAlbums(boolean hidden) {
        ArrayList<Album> list = new ArrayList<Album>();
        includeVideo = SP.getBoolean("set_include_video", false);
        if (hidden)
            for (File storage : roots)
                fetchRecursivelyHiddenFolder(storage, list, storage.getAbsolutePath());
        else
            for (File storage : roots)
                fetchRecursivelyFolder(storage, list, storage.getAbsolutePath());
        return list;
    }

    private ArrayList<File> getExcludedFolders(Context context) {
        ArrayList<File>  list = new ArrayList<File>();
        //forced excluded folder
        list.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android"));

        CustomAlbumsHandler handler = new CustomAlbumsHandler(context);
        list.addAll(handler.getExcludedFolderFiles());
        return list;
    }

    private void fetchRecursivelyHiddenFolder(File dir, ArrayList<Album> albumArrayList, String rootExternalStorage) {
        if (!excludedfolders.contains(dir)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            if (folders != null) {
                for (File temp : folders) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!excludedfolders.contains(temp) && (nomedia.exists() || temp.isHidden()))
                        checkAndAddAlbum(temp, albumArrayList, rootExternalStorage);

                    fetchRecursivelyHiddenFolder(temp, albumArrayList, rootExternalStorage);
                }
            }
        }
    }

    private void fetchRecursivelyFolder(File dir, ArrayList<Album> albumArrayList, String rootExternalStorage) {
        if (!excludedfolders.contains(dir)) {
            checkAndAddAlbum(dir, albumArrayList, rootExternalStorage);
            File[] children = dir.listFiles(new FoldersFileFilter());
            if (children != null) {
                for (File temp : children) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!excludedfolders.contains(temp) && !temp.isHidden() && !nomedia.exists()) {
                        //not excluded/hidden folder
                        fetchRecursivelyFolder(temp, albumArrayList, rootExternalStorage);
                    }
                }
            }
        }
    }


    private void checkAndAddAlbum(File temp, ArrayList<Album> albumArrayList, String rootExternalStorage) {
        File[] files = temp.listFiles(new ImageFileFilter(includeVideo));
        if (files != null && files.length > 0) {
            //valid folder
            Album asd = new Album(temp.getAbsolutePath(), temp.getName(), files.length, rootExternalStorage);
            asd.setCoverPath(customAlbumsHandler.getCoverPathAlbum(asd.getPath()));

            long lastMod = Long.MIN_VALUE;
            File choice = null;
            for (File file : files) {
                if (file.lastModified() > lastMod) {
                    choice = file;
                    lastMod = file.lastModified();
                }
            }
            if (choice != null)
                asd.media.add(0, new Media(choice.getAbsolutePath(), choice.lastModified()));

            albumArrayList.add(asd);
        }
    }

    private HashSet<File> getRoots(Context context) {
        HashSet<File> roots = new HashSet<File>();
        roots.add(Environment.getExternalStorageDirectory());

        String[] extSdCardPaths = ContentHelper.getExtSdCardPaths(context);
        for (String extSdCardPath : extSdCardPaths) {
            File mas = new File(extSdCardPath);
            if (mas.canRead())
                roots.add(mas);
        }

        String sdCard = System.getenv("SECONDARY_STORAGE");
        if (sdCard != null) roots.add(new File(sdCard));
        return roots;
    }


    public static ArrayList<Media> getAlbumPhotos(String path, int filter, boolean includeVideo) {
        ArrayList<Media> list = new ArrayList<Media>();
        File[] images = new File(path).listFiles(new ImageFileFilter(filter, includeVideo));
        for (File image : images)
            list.add(new Media(image));
        return list;
    }

}
