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
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dnld on 26/04/16.
 */
public class Album {

    String name = null;
    private String path = null;
    private int count = -1;
    private boolean selected = false;
    private int filter_photos;
    public AlbumSettings settings = new AlbumSettings();
    private int current = -1;

    private String storageRootPath;

    public ArrayList<Media> media = new ArrayList<Media>();
    public ArrayList<Media> selectedMedias = new ArrayList<Media>();

    public Album() {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
    }

    public Album(String path, String name, int count, String storageRootPath) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = path;
        this.name = name;
        this.count = count;
        this.storageRootPath = storageRootPath;
    }

    public Album(String path, String name, int count) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = path;
        this.name = name;
        this.count = count;
    }

    public Album(Context context, File mediaPath) {
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

    public Album(String path, String name) {
        media = new ArrayList<Media>();
        selectedMedias = new ArrayList<Media>();
        this.path = path;
        this.name = name;
    }

    public void updatePhotos(Context context) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<Media> mediaArrayList = new ArrayList<Media>();
        File[] images = new File(getPath()).listFiles(new ImageFileFilter(filter_photos, SP.getBoolean("set_include_video",true)));
        for (File image : images)
            mediaArrayList.add(0, new Media(image));
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

   /* public void updatePhotos(PhotosAdapter adapter) {
        media = new ArrayList<Media>();
        File[] images = new File(getPath()).listFiles(new ImageFileFilter(filter_photos));
        for (int i = 0; i < images.length; i++) {
            media.add(0, new Media(images[i].getAbsolutePath(), images[i].lastModified(), images[i].length()));
            adapter.notifyItemInserted(i);
        }
        sortPhotos();
        adapter.notifyDataSetChanged();
    }*/

    public boolean areFiltersActive() {
        return filter_photos != ImageFileFilter.FILTER_ALL;
    }

    public void filterMedias(Context context, int filter) {
        filter_photos = filter;
        updatePhotos(context);
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

    public void setCurrentPhotoIndex(int index){ current = index; }

    public Media getCurrentMedia() { return getMedia(current); }

    public int getCurrentMediaIndex() { return current; }

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
            if (ContentHelper.moveFile(context, from, to)) {
                scanFile(context, new String[]{ to.getAbsolutePath(), from.getAbsolutePath() });
                getCurrentMedia().path = to.getAbsolutePath();
                media.remove(getCurrentMediaIndex());
                setCount(media.size());
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

    public void copyPhoto(Context context, String olderPath, String folderPath) {
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getPhotoPathMoved(olderPath, folderPath));

            if (ContentHelper.copyFile(context, from, to)) {
                scanFile(context, new String[]{ to.getAbsolutePath() });
            }
            /*InputStream in = new FileInputStream(from);
            OutputStream out = new FileOutputStream(to);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close();
            out.close();*/

            //scanFile(context, new String[]{to.getAbsolutePath()});
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteCurrentMedia(Context context) {
        deleteMedia(context, media.get(getCurrentMediaIndex()));
        media.remove(getCurrentMediaIndex());
        setCount(media.size());
    }

    private void deleteMedia(Context context, Media media) {
        File file = new File(media.getPath());
        if (ContentHelper.deleteFile(context, file))
            scanFile(context, new String[]{ file.getAbsolutePath() });
    }

    public void deleteSelectedMedia(Context context) {
        for (Media selectedMedia : selectedMedias) {
            deleteMedia(context, selectedMedia);
            media.remove(selectedMedia);
        }
        clearSelectedPhotos();
        setCount(media.size());
    }

    public void renameAlbum(Context context, String newName) {
        File dir = new File(StringUtils.getAlbumPathRenamed(getPath(), newName));
        if (ContentHelper.mkdir(context, dir)) {
            path = dir.getAbsolutePath();
            name = newName;
            for (int i = 0; i < media.size(); i++) {
                File from = new File(media.get(i).getPath());
                File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(media.get(i).getPath(), newName));
                //File to = new File(StringUtils.getPhotoPathRenamedAlbumChange(media.get(i).getPath(), newName));
                if (ContentHelper.moveFile(context, from, to)) {
                    MediaScannerConnection.scanFile(context,
                            new String[]{from.getAbsolutePath(), to.getAbsolutePath()}, null, null);
                    media.get(i).path = to.getAbsolutePath();
                }
            }
        }
    }

    public void scanFile(Context context, String[] path) { MediaScannerConnection.scanFile(context, path, null, null); }

}
