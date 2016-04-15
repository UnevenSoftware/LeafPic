package com.leafpic.app.Base;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.leafpic.app.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

    public ArrayList<Media> medias;
    public ArrayList<Media> selectedMedias;
    public AlbumSettings settings;
    Context context;
    MediaStoreHandler as;
    private ArrayList<Integer> selectedPhotosIndexs;
    private int current;

    private int filter_photos = Album.FILTER_ALL;

    private String contentDescription = null;

    private Integer last_position_selecte = -1;



    public HandlingPhotos(Context ctx, Album album) {
        context = ctx;
        as = new MediaStoreHandler(context);
        ID = album.ID;
        FolderPath = album.Path;
        DisplayName = album.DisplayName;
        contentDescription = album.getContentDescdription(context);

        selectedMedias = new ArrayList<Media>();
        selectedPhotosIndexs = new ArrayList<Integer>();

        setSettings();
        updatePhotos();
    }

    public HandlingPhotos(Context ctx, String photoPath) {
        context = ctx;
        as = new MediaStoreHandler(context);

        //Album album = as.getAlbumPhoto(photoPath);
        selectedMedias = new ArrayList<Media>();
        //ID = album.ID;
        setSettings();
        updatePhotos();
    }

    public HandlingPhotos(Context ctx) {
        context = ctx;
        as = new MediaStoreHandler(context);
        selectedMedias = new ArrayList<Media>();
    }

    public void filterMedias(int filter){
        filter_photos=filter;
        updatePhotos();
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void updatePhotos() {
        medias = as.getAlbumPhotos(ID, getSortingMode(), filter_photos);
    }

    public String getSelectedPhotosSerilized() {
        String s = "";
        if (selectedMedias.size() > 0) {
            for (Media media : selectedMedias)
                s += media.Path + "ç";

            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public String getSelectedPhotosIndexSerilized() {
        String s = "";
        if (selectedPhotosIndexs.size() > 0) {
            for (Integer photo : selectedPhotosIndexs)
                s += photo + "ç";

            return s.substring(0, s.length() - 1);
        }
        return s;
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
        if (hasCustomPreview()) return settings.coverPath;
        return medias.get(0).Path;
    }

    public boolean hasCustomPreview() {
        return settings.coverPath != null;
    }

    public void setSelectedPhotoAsPreview() {
        if (selectedMedias.size() > 0) {
            CustomAlbumsHandler h = new CustomAlbumsHandler(context);
            h.setAlbumPhotPreview(ID, selectedMedias.get(0).Path);
            settings.coverPath = selectedMedias.get(0).Path;
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
        for (Media media : medias) {
            media.setSelected(false);
        }
        selectedMedias.clear();
        selectedPhotosIndexs.clear();
    }

    public int getSelectedCount() {
        return selectedMedias.size();
    }

    public Media getCurrentPhoto() {
        return medias.get(getCurrentPhotoIndex());
    }

    public void setCurrentPhoto(String path) {
        setCurrentPhotoIndex(getPhotoIndex(path));
    }

    int getPhotoIndex(String path) {
        for (int i = 0; i < medias.size(); i++) {
            if (medias.get(i).Path.equals(path)) return i;
        }
        return -1;
    }

    public int getCurrentPhotoIndex() {
        return current;
    }

    public void setCurrentPhotoIndex(int n) {
        current = n;
    }

    public Media getPhoto(String path) {
        for (int i = 0; i < medias.size(); i++)
            if (medias.get(i).Path.equals(path)) {
                last_position_selecte = i;
                return medias.get(i);
            }
        return null;
    }


    public void selectAllPhotos(){
        for (int i = 0; i < medias.size(); i++)
            if(!medias.get(i).isSelected()) {
                medias.get(i).setSelected(true);
                selectedMedias.add(medias.get(i));
                selectedPhotosIndexs.add(i);
            }
    }

    public int toggleSelectPhoto(String path) {
        Media x = getPhoto(path);
        if (x != null) {
            x.setSelected(!x.isSelected());
            if (x.isSelected()) {
                selectedMedias.add(x);
                selectedPhotosIndexs.add(last_position_selecte);
            } else {
                selectedMedias.remove(x);
                selectedPhotosIndexs.remove(last_position_selecte);
            }
        }

        return last_position_selecte;
    }

    public void deleteSelectedPhotos() {
        for (Media media : selectedMedias)
            deletePhoto(media);

        clearSelectedPhotos();
    }

    public void deleteCurrentPhoto() {
        deletePhoto(getCurrentPhoto());
    }

    public void deletePhoto(Media a) {
        //HandlingAlbums h = new HandlingAlbums(context);
        //File file = new File(a.Path);
        //h.deleteFolderRecursive(file);
        context.getContentResolver().delete(a.getUri(),null,null);
        medias.remove(a);
    }

    public void renamePhoto(String olderPath, String path){
        try {
            File from = new File(olderPath);
            File to = new File(path);
            scanFile(new String[]{from.getAbsolutePath()});
            from.renameTo(to);
            scanFile(new String[]{to.getAbsolutePath()});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void movePhoto(String olderPath, String folderPath) {
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getPhotoPathMoved(olderPath, folderPath));
            scanFile(new String[]{from.getAbsolutePath()});
            from.renameTo(to);
            scanFile(new String[]{to.getAbsolutePath()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copySelectedPhotos(String paths, String folderPath) {
        for (String path : paths.split("ç"))
            copyPhoto(path, folderPath);
    }

    public void moveSelectedPhotos(String paths, String folderPath) {
        for (String path : paths.split("ç"))
            movePhoto(path, folderPath);
    }

    public void copyPhoto(String olderPath, String folderPath) {
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getPhotoPathMoved(olderPath, folderPath));

            InputStream in = new FileInputStream(from);
            OutputStream out = new FileOutputStream(to);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close();
            out.close();

            scanFile(new String[]{to.getAbsolutePath()});
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FolderPath);
        if (medias == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(medias);
        }
        if (selectedMedias == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(selectedMedias);
        }
        dest.writeString(DisplayName);
        dest.writeInt(current);
    }

    protected HandlingPhotos(Parcel in) {
        FolderPath = in.readString();
        if (in.readByte() == 0x01) {
            medias = new ArrayList<Media>();
            in.readList(medias, Media.class.getClassLoader());
        } else {
            medias = null;
        }
        if (in.readByte() == 0x01) {
            selectedMedias = new ArrayList<Media>();
            in.readList(selectedMedias, Media.class.getClassLoader());
        } else {
            selectedMedias = null;
        }
        DisplayName = in.readString();
        current = in.readInt();
    }
}