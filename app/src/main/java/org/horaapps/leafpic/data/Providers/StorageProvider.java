package org.horaapps.leafpic.data.Providers;

import android.content.Context;
import android.os.Environment;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.CustomAlbumsHelper;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.base.FoldersFileFilter;
import org.horaapps.leafpic.data.base.ImageFileFilter;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;

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
    private CustomAlbumsHelper customAlbumsHelper;
    private PreferenceUtil SP;

    public StorageProvider(Context context) {
        SP = PreferenceUtil.getInstance(context);
        customAlbumsHelper = CustomAlbumsHelper.getInstance(context);
        roots = getRoots(context);
        excludedFolders = getExcludedFolders(context);
    }

    public ArrayList<Album> getAlbums(Context context, boolean hidden) {
        ArrayList<Album> list = new ArrayList<Album>();
        includeVideo = SP.getBoolean("set_include_video", false);
        if (hidden)
            for (File storage : roots)
                fetchRecursivelyHiddenFolder(context, storage, list);
        else
            for (File storage : roots)
                fetchRecursivelyFolder(context, storage, list);
        return list;
    }

    private ArrayList<File> getExcludedFolders(Context context) {
        ArrayList<File>  list = new ArrayList<File>();
        //forced excluded folder
        list.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android"));

        CustomAlbumsHelper handler = CustomAlbumsHelper.getInstance(context);
        list.addAll(handler.getExcludedFolders());
        return list;
    }

    private void fetchRecursivelyHiddenFolder(Context context, File dir, ArrayList<Album> albumArrayList) {
        if (!excludedFolders.contains(dir)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            if (folders != null) {
                for (File temp : folders) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!excludedFolders.contains(temp) && (nomedia.exists() || temp.isHidden()))
                        checkAndAddFolder(context, temp, albumArrayList);

                    fetchRecursivelyHiddenFolder(context, temp, albumArrayList);
                }
            }
        }
    }

    private void fetchRecursivelyFolder(Context context, File dir, ArrayList<Album> albumArrayList) {
        if (!excludedFolders.contains(dir)) {
            checkAndAddFolder(context, dir, albumArrayList);
            File[] children = dir.listFiles(new FoldersFileFilter());
            if (children != null) {
                for (File temp : children) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!excludedFolders.contains(temp) && !temp.isHidden() && !nomedia.exists()) {
                        //not excluded/hidden folder
                        fetchRecursivelyFolder(context, temp, albumArrayList);
                    }
                }
            }
        }
    }

    private void checkAndAddFolder(Context context, File dir, ArrayList<Album> albumArrayList) {
        File[] files = dir.listFiles(new ImageFileFilter(includeVideo));
        if (files != null && files.length > 0) {
            //valid folder
            Album asd = new Album(context, dir.getAbsolutePath(), -1, dir.getName(), files.length);
            asd.setCoverPath(customAlbumsHelper.getCoverPathAlbum(asd.getPath(), asd.getId()));

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


    public static ArrayList<Media> getMedia(String path, boolean includeVideo) {
        ArrayList<Media> list = new ArrayList<Media>();
        File[] images = new File(path).listFiles(new ImageFileFilter(includeVideo));
        for (File image : images)
            list.add(new Media(image));
        return list;
    }

}
