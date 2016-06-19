package com.horaapps.leafpic.Base;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.horaapps.leafpic.Adapters.PhotosAdapter;
import com.horaapps.leafpic.R;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.StringUtils;
import com.koushikdutta.ion.builder.Builders;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by dnld on 26/04/16.
 */
public class Album {

    String name = null;
    private String path = null;
    private int count = -1;
    private boolean selected = false;
    private int filter;
    public AlbumSettings settings = new AlbumSettings();
    private int currentMediaIndex = 0;

    private String storageRootPath;

    public ArrayList<Media> media = new ArrayList<Media>();
    public ArrayList<Media> selectedMedias = new ArrayList<Media>();

    public Album() { }

    public Album(String path, String name) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = path;
        this.name = name;
    }

    public Album(String path, String name, int count, String storageRootPath) {
        this(path, name, count);
        this.storageRootPath = storageRootPath;
    }

    public Album(String path, String name, int count) {
        this(path, name);
        this.count = count;
    }

    public Album(Context context, @NotNull File mediaPath) {
        File folder = mediaPath.getParentFile();
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = folder.getPath();
        this.name = folder.getName();
        updatePhotos(context);
        setCurrentPhoto(mediaPath.getAbsolutePath());
    }

    public Album(Context context, Uri mediaUri) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        media.add(0, new Media(context, mediaUri));
        setCurrentPhotoIndex(0);
    }

    public ArrayList<Media> getMedia() {
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();
        switch (filter) {
            case ImageFileFilter.FILTER_ALL:
                mediaArrayList = media;
                default:
                    break;
            case ImageFileFilter.FILTER_GIFS:
                for (Media media1 : media)
                    if (media1.isGif()) mediaArrayList.add(media1);
                break;
            case ImageFileFilter.FILTER_IMAGES:
                for (Media media1 : media)
                    if (media1.isImage()) mediaArrayList.add(media1);
                break;
            case ImageFileFilter.FILTER_VIDEO:
                for (Media media1 : media)
                    if (media1.isVideo()) mediaArrayList.add(media1);
                break;
        }
        return mediaArrayList;
    }

    public void updatePhotos(Context context) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();
        File[] images = new File(getPath()).listFiles(new ImageFileFilter(ImageFileFilter.FILTER_ALL, SP.getBoolean("set_include_video",true)));
        for (File image : images)
            mediaArrayList.add(new Media(image));
        media = mediaArrayList;
        sortPhotos();
        setCount(media.size());
    }

    public ArrayList<String> getParentsFolders() {
        ArrayList<String> result = new ArrayList<String>();
        String[] asd = getPath().split("/");
        String[] asdroot  = storageRootPath.split("/");

        String conc = storageRootPath;
        result.add(conc);
        for (int i = asdroot.length; i < asd.length; i++)
            result.add(conc += "/" + asd[i]);

        Collections.sort(result, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return Integer.compare(rhs.length(), lhs.length());
            }
        });
        return result;
    }

    public void filterMedias(int filter) {
        this.filter = filter;
    }

    public void setSettings(Context context) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        settings = h.getSettings(getPath());
    }

    public boolean hasCustomCover() {
        return settings.coverPath != null;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Media getMedia(int index) { return media.get(index); }

    public void setCurrentPhotoIndex(int index){ currentMediaIndex = index; }

    public Media getCurrentMedia() { return getMedia(currentMediaIndex); }

    public int getCurrentMediaIndex() { return currentMediaIndex; }

    public String getContentDescription(Context c) {
        return c.getString(R.string.media);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    private void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    void setCoverPath(String path) {
        settings.coverPath = path;
    }

    public boolean isHidden() {
        return new File(getPath(), ".nomedia").exists();
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

    private void setCurrentPhoto(String path) {
        for (int i = 0; i < media.size(); i++)
            if (media.get(i).getPath().equals(path)) currentMediaIndex = i;
    }

    public int getSelectedCount() {
        return selectedMedias.size();
    }

    public boolean areMediaSelected() { return getSelectedCount() != 0;}

    public void selectAllPhotos() {
        for (int i = 0; i < media.size(); i++) {
            if (!media.get(i).isSelected()) {
                media.get(i).setSelected(true);
                selectedMedias.add(media.get(i));
            }
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

    public boolean renameCurrentMedia(Context context, String newName) {
        boolean success = false;
        try {
            File from = new File(getCurrentMedia().getPath());
            File to = new File(StringUtils.getPhotoPathRenamed(getCurrentMedia().getPath(), newName));
            if (success =  ContentHelper.moveFile(context, from, to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return success;
    }

    public boolean moveCurrentPhoto(Context context, String newName) {
        boolean success = false;
        try {
            File from = new File(getCurrentMedia().getPath());
            File to = new File(StringUtils.getPhotoPathMoved(getCurrentMedia().getPath(), newName));
            if (success = ContentHelper.moveFile(context, from, to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
                media.remove(getCurrentMediaIndex());
                setCount(media.size());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return success;
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
        for (Media m : media)
            m.setSelected(false);
        selectedMedias.clear();
    }

    public void sortPhotos() {
        MediaComparators mediaComparators = new MediaComparators(settings.ascending);
        switch (settings.columnSortingMode) {
            case AlbumSettings.SORT_BY_NAME:
                Collections.sort(media, mediaComparators.getNameComparator());
                break;
            case AlbumSettings.SORT_BY_SIZE:
                Collections.sort(media, mediaComparators.getSizeComparator());
                break;
            case AlbumSettings.SORT_BY_TYPE:
                Collections.sort(media, mediaComparators.getTypeComparator());
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

    public boolean copyPhoto(Context context, String olderPath, String folderPath) {
        boolean success = false;
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getPhotoPathMoved(olderPath, folderPath));
            if (success = ContentHelper.copyFile(context, from, to))
                scanFile(context, new String[]{ to.getAbsolutePath() });

        } catch (Exception e) { e.printStackTrace(); }
        return success;
    }

    public boolean deleteCurrentMedia(Context context) {
        boolean success = deleteMedia(context, getCurrentMedia());
        if (success) {
            media.remove(getCurrentMediaIndex());
            setCount(media.size());
        }
        return success;
    }

    private boolean deleteMedia(Context context, Media media) {
        boolean success;
        File file = new File(media.getPath());
        if (success = ContentHelper.deleteFile(context, file))
            scanFile(context, new String[]{ file.getAbsolutePath() });
        return success;
    }

    public boolean deleteSelectedMedia(Context context) {
        boolean success = true;
        for (Media selectedMedia : selectedMedias) {
            if (deleteMedia(context, selectedMedia))
                media.remove(selectedMedia);
            else success = false;
        }
        if (success) {
            clearSelectedPhotos();
            setCount(media.size());
        }
        return success;
    }

    public boolean renameAlbum(Context context, String newName) {
        boolean success = true;
        File dir = new File(StringUtils.getAlbumPathRenamed(getPath(), newName));
        if (ContentHelper.mkdir(context, dir)) {
            path = dir.getAbsolutePath();
            name = newName;
            for (int i = 0; i < media.size(); i++) {
                File from = new File(media.get(i).getPath());
                File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(media.get(i).getPath(), newName));
                if (ContentHelper.moveFile(context, from, to)) {
                    MediaScannerConnection.scanFile(context,
                            new String[]{from.getAbsolutePath(), to.getAbsolutePath()}, null, null);
                    media.get(i).path = to.getAbsolutePath();
                } else success = false;
            }
        }
        return success;
    }

    public void scanFile(Context context, String[] path) { MediaScannerConnection.scanFile(context, path, null, null); }

    public boolean areFiltersActive() {
        return filter != ImageFileFilter.FILTER_ALL;
    }
}
