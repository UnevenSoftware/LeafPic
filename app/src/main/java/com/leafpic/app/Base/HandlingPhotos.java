package com.leafpic.app.Base;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.leafpic.app.utils.StringUtils;

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
    public AlbumSettings settings;
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
            setSettings();
            photos = as.getAlbumPhotos(album, getSortingMode());
        } else {
            ID = album.Path;
            setSettings();
            HiddenPhotosHandler db = new HiddenPhotosHandler(context);
            photos = db.getPhotosByAlbum(album.Path);
        }

    }

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

    public void sort() {

        if (!hidden) {
            Album a = new Album();
            a.ID = ID;
            photos = as.getAlbumPhotos(a, getSortingMode());
        } else {
            HiddenPhotosHandler db = new HiddenPhotosHandler(context);
            photos = db.getPhotosByAlbum(ID);
        }
    }

    public void setDefaultSortingMode(String column) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.setAlbumSortingMode(ID, column);
        settings.columnSortingMode = column;
    }

    public void setDefaultSortingAscending(Boolean ascending) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.setAlbumSortingAscending(ID, ascending);
        settings.ascending = ascending;
    }

    public String getSortingMode() {
        if (settings.columnSortingMode != null) return settings.getSQLSortingMode();
        else return MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
    }

    public String getPreviewAlbumImg() {
        Log.wtf("asdfsfd", settings.coverPath);
        if (settings.coverPath != null) return settings.coverPath;
        return photos.get(0).Path;
    }

    public void setSelectedPhotoAsPreview() {
        if (selectedPhotos.size() > 0) {
            CustomAlbumsHandler h = new CustomAlbumsHandler(context);
            h.setAlbumPhotPreview(ID, selectedPhotos.get(0).Path);
            settings.coverPath = selectedPhotos.get(0).Path;
        }
    }

    public void setSettings() {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        settings = h.getSettings(ID);
    }

    public void setContext(Context ctx) {
        context = ctx;
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

    public void renamePhoto(String olderPath, String name){
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getAlbumPathRenamed(olderPath, name));
            String s[] = from.list(), dirPath = from.getAbsolutePath();
            for (String paht : s) scanFile(new String[]{dirPath + "/" + paht});

            from.renameTo(to);
            s = to.list();
            dirPath = to.getAbsolutePath();
            for (String paht : s) scanFile(new String[]{dirPath + "/" + paht});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scanFile(String[] path) {
        MediaScannerConnection.scanFile(context, path, null, new MediaScannerConnection.OnScanCompletedListener() {

            @Override
            public void onScanCompleted(String path, Uri uri) {
                System.out.println("SCAN COMPLETED: " + path);
            }
        });
    }

}