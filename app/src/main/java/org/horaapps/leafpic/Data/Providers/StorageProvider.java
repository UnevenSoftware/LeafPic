package org.horaapps.leafpic.Data.Providers;

import android.content.Context;
import android.os.Environment;

import org.horaapps.leafpic.Data.Album;
import org.horaapps.leafpic.Data.CustomAlbumsHandler;
import org.horaapps.leafpic.Data.FoldersFileFilter;
import org.horaapps.leafpic.Data.ImageFileFilter;
import org.horaapps.leafpic.Data.Media;
import org.horaapps.leafpic.utils.ContentHelper;
import org.horaapps.leafpic.utils.PreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by weconstudio on 07/07/16.
 */
public class StorageProvider {

    private HashSet<File> roots;
    private ArrayList<File> excludedFolders;
    private boolean includeVideo = true;
    private CustomAlbumsHandler customAlbumsHandler;

    private PreferenceUtil SP;

    public StorageProvider(Context context) {
        SP = PreferenceUtil.getInstance(context);
        customAlbumsHandler = new CustomAlbumsHandler(context);
        roots = getRoots(context);
        excludedFolders = getExcludedFolders(context);
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
        list.addAll(handler.getExcludedFolders(false));
        return list;
    }

    private void fetchRecursivelyHiddenFolder(File dir, ArrayList<Album> albumArrayList, String rootExternalStorage) {
        if (!excludedFolders.contains(dir)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            if (folders != null) {
                for (File temp : folders) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!excludedFolders.contains(temp) && (nomedia.exists() || temp.isHidden()))
                        checkAndAddFolder(temp, albumArrayList, rootExternalStorage);

                    fetchRecursivelyHiddenFolder(temp, albumArrayList, rootExternalStorage);
                }
            }
        }
    }

    private void fetchRecursivelyFolder(File dir, ArrayList<Album> albumArrayList, String rootExternalStorage) {
        if (!excludedFolders.contains(dir)) {
            checkAndAddFolder(dir, albumArrayList, rootExternalStorage);
            File[] children = dir.listFiles(new FoldersFileFilter());
            if (children != null) {
                for (File temp : children) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!excludedFolders.contains(temp) && !temp.isHidden() && !nomedia.exists()) {
                        //not excluded/hidden folder
                        fetchRecursivelyFolder(temp, albumArrayList, rootExternalStorage);
                    }
                }
            }
        }
    }

    private void checkAndAddFolder(File dir, ArrayList<Album> albumArrayList, String rootExternalStorage) {
        File[] files = dir.listFiles(new ImageFileFilter(includeVideo));
        if (files != null && files.length > 0) {
            //valid folder
            Album asd = new Album(dir.getAbsolutePath(), dir.getName(), files.length, rootExternalStorage);
            asd.setCoverPath(customAlbumsHandler.getCoverPathAlbum(asd.getPath(), asd.getId()));

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


    public static ArrayList<Media> getMedia(String path, int filter, boolean includeVideo) {
        ArrayList<Media> list = new ArrayList<Media>();
        File[] images = new File(path).listFiles(new ImageFileFilter(filter, includeVideo));
        for (File image : images)
            list.add(new Media(image));
        return list;
    }

}
