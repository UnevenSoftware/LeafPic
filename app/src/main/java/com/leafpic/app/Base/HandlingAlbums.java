package com.leafpic.app.Base;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.leafpic.app.R;
import com.leafpic.app.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class HandlingAlbums implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<HandlingAlbums> CREATOR = new Parcelable.Creator<HandlingAlbums>() {
        @Override
        public HandlingAlbums createFromParcel(Parcel in) {
            return new HandlingAlbums(in);
        }

        @Override
        public HandlingAlbums[] newArray(int size) {
            return new HandlingAlbums[size];
        }
    };

    private SharedPreferences SP;
    public final String CAMERA_PATTERN = "DCIM/Camera";//TODO improve with regex
    public ArrayList<Album> dispAlbums;
    public int last_position_selecte = -1;

    private Context context;
    private ArrayList<Album> selectedAlbums;

    public HandlingAlbums(Context ctx) {
        context = ctx;
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    protected HandlingAlbums(Parcel in) {
        if (in.readByte() == 0x01) {
            dispAlbums = new ArrayList<Album>();
            in.readList(dispAlbums, Album.class.getClassLoader());
        } else {
            dispAlbums = null;
        }
        last_position_selecte = in.readInt();
        if (in.readByte() == 0x01) {
            selectedAlbums = new ArrayList<Album>();
            in.readList(selectedAlbums, Album.class.getClassLoader());
        } else {
            selectedAlbums = null;
        }
    }
    public void setContext(Context c){
        context=c;
    }

    public void loadPreviewAlbums() {
        MediaStoreHandler as = new MediaStoreHandler(context);
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        dispAlbums = as.getMediaStoreAlbums(getSortingMode());

        int cameraIndex = -1;

        for (int i = 0; i < dispAlbums.size(); i++) {
            dispAlbums.get(i).medias = as.getFirstAlbumPhoto(dispAlbums.get(i).ID);
            if(!dispAlbums.get(i).setPath())
                dispAlbums.remove(dispAlbums.get(i));
            else {
                dispAlbums.get(i).setCoverPath(h.getPhotPrevieAlbum(dispAlbums.get(i).ID));
                if (dispAlbums.get(i).Path.contains(CAMERA_PATTERN)) cameraIndex = i;
            }
        }

        if (cameraIndex != -1) {
            Album camera = dispAlbums.remove(cameraIndex);
            camera.DisplayName = context.getString(R.string.camera);
            dispAlbums.add(0, camera);
        }
    }

    public String getColumnSortingMode() {
        SP = context.getSharedPreferences("albums-sort",Context.MODE_PRIVATE);
        return SP.getString("column_sort", MediaStore.Images.ImageColumns.DATE_TAKEN);
    }

    public boolean isAscending() {
        SP = context.getSharedPreferences("albums-sort",Context.MODE_PRIVATE);
        return SP.getBoolean("ascending_mode", false);
    }

    public void setDefaultSortingMode(String column) {
        SP = context.getSharedPreferences("albums-sort",Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = SP.edit();
        editor.putString("column_sort",column);
        editor.apply();
    }

    public void setDefaultSortingAscending(Boolean ascending) {
        SP = context.getSharedPreferences("albums-sort",Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("ascending_mode",ascending);
        editor.apply();
    }

    public String getSortingMode() {
        SP = context.getSharedPreferences("albums-sort",Context.MODE_PRIVATE);

        return " " +  SP.getString("column_sort", MediaStore.Images.ImageColumns.DATE_TAKEN)
                + ( SP.getBoolean("ascending_mode", false) ? " ASC" : " DESC");
    }

    public void loadExcludedAlbums(){
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        MediaStoreHandler as = new MediaStoreHandler(context);
        dispAlbums = h.getExcludedALbums();

        for (int i = 0; i < dispAlbums.size(); i++) {
            dispAlbums.get(i).medias = as.getFirstAlbumPhoto(dispAlbums.get(i).ID);
            dispAlbums.get(i).setPath();
        }
    }

    public int toggleSelectAlbum(String path) {
        Album s = getAlbum(path);
        if (s != null) {
            s.setSelcted(!s.isSelected());
            if (s.isSelected()) selectedAlbums.add(s);
            else selectedAlbums.remove(s);
        }
        return last_position_selecte;
    }

    public int selectAlbum(String path, boolean val) {
        Album x = getAlbum(path);
        if (x != null) {
            if (val) selectedAlbums.add(x);
            else selectedAlbums.remove(x);
            x.setSelcted(val);
        }
        return last_position_selecte;
    }

    public void selectAllAlbums(){
        for (Album dispAlbum : dispAlbums)
            if(!dispAlbum.isSelected()) {
                dispAlbum.setSelcted(true);
                selectedAlbums.add(dispAlbum);
            }
    }

    public int getSelectedCount() {
        return selectedAlbums.size();
    }

    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums)
            dispAlbum.setSelcted(false);

        selectedAlbums.clear();
    }

    public Album getAlbum(String p) {
        for (int i = 0; i < dispAlbums.size(); i++)
            if (dispAlbums.get(i).Path.equals(p)) {
                last_position_selecte = i;
                return dispAlbums.get(i);
            }
        return null;
    }

    public void deleteSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
                MediaStoreHandler.deleteAlbumMedia(selectedAlbum,context);
        clearSelectedAlbums();
    }

    public Album getSelectedAlbum(int index){
        return selectedAlbums.get(index);
    }

    public void scanFile(String[] path) {
        MediaScannerConnection.scanFile(context, path, null, null);
    }

    public void hideSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            hideAlbum(selectedAlbum);
        clearSelectedAlbums();
    }

    public void hideAlbum(final Album a) {
        hideAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void hideAlbum(String path) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (!file.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.flush();
                out.close();
                scanFile(new String[]{file.getAbsolutePath()});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void excludeSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            excludeAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void excludeAlbum(Album a) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.excludeAlbum(a.ID);
        dispAlbums.remove(a);
    }

    /*************
     * This Metods doesnt work for the moment
     **************/

    public void renameAlbum(String olderPath, String name) {
        try {
            File from = new File(olderPath);
            File to = new File(StringUtils.getAlbumPathRenamed(olderPath, name));
            String s[] = from.list(), dirPath = from.getAbsolutePath();
            for (String paht : s) {
                scanFile(new String[]{dirPath + "/" + paht});
            }
            from.renameTo(to);
            s = to.list();
            dirPath = to.getAbsolutePath();
            for (String paht : s) scanFile(new String[]{dirPath + "/" + paht});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dispAlbums == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(dispAlbums);
        }
        dest.writeInt(last_position_selecte);
        if (selectedAlbums == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(selectedAlbums);
        }
    }
}