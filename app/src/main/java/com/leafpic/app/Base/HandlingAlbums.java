package com.leafpic.app.Base;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import com.leafpic.app.utils.string;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class HandlingAlbums {
    public ArrayList<Album> dispAlbums;
    public int last_position_selecte = -1;
    private Context context;
    private ArrayList<Album> selectedAlbums;

    public HandlingAlbums(Context ctx) {
        context = ctx;
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    public void loadPreviewAlbums() {
        MadiaStoreHandler as = new MadiaStoreHandler(context);
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        dispAlbums = as.getMediaStoreAlbums();

        for (Album dispAlbum : dispAlbums) {
            dispAlbum.setCoverPath(h.getPhotPrevieAlbum(dispAlbum.ID));
            dispAlbum.photos = as.getFirstAlbumPhoto(dispAlbum);
        }

    }

    public int selectAlbum(String a, boolean val) {
        Album x = getAlbum(a);
        if (x != null) {
            x.setSelcted(val);
            if (val) selectedAlbums.add(x);
            else selectedAlbums.remove(x);
        }
        return last_position_selecte;
    }

    public int selectAlbum(Album x, boolean val) {
        if (x != null) {
            x.setSelcted(val);
            if (val) selectedAlbums.add(x);
            else selectedAlbums.remove(x);
        }
        return last_position_selecte;
    }

    public int getSelectedCount() {
        return selectedAlbums.size();
    }

    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.setSelcted(false);
        }
        selectedAlbums.clear();
    }

    public Album getAlbum(String p) {
        for (int i = 0; i < dispAlbums.size(); i++) {
            if (dispAlbums.get(i).Path.equals(p)) {
                last_position_selecte = i;
                return dispAlbums.get(i);
            }
        }
        return null;
    }

    public void deleteSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            deleteAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void deleteAlbum(Album a) {
        deleteAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void deleteAlbum(String path) {
        File dir = new File(path);
        deleteFolderRecursive(dir);
    }

    public void deleteFolderRecursive(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                File temp = new File(dir, child);
                if (temp.isDirectory())
                    deleteFolderRecursive(temp);
                else {
                    temp.delete();
                    scanFile(new String[]{temp.getAbsolutePath()});
                }
            }
        }
        dir.delete();
        scanFile(new String[]{dir.getAbsolutePath()});
    }

    public void scanFile(String[] path) {
        MediaScannerConnection.scanFile(context, path, null, new MediaScannerConnection.OnScanCompletedListener() {

            @Override
            public void onScanCompleted(String path, Uri uri) {
                System.out.println("SCAN COMPLETED: " + path);

            }
        });
    }

    public void hideSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            hideAlbum(selectedAlbum);
        clearSelectedAlbums();
    }

    public void hideAlbum(final Album a) {
        hideAlbum(a.Path);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                HiddenPhotosHandler db = new HiddenPhotosHandler(context);
                MadiaStoreHandler mediaStoreHandler = new MadiaStoreHandler(context);
                for (Photo photo : mediaStoreHandler.getAlbumPhotos(a)) {
                    photo.FolderPath = a.Path;
                    db.addPhoto(photo);
                }
                db.close();
            }
        });
        t.start();

        dispAlbums.remove(a);
    }

    public void hideAlbum(String path) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (!file.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.flush();
                out.close();
                scanFile(new String[]{file.getAbsolutePath()});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hideAlbum(String path, final ArrayList<Photo> ph) {
        final File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (!file.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.flush();
                out.close();
                scanFile(new String[]{file.getAbsolutePath()});

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HiddenPhotosHandler db = new HiddenPhotosHandler(context);

                        for (Photo photo : ph) {
                            photo.FolderPath = dirName.getAbsolutePath();
                            db.addPhoto(photo);
                        }
                        db.close();
                    }
                });
                t.start();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void excludeSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            excludeAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void excludeAlbum(Album a) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.excludeAlbum(a.ID);
        dispAlbums.remove(a);
    }

    public boolean loadPreviewHiddenAlbums() {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        HiddenPhotosHandler db = new HiddenPhotosHandler(context);
        if (db.getPhotosCount() == 0)
            db.loadHiddenALbums();

        dispAlbums = db.getAlbums();
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.setCoverPath(h.getPhotPrevieAlbum(dispAlbum.Path));
            dispAlbum.photos = db.getFirstPhotosByAlbum(dispAlbum.Path);
        }
        db.close();
        return true;
    }

    public void unHideSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            unHideAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void unHideAlbum(Album a) {
        unHideAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void unHideAlbum(String path) {

        HiddenPhotosHandler db = new HiddenPhotosHandler(context);
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (file.exists()) {
            try {
                file.delete();
                scanFile(new String[]{file.getAbsolutePath()});
                db.deleteAlbum(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dispAlbums.remove(getAlbum(path));
        db.close();
    }

    public void LogAlbums() {
        for (Album dispAlbum : dispAlbums) {
            Log.wtf("asdasd", dispAlbum.Path);
            for (Photo photo : dispAlbum.photos) {
                Log.d("asdasdasdasd", photo.Path);
            }
        }
    }

    /*************
     * This Metods doesnt work for the moment
     **************/

    public void renameAlbum(String olderPath, String name) {
        try {
            File from = new File(olderPath);
            File to = new File(string.getAlbumPathRenamed(olderPath, name));
            from.renameTo(to);
            scanFile(new String[]{from.getAbsolutePath(), to.getAbsolutePath()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}