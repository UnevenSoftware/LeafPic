package com.horaapps.leafpic.Base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.horaapps.leafpic.R;
import com.horaapps.leafpic.SplashScreen;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    Pattern CAMERA_FOLDER_PATTERN = Pattern.compile("\\b/DCIM/Camera/?$");
    public ArrayList<Album> dispAlbums;

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
        if (in.readByte() == 0x01) {
            selectedAlbums = new ArrayList<Album>();
            in.readList(selectedAlbums, Album.class.getClassLoader());
        } else {
            selectedAlbums = null;
        }
    }

    public void setContext(Context c) {
        context = c;
    }

    public void loadPreviewAlbums() {
        MediaStoreHandler as = new MediaStoreHandler(context);
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        dispAlbums = as.getMediaStoreAlbums(getSortingMode());

        int cameraIndex = -1;

        for (int i = 0; i < dispAlbums.size(); i++) {
            dispAlbums.get(i).medias = as.getFirstAlbumPhoto(dispAlbums.get(i).ID);
            if (!dispAlbums.get(i).setPath())
                dispAlbums.remove(dispAlbums.get(i));
            else {
                dispAlbums.get(i).setCoverPath(h.getPhotPrevieAlbum(dispAlbums.get(i).ID));
                Matcher matcher = CAMERA_FOLDER_PATTERN.matcher(dispAlbums.get(i).Path);
                if (matcher.find()) cameraIndex = i;
            }
        }

        if (cameraIndex != -1) {
            Album camera = dispAlbums.remove(cameraIndex);
            camera.DisplayName = context.getString(R.string.camera);
            dispAlbums.add(0, camera);
        }
    }

    public String getColumnSortingMode() {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);
        return SP.getString("column_sort", MediaStore.Images.ImageColumns.DATE_TAKEN);
    }

    public boolean isAscending() {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);
        return SP.getBoolean("ascending_mode", false);
    }

    public void setDefaultSortingMode(String column) {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = SP.edit();
        editor.putString("column_sort", column);
        editor.apply();
    }

    public void setDefaultSortingAscending(Boolean ascending) {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("ascending_mode", ascending);
        editor.apply();
    }

    public String getSortingMode() {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);

        return " " + SP.getString("column_sort", MediaStore.Images.ImageColumns.DATE_TAKEN)
                + (SP.getBoolean("ascending_mode", false) ? " ASC" : " DESC");
    }

    public void loadExcludedAlbums() {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        MediaStoreHandler as = new MediaStoreHandler(context);
        dispAlbums = h.getExcludedALbums();

        for (int i = 0; i < dispAlbums.size(); i++) {
            dispAlbums.get(i).medias = as.getFirstAlbumPhoto(dispAlbums.get(i).ID);
            dispAlbums.get(i).setPath();
        }
    }

    public int toggleSelectAlbum(int index) {
        if (dispAlbums.get(index) != null) {
            dispAlbums.get(index).setSelcted(!dispAlbums.get(index).isSelected());
            if (dispAlbums.get(index).isSelected()) selectedAlbums.add(dispAlbums.get(index));
            else selectedAlbums.remove(dispAlbums.get(index));
        }
        return index;
    }

    public void selectAllAlbums() {
        for (Album dispAlbum : dispAlbums)
            if (!dispAlbum.isSelected()) {
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

    public Album getAlbum(int p) {
        return dispAlbums.get(p);
    }

    public int getIndex(Album a) {
        return dispAlbums.indexOf(a);
    }

    public void replaceAlbum(int index, Album a) {
        dispAlbums.remove(index);
        dispAlbums.add(index, a);
    }

    public void deleteSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            MediaStoreHandler.deleteAlbumMedia(selectedAlbum, context);
        clearSelectedAlbums();
    }

    public Album getSelectedAlbum(int index) {
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
        if (selectedAlbums == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(selectedAlbums);
        }
    }

    public void InstallShortcutForSelectedAlbums(Context appCtx) {
        for (Album selectedAlbum : selectedAlbums) {

            Intent shortcutIntent;
            shortcutIntent = new Intent(appCtx, SplashScreen.class);
            shortcutIntent.setAction(SplashScreen.ACTION_OPEN_ALBUM);
            shortcutIntent.putExtra("albumID", selectedAlbum.ID);
            shortcutIntent.putExtra("albumName", selectedAlbum.DisplayName);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedAlbum.DisplayName);

            File image = new File(selectedAlbum.getCoverAlbum().Path);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap;

            if(image.toString().endsWith("jpg")
                    || image.toString().endsWith("png")
                    || image.toString().endsWith("jpeg")
                    || image.toString().endsWith("gif")) {
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);//,bmOptions
                bitmap = getCorpedBitmap(bitmap);

            } else {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedAlbum.getCoverAlbum().Path,
                        MediaStore.Images.Thumbnails.MINI_KIND);
                bitmap = getCorpedBitmap(thumb);
            }

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, addWhiteBorder(scaledBitmap, 5));

            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            appCtx.sendBroadcast(addIntent);
        }
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        /*canvas.drawColor(ContextCompat.getColor((context),
                SP.getInt("basic_theme", 1)==1
                        ? R.color.md_light_cards
                        : (SP.getInt("basic_theme", 1)==2
                        ? R.color.md_dark_cards
                        : R.color.md_black_1000))
                );*/
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    private Bitmap getCorpedBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2, 0,
                    srcBmp.getHeight(), srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(srcBmp, 0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(), srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
}