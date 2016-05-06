package com.horaapps.leafpic.Base;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

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
public class Album {

    String name = null;
    String path = null;
    int count = -1;
    boolean selected = false;

    private int filter_photos;// = ImageFileFilter.FILTER_ALL;
    public AlbumSettings settings = new AlbumSettings();
    MediaComparators mediaComparators;

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

    public ArrayList<Media> media = new ArrayList<Media>();
    public ArrayList<Media> selectedMedias = new ArrayList<Media>();

    public Album() {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
    }

    public Album(String path, String name, int count) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = path;
        this.name = name;
        this.count = count;
    }

    public Album(String mediaPath) {
        File folder = new File(mediaPath).getParentFile();
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = folder.getPath();
        this.name = folder.getName();
        updatePhotos();
        setCurrentPhoto(mediaPath);
    }

    public Album(String path, String name) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = path;
        this.name = name;
        //updatePhotos();
    }

    public void updatePhotos() {
        media = new ArrayList<Media>();
        File[] images = new File(getPath()).listFiles(new ImageFileFilter(filter_photos));
        for (File image : images)
            media.add(0, new Media(image.getAbsolutePath(), image.lastModified(), image.length()));
        sortPhotos();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Media getMedia(int index) { return media.get(index); }

    public void setCurrentPhotoIndex(int index){ current = index; }

    public Media getCurrentMedia() { return getMedia(current); }

    public int getCurrentMediaIndex() { return current; }

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

    public Media getCoverAlbum() {
        if (hasCustomCover())
            return new Media(settings.coverPath);
        if (media.size() > 0)
            return media.get(0);
        return new Media();
    }

    public void setSelectedPhotoAsPreview(Context context) {
        if (selectedMedias.size() > 0) {
            CustomAlbumsHandler h = new CustomAlbumsHandler(context);
            h.setAlbumPhotPreview(getPath(), selectedMedias.get(0).getPath());
            settings.coverPath = selectedMedias.get(0).getPath();
        }
    }

    public void setCurrentPhoto(String path) {
        for (int i = 0; i < media.size(); i++)
            if (media.get(i).getPath().equals(path)) current = i;
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

    public void renameCurrentMedia(Context context, String newName) {
        try {
            File from = new File(getCurrentMedia().getPath());
            File to = new File(StringUtils.getPhotoPathRenamed(getCurrentMedia().getPath(), newName));
            if (from.renameTo(to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int moveCurrentPhoto(Context context, String newName) {
        try {
            File from = new File(getCurrentMedia().getPath());
            File to = new File(StringUtils.getPhotoPathMoved(getCurrentMedia().getPath(), newName));
            if (from.renameTo(to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
                media.remove(getCurrentMediaIndex());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return getCurrentMediaIndex();
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
        for (Media sm : selectedMedias) {
            indexNow = media.indexOf(sm);
            if (indexRightBeforeOrAfter == -1) {
                indexRightBeforeOrAfter = indexNow;
            }

            if (indexNow > targetIndex) {
                break;
            }
            indexRightBeforeOrAfter = indexNow;
        }

        if (indexRightBeforeOrAfter != -1) {
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
        for (Media m : media) {
            m.setSelected(false);
        }
        selectedMedias.clear();
    }

    public void sortPhotos() {
        mediaComparators = new MediaComparators(settings.ascending);
        switch (settings.columnSortingMode) {
            case AlbumSettings.SORT_BY_NAME:
                Collections.sort(media, mediaComparators.getNameComparator());
                break;
            case AlbumSettings.SORT_BY_SIZE:
                Collections.sort(media, mediaComparators.getSizeComparator());
                break;
            case AlbumSettings.SORT_BY_DATE:
            default:
                Collections.sort(media, mediaComparators.getDateComparator());
                break;
        }
    }

    public void copySelectedPhotos(Context context, String folderPath) {
        for (Media media : selectedMedias)
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

    public void deleteMedia(Context context, Media media) {
        File file = new File(media.getPath());
        if (file.delete())
            scanFile(context, new String[]{ file.getAbsolutePath() });
    }

    public void deleteSelectedMedia(Context context) {
        for (Media selectedMedia : selectedMedias) {
            deleteMedia(context, selectedMedia);
            media.remove(selectedMedia);
        }
        clearSelectedPhotos();
    }

    public void renameAlbum(Context context, String newName) {
        File dir = new File(StringUtils.getAlbumPathRenamed(getPath(), newName));
        if (dir.mkdir() || dir.exists()) {
            path = dir.getAbsolutePath();
            name = newName;
            for (int i = 0; i < media.size(); i++) {
                File from = new File(media.get(i).getPath());
                File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(media.get(i).getPath(), newName));
                if (from.renameTo(to)) {
                    MediaScannerConnection.scanFile(context,
                            new String[]{from.getAbsolutePath(), to.getAbsolutePath()}, null, null);
                    media.get(i).path = to.getAbsolutePath();
                }
            }
        }
    }

    public void scanFile(Context context, String[] path) {   MediaScannerConnection.scanFile(context, path, null, null); }

}
