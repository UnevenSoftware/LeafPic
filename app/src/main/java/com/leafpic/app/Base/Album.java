package com.leafpic.app.Base;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 12/11/15.
 */
public class Album implements Parcelable {

    public static final int FILTER_ALL=45;
    public static final int FILTER_IMAGE=55;
    public static final int FILTER_VIDEO=75;
    public static final int FILTER_GIF=555;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    public String ID = null;
    public String DisplayName = null;
    public String Path = null;
    public AlbumSettings settings = new AlbumSettings();
    public AlbumMediaCount count = new AlbumMediaCount();
    public ArrayList<Media> medias = new ArrayList<Media>();
    public ArrayList<Media> selectedMedias = new ArrayList<Media>();
    Context context;
    private boolean selected = false;
    private int current = -1;
    private int filter_photos = FILTER_ALL;
    private Integer last_position_selecte = -1;

    public Album(String id, String name, AlbumMediaCount count) {
        ID = id;
        DisplayName = name;
        this.count = count;
    }

    public Album(String id) {
        ID = id;
    }

    public Album(Context ctx) {
        context = ctx;
    }

    public Album(Context ctx, String photoPath) {
        context = ctx;
        MadiaStoreHandler as = new MadiaStoreHandler(context);
        ID = as.getAlbumPhoto(photoPath);
        setSettings();
        updatePhotos();
        setCurrentPhoto(photoPath);
    }

    /**
     * parcellable
     */

    protected Album(Parcel in) {
        ID = in.readString();
        DisplayName = in.readString();
        Path = in.readString();
        settings = (AlbumSettings) in.readValue(AlbumSettings.class.getClassLoader());
        count = (AlbumMediaCount) in.readValue(AlbumMediaCount.class.getClassLoader());
        if (in.readByte() == 0x01) {
            medias = new ArrayList<Media>();
            in.readList(medias, Media.class.getClassLoader());
        } else {
            medias = null;
        }
        current = in.readInt();
        filter_photos = in.readInt();
        last_position_selecte = in.readByte() == 0x00 ? null : in.readInt();
    }

    public void setContext(Context ctx) {
        context = ctx;
    }

    public void setSettings() {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        settings = h.getSettings(ID);
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

    public String getContentDescdription(Context c){
        if (count.photos>0 && count.videos==0) return  count.photos== 1 ? c.getString(R.string.singular_photo) : c.getString(R.string.plural_photos);
        else if (count.photos==0 && count.videos>0) return c.getString(R.string.video);
        else return c.getString(R.string.media);
    }

    /** media stuff */
    public boolean areFiltersActive(){
        return filter_photos != FILTER_ALL;
    }

    public void filterMedias(int filter){
        filter_photos=filter;
        updatePhotos();
    }

    public void updatePhotos() {
        MadiaStoreHandler as = new MadiaStoreHandler(context);
        medias = as.getAlbumPhotos(ID, getSortingMode(), filter_photos);
    }

    public void clearSelectedPhotos() {
        for (Media media : medias) {
            media.setSelected(false);
        }
        selectedMedias.clear();
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
            }
    }

    public int toggleSelectPhoto(String path) {
        Media x = getPhoto(path);
        if (x != null) {
            x.setSelected(!x.isSelected());
            if (x.isSelected())
                selectedMedias.add(x);
             else
                selectedMedias.remove(x);
        }

        return last_position_selecte;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelcted(boolean value) {
        selected = value;
    }

    public boolean setPath() {
        if(medias.size() > 0) {
            Path = StringUtils.getBucketPathbyImagePath(medias.get(0).Path);
            return true;
        }
        return false;
    }

    public boolean hasCustomCover() {
        return settings.coverPath != null;
    }

    public String getPathCoverAlbum() {
        if (hasCustomCover()) return settings.coverPath;
        if (medias.size() > 0) return "file://" + medias.get(0).Path;
        else return "drawable://" + R.drawable.ic_empty;
    }

    public Media getCoverAlbum() {
        if (hasCustomCover())
            return new Media(settings.coverPath);
        if (medias.size() > 0) return medias.get(0); //return also image info like date, orientation...
        return new Media("drawable://" + R.drawable.ic_empty);
    }

    public void setCoverPath(String path) {
        settings.coverPath = path;
    }

    public void setSelectedPhotoAsPreview() {
        if (selectedMedias.size() > 0) {
            CustomAlbumsHandler h = new CustomAlbumsHandler(context);
            h.setAlbumPhotPreview(ID, selectedMedias.get(0).Path);
            settings.coverPath = selectedMedias.get(0).Path;
        }
    }

    public int getImagesCount() {
        return count.getTotal();
    }

    public void deleteSelectedPhotos() {
        for (Media media : selectedMedias)
            deletePhoto(media);

        clearSelectedPhotos();
    }

    public void deleteCurrentPhoto() {
        deletePhoto(getCurrentPhoto());
    }

   /* public void movePhoto(String olderPath, String folderPath) {
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
    }*/

    public ArrayList<Media> getSelectedMedias() {
        return selectedMedias;
    }

    public void deletePhoto(Media a) {
        HandlingAlbums h = new HandlingAlbums(context);
        File file = new File(a.Path);
        h.deleteFolderRecursive(file);
        medias.remove(a);
    }

    public void renamePhoto(String olderPath, String path) {
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
        dest.writeString(ID);
        dest.writeString(DisplayName);
        dest.writeString(Path);
        dest.writeValue(settings);
        dest.writeValue(count);
        if (medias == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(medias);
        }
        dest.writeInt(current);
        dest.writeInt(filter_photos);
        if (last_position_selecte == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(last_position_selecte);
        }
    }
}