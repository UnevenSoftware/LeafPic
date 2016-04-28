package com.horaapps.leafpic.Base;

import android.content.Context;
import android.util.Log;

import com.horaapps.leafpic.Adapters.PhotosAdapter;
import com.horaapps.leafpic.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 26/04/16.
 */
public class newAlbum {

    String name = null;
    String path = null;
    int count = -1;
    boolean selected = false;

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
        File[] images = new File(getPath()).listFiles(new ImageFileFilter());
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

    public newMedia getCoverAlbum() {
        /*if (hasCustomCover())
            return new Media(settings.coverPath);*/
        if (media.size() > 0)
            return media.get(0); //return also image info like date, orientation...
        return new newMedia("drawable://" + R.drawable.ic_empty);
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
//            Log.d("SELECT", String.format("checking: %d, indexRightBeforeOrAfter: %d targetIndex: %d", indexNow, indexRightBeforeOrAfter, targetIndex));
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
//                Log.d("SELECT", String.format("Selecting: %d", index));
                if (media.get(index) != null) {
                    if (!media.get(index).isSelected()) {
                        media.get(index).setSelected(true);
                        selectedMedias.add(media.get(index));
                        adapter.notifyItemChanged(index);
                    }
                }
            }
        }
//        Log.d("SELECT", String.format("target: %d  indexRightBeforeOrAfter: %d", targetIndex, indexRightBeforeOrAfter));
    }

    public void clearSelectedPhotos() {
        for (newMedia m : media) {
            m.setSelected(false);
        }
        selectedMedias.clear();
    }

}
