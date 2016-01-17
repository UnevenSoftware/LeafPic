package com.leafpic.app;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 1/4/16.
 */
class HandlingPhotos {
    public String FolderPath;
    public ArrayList<Photo> photos;
    public ArrayList<Photo> selectedPhotos;
    Context context;
    boolean hidden;


    public HandlingPhotos(Context ctx, String folderPath, boolean hide) {
        context = ctx;
        DatabaseHandler db = new DatabaseHandler(context);
        FolderPath = folderPath;
        hidden = hide;
        photos = db.getPhotosByAlbum(FolderPath);
        selectedPhotos = new ArrayList<Photo>();
        db.close();
    }

    public Photo getPhoto(String path) {
        for (Photo photo : photos) {
            if (photo.Path.equals(path))
                return photo;
        }
        return null;
    }

    public void selectPhoto(Photo a, boolean val) {
        Photo x = photos.get(photos.indexOf(a));
        x.setSelected(val);
        if (val) selectedPhotos.add(x);
        else selectedPhotos.remove(x);
    }

    public void deleteSelectedPhotos() {
        for (Photo photo : selectedPhotos)
            deletePhoto(photo);

        clearSelectedPhotos();
    }

    public void deletePhoto(String path) {
        HandlingAlbums h = new HandlingAlbums(context);
        DatabaseHandler db = new DatabaseHandler(context);
        File file = new File(path);
        h.deleteFolderRecursive(file);
        db.deletePhotoByPath(path);
        db.close();
        photos.remove(getPhoto(path));
    }

    public void deletePhoto(Photo a) {
        HandlingAlbums h = new HandlingAlbums(context);
        DatabaseHandler db = new DatabaseHandler(context);
        File file = new File(a.Path);
        h.deleteFolderRecursive(file);
        db.deletePhotoByPath(a.Path);
        db.close();
        photos.remove(a);
    }

    public void clearSelectedPhotos() {
        for (Photo photo : photos) {
            photo.setSelected(false);
        }
        selectedPhotos.clear();
    }


}