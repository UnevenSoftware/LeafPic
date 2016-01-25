package com.leafpic.app;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.leafpic.app.utils.string;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 1/4/16.
 */

class HandlingPhotos implements Parcelable {

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
    public ArrayList<Photo> photos;
    public ArrayList<Photo> selectedPhotos;
    public String DisplayName;
    Context context;
    boolean hidden;
    private int current;
    private int last_position_selecte = -1;



    public HandlingPhotos(Context ctx, String folderPath, boolean hide) {
        context = ctx;
        DatabaseHandler db = new DatabaseHandler(context);
        FolderPath = folderPath;
        hidden = hide;
        photos = db.getPhotosByAlbum(FolderPath);
        selectedPhotos = new ArrayList<Photo>();
        DisplayName = string.getBucketNamebyBucketPath(folderPath);
        db.close();
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

    public String getPreviewAlbumImg(){
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

    public int selectPhoto(Photo a, boolean val) {
        Photo x = photos.get(photos.indexOf(a));
        x.setSelected(val);
        if (val) selectedPhotos.add(x);
        else selectedPhotos.remove(x);
        return last_position_selecte;
    }

    public void deleteSelectedPhotos() {
        for (Photo photo : selectedPhotos)
            deletePhoto(photo);

        clearSelectedPhotos();
    }

    public Photo getCurrentPhoto() {
        return photos.get(getCurrentPhotoIndex());
    }

    public void setCurrentPhoto(String path) {
        setCurrentPhotoIndex(getPhotoIndex(path));
    }

    public void deleteCurrentPhoto() {
        deletePhoto(getCurrentPhoto());
    }

    public void deletePhoto(Photo a) {
        HandlingAlbums h = new HandlingAlbums(context);
        DatabaseHandler db = new DatabaseHandler(context);
        db.deletePhoto(a);
        db.close();
        File file = new File(a.Path);
        h.deleteFolderRecursive(file);
        photos.remove(a);
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