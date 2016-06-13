package com.horaapps.leafpic.Base;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.horaapps.leafpic.Adapters.PhotosAdapter;
import com.horaapps.leafpic.MyApplication;
import com.horaapps.leafpic.R;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 26/04/16.
 */
public class Album {

    String name = null;
    String path = null;
    boolean selected = false;
    
    public AlbumSettings settings = new AlbumSettings();
    
    AlbumMedia albumMedia;
    

    public Album() {
        Album(null, null);
    }

    public Album(String path, String name) {
        Album(path,name,-1);
    }

    public Album(String path, String name, int count) {
        albumMedia = new AlbumMedia();
        this.path = path;
        this.name = name;
    }

    public Album(Context context, String mediaPath) {
        File folder = new File(mediaPath).getParentFile();
        albumMedia = new AlbumMedia();
        this.path = folder.getPath();
        this.name = folder.getName();
        updatePhotos(context);
        setCurrentPhoto(mediaPath);
    }


    public void updatePhotos(Context context) {
        albumMedia.updatePhotos(context);
    }

    public boolean areFiltersActive() {//UNUSED FUNCTION
        return albumMedia.areFiltersActive();
    }

    public void filterMedias(Context context, int filter) {
        albumMedia.filterMedias(context,filter);
    }

    public void setSettings(Context context) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        settings = h.getSettings(getPath());
    }

    public boolean hasCustomCover() {
        return settings.coverPath != null;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Media getMedia(int index) { return albumMedia.getMedia(index); }

    public void setCurrentPhotoIndex(int index){ albumMedia.setCurrentPhotoIndex(index); }

    public Media getCurrentMedia() { return albumMedia.getCurrentMedia(); }

    public int getCurrentMediaIndex() { return albumMedia.getCurrentMediaIndex(); }

    public String getContentDescription(Context c) {
        return c.getString(R.string.media);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setCount(int count) {
        // this.count = count;
        // This should not be used
    }

    public int getCount() {
        return albumMedia.getMediaSize();
    }

    public void setCoverPath(String path) {
        settings.coverPath = path;
    }

    public boolean isHidden() {
        return new File(getPath(), ".nomedia").exists();
    }

    public Media getCoverAlbum() {
        if (hasCustomCover())
            return new Media(settings.coverPath);
        else return albumMedia.getCoverAlbum();
    }

    public void setSelectedPhotoAsPreview(Context context) {
        if(albumMedia.hasMediaSelected()){
            albumMedia.setSelectedPhotoAsPreview(context);
            settings.coverPath = albumMedia.getFirstSelectedMediaPath();
        }
    }

    public void setCurrentPhoto(String path) {
        albumMedia.setCurrentPhoto(path);
    }

    public int getSelectedCount() {
        return albumMedia.getSelectedMediaSize();
    }

    public void selectAllPhotos() {
        albumMedia.selectAllPhotos();
    }

    public int toggleSelectPhoto(int index) {
        return albumMedia.toggleSelectPhoto(index);
    }

    public void setDefaultSortingMode(Context context, int column) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.setAlbumSortingMode(getPath(), column);
        settings.columnSortingMode = column;
    }

    public void renameCurrentMedia(Context context, String newName) {
        albumMedia.renameCurrentMedia(context, newName);
    }

    public int moveCurrentPhoto(Context context, String newName) {
        // move shouldnt return the new photo index
        return moveCurrentPhoto(context, newName);
    }

    public void setDefaultSortingAscending(Context context, Boolean ascending) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.setAlbumSortingAscending(getPath(), ascending);
        settings.ascending = ascending;
    }


    /**
     * On longpress, it finds the last or the first selected image before or after the targetIndex
     * and selects them all.
     *
     * @param targetIndex
     * @param adapter
     */
    public void selectAllPhotosUpTo(int targetIndex, PhotosAdapter adapter) {
        albumMedia.selectAllPhotosUpTo(targetIndex, adapter);
    }

    public void clearSelectedPhotos() {
        albumMedia.clearSelectedPhotos();
    }

    public void sortPhotos() {
        mediaComparators = new MediaComparators(settings.ascending);
        switch (settings.columnSortingMode) {
            case AlbumSettings.SORT_BY_NAME:
                Collections.sort(albumMedia.getMedia(), mediaComparators.getNameComparator());
                break;
            case AlbumSettings.SORT_BY_SIZE:
                Collections.sort(albumMedia.getMedia(), mediaComparators.getSizeComparator());
                break;
            case AlbumSettings.SORT_BY_DATE:
            default:
                Collections.sort(albumMedia.getMedia(), mediaComparators.getDateComparator());
                break;
        }
    }

    public void copySelectedPhotos(Context context, String folderPath) {
        albumMedia.copySelectedPhotos(context, folderPath);
    }

    public void copyPhoto(Context context, String olderPath, String folderPath) {
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

            scanFile(context, new String[]{to.getAbsolutePath()});
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteCurrentMedia(Context context) {
        albumMedia.deleteCurrentMedia(context);
    }

    public void deleteMedia(Context context, Media media) {
        albumMedia.deleteMedia(context, media);
    }

    public void deleteSelectedMedia(Context context) {
        albumMedia.deleteSelectedMedia(context);
    }

    public void renameAlbum(Context context, String newName) {
        File dir = new File(StringUtils.getAlbumPathRenamed(getPath(), newName));
        if (dir.mkdir() || dir.exists()) {
            path = dir.getAbsolutePath();
            name = newName;
            for (int i = 0; i < albumMedia.getMediaSize(); i++) {
                Media mediaItem = albumMedia.getMedia(i);
                File from = new File(mediaItem.getPath());
                File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(mediaItem.getPath(), newName));
                if (from.renameTo(to)) {
                    MediaScannerConnection.scanFile(context,
                            new String[]{from.getAbsolutePath(), to.getAbsolutePath()}, null, null);
                    mediaItem.path = to.getAbsolutePath();
                }
            }
        }
    }

    public void scanFile(Context context, String[] path) {   albumMedia.scanFile(context, path); }

}
