package com.horaapps.leafpic.Base;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;

import com.horaapps.leafpic.Adapters.PhotosAdapter;
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
public class newAlbum {

    String name = null;
    String path = null;
    int count = -1;
    boolean selected = false;

    private int filter_photos = ImageFileFilter.FILTER_ALL;
    public AlbumSettings settings = new AlbumSettings();
    MediaComapartors mediaComapartors;

    public boolean areFiltersActive() {
        return filter_photos != ImageFileFilter.FILTER_ALL;
    }

    public void filterMedias(int filter) {
        filter_photos = filter;
        updatePhotos();
    }

    public void setSettings(Context context) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        settings = h.getSettings(getPath());
    }

    public boolean hasCustomCover() {
        return settings.coverPath != null;
    }

    int current = -1;

    public ArrayList<newMedia> media = new ArrayList<newMedia>();
    public ArrayList<newMedia> selectedMedias = new ArrayList<newMedia>();


    public newAlbum(String path, String name, int count) {
        media = new ArrayList<newMedia>();
        selectedMedias = new ArrayList<newMedia>();
        this.path = path;
        this.name = name;
        this.count = count;
    }


    public newAlbum(String path, String name) {
        media = new ArrayList<newMedia>();
        selectedMedias = new ArrayList<newMedia>();
        this.path = path;
        this.name = name;
    }

    public void updatePhotos() {
        media = new ArrayList<newMedia>();
        File[] images = new File(getPath()).listFiles(new ImageFileFilter(filter_photos));
        for (File image : images)
            media.add(0, new newMedia(image.getAbsolutePath(),image.lastModified()));
    }

    public void loadLastPhoto() {
        File last = MediaFolders.lastFileModified(new File(path));
        media = new ArrayList<newMedia>();
        media.add(0, new newMedia(last.getAbsolutePath(), last.lastModified()));

    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public newMedia getMedia(int index) { return media.get(index); }

    public void setCurrentPhotoIndex(int index){ current = index; }

    public newMedia getCurrentMedia() { return getMedia(current); }

    public String getContentDescdription(Context c) {
        return c.getString(R.string.media);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void setCoverPath(String path) {
        settings.coverPath = path;
    }

    public newMedia getCoverAlbum() {
        if (hasCustomCover())
            return new newMedia(settings.coverPath);
        if (media.size() > 0)
            return media.get(0); //return also image info like date, orientation...
        return new newMedia("drawable://" + R.drawable.ic_empty);//TODO avoid this
    }

    public void setSelectedPhotoAsPreview(Context context) {
        if (selectedMedias.size() > 0) {
            CustomAlbumsHandler h = new CustomAlbumsHandler(context);
            h.setAlbumPhotPreview(getPath(), selectedMedias.get(0).getPath());
            settings.coverPath = selectedMedias.get(0).getPath();
        }
    }

    public int getSelectedCount() {
        return selectedMedias.size();
    }

    public void selectAllPhotos() {
        for (int i = 0; i < media.size(); i++)
            if (!media.get(i).isSelected()) {
                media.get(i).setSelected(true);
                selectedMedias.add(media.get(i));
            }
    }

    public int toggleSelectPhoto(int index) {
        if (media.get(index) != null) {
            media.get(index).setSelected(!media.get(index).isSelected());
            if (media.get(index).isSelected())
                selectedMedias.add(media.get(index));
            else
                selectedMedias.remove(media.get(index));
        }
        return index;
    }

    public void setDefaultSortingMode(Context context, int column) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.setAlbumSortingMode(getPath(), column);
        settings.columnSortingMode = column;
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
        int indexRightBeforeOrAfter = -1;
        int indexNow;
        for (newMedia sm : selectedMedias) {
            indexNow = media.indexOf(sm);
            if (indexRightBeforeOrAfter == -1) {
                indexRightBeforeOrAfter = indexNow;
            }

            if (indexNow > targetIndex) {
                break;
            }
            indexRightBeforeOrAfter = indexNow;
        }

        if (indexRightBeforeOrAfter == -1) {
            Log.wtf("Album", "indexRightBeforeOrAfter==-1 this should not happen.");
        } else {
            for (int index = Math.min(targetIndex, indexRightBeforeOrAfter); index <= Math.max(targetIndex, indexRightBeforeOrAfter); index++) {
                if (media.get(index) != null) {
                    if (!media.get(index).isSelected()) {
                        media.get(index).setSelected(true);
                        selectedMedias.add(media.get(index));
                        adapter.notifyItemChanged(index);
                    }
                }
            }
        }
    }

    public void clearSelectedPhotos() {
        for (newMedia m : media) {
            m.setSelected(false);
        }
        selectedMedias.clear();
    }

    public void sortPhotos() {
        mediaComapartors = new MediaComapartors(settings.ascending);
        switch (settings.columnSortingMode) {
            case AlbumSettings.SORT_BY_NAME:
                Collections.sort(media, mediaComapartors.getNameComapartor());
                break;
            case AlbumSettings.SORT_BY_SIZE:
                Collections.sort(media, mediaComapartors.getSizeComapartor());
                break;
            case AlbumSettings.SORT_BY_DATE:
            default:
                Collections.sort(media, mediaComapartors.getDateComapartor());
                break;
        }
    }

    public void copySelectedPhotos(Context context, String folderPath) {
        for (newMedia media : selectedMedias)
            copyPhoto(context, media.getPath(), folderPath);
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

    public void deleteMedia(Context context, newMedia media) {
        File file = new File(media.getPath());
        if (file.delete())
            scanFile(context, new String[]{ file.getAbsolutePath() });
    }

    public void deleteSelectedMedia(Context context) {
        for (newMedia selectedMedia : selectedMedias) {
            deleteMedia(context, selectedMedia);
            media.remove(selectedMedia);
        }
        clearSelectedPhotos();
    }

    public void scanFile(Context context, String[] path) {   MediaScannerConnection.scanFile(context, path, null, null); }

}
