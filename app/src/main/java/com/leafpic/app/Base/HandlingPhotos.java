package com.leafpic.app.Base;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 1/4/16.
 */

public class HandlingPhotos implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<HandlingPhotos> CREATOR = new Parcelable.Creator<HandlingPhotos>() {
        @Override
        public HandlingPhotos createFromParcel(Parcel in) {
            return new HandlingPhotos(in);
        }

        @Override
        public HandlingPhotos[] newArray(int size) {
            return new HandlingPhotos[size];
        }
    };
    public String FolderPath;
    public String ID;
    public String DisplayName;
    public ArrayList<Photo> photos;
    public ArrayList<Photo> selectedPhotos;
    public boolean hidden;
    Context context;
    MadiaStoreHandler as;
    private int current;
    private int last_position_selecte = -1;


    public HandlingPhotos(Context ctx, Album album) {
        context = ctx;
        as = new MadiaStoreHandler(context);
        FolderPath = album.Path;
        hidden = album.isHidden();

        selectedPhotos = new ArrayList<Photo>();
        DisplayName = album.DisplayName;
        if (!hidden) {
            ID = album.ID;
            photos = as.getAlbumPhotos(album);
        } else {
            ID = album.Path;
            HiddenPhotosHandler db = new HiddenPhotosHandler(context);
            photos = db.getPhotosByAlbum(album.Path);
        }

    }

    /***
     * parcellable
     **/

    protected HandlingPhotos(Parcel in) {
        FolderPath = in.readString();
        if (in.readByte() == 0x01) {
            photos = new ArrayList<Photo>();
            in.readList(photos, Photo.class.getClassLoader());
        } else {
            photos = null;
        }
        if (in.readByte() == 0x01) {
            selectedPhotos = new ArrayList<Photo>();
            in.readList(selectedPhotos, Photo.class.getClassLoader());
        } else {
            selectedPhotos = null;
        }
        DisplayName = in.readString();
        current = in.readInt();
        hidden = in.readByte() != 0x00;
    }

    public void setSelectedPhotoAsPreview() {
        if (selectedPhotos.size() > 0) {
            CustomAlbumsHandler h = new CustomAlbumsHandler(context);
            h.setAlbumPhotPreview(ID, selectedPhotos.get(0).Path);
        }
    }

    public void clearSelectedPhotos() {
        for (Photo photo : photos) {
            photo.setSelected(false);
        }
        selectedPhotos.clear();
    }

    public int getSelectedCount() {
        return selectedPhotos.size();
    }

    public Photo getCurrentPhoto() {
        return photos.get(getCurrentPhotoIndex());
    }

    public void setCurrentPhoto(String path) {
        setCurrentPhotoIndex(getPhotoIndex(path));
    }

    public String getPreviewAlbumImg(){
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        String s = h.getPhotPrevieAlbum(ID);
        if (s != null) return s;
        return photos.get(0).Path;
    }

    public void setContext(Context ctx) {
        context = ctx;
    }

    int getPhotoIndex(String path) {
        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).Path.equals(path)) return i;
        }
        return -1;
    }

    public int getCurrentPhotoIndex() {
        return current;
    }

    public void setCurrentPhotoIndex(int n) {
        current = n;
    }

    public Photo getPhoto(String path) {
        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).Path.equals(path)) {
                last_position_selecte = i;
                return photos.get(i);
            }
        }

        return null;
    }

    public int selectPhoto(String path, boolean val) {
        Photo x = getPhoto(path);
        if (x != null) {
            x.setSelected(val);
            if (val) selectedPhotos.add(x);
            else selectedPhotos.remove(x);
        }
        return last_position_selecte;
    }

    public void deleteSelectedPhotos() {
        for (Photo photo : selectedPhotos)
            deletePhoto(photo);

        clearSelectedPhotos();
    }

    public void deleteCurrentPhoto() {
        deletePhoto(getCurrentPhoto());
    }

    public void deletePhoto(Photo a) {
        HandlingAlbums h = new HandlingAlbums(context);
        File file = new File(a.Path);
        h.deleteFolderRecursive(file);
        photos.remove(a);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FolderPath);
        if (photos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(photos);
        }
        if (selectedPhotos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(selectedPhotos);
        }
        dest.writeString(DisplayName);
        dest.writeInt(current);
        // dest.writeValue(context);
        dest.writeByte((byte) (hidden ? 0x01 : 0x00));
    }
}