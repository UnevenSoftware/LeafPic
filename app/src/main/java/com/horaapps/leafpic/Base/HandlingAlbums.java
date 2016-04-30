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
import android.os.Environment;
import android.provider.MediaStore;

import com.horaapps.leafpic.SplashScreen;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 27/04/16.
 */
public class HandlingAlbums {

    public ArrayList<Album> dispAlbums;
    private ArrayList<Album> selectedAlbums;

    CustomAlbumsHandler customAlbumsHandler;
    private SharedPreferences SP;

    ArrayList<File> excludedfolders;
    AlbumsComparators albumsComparators;

    public HandlingAlbums(Context context) {
        SP = context.getSharedPreferences("albums-sort", Context.MODE_PRIVATE);
        customAlbumsHandler = new CustomAlbumsHandler(context);

        excludedfolders = new ArrayList<File>();
        loadExcludedFolders(context);
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    public void loadPreviewAlbums() {
        dispAlbums = new ArrayList<Album>();
        fetchRecursivelyFolder(Environment.getExternalStorageDirectory());
        sortAlbums();
    }

    private void fetchRecursivelyFolder(File dir) {
        //if (!excludedfolders.contains(dir)) {
            File[] children = dir.listFiles(new FoldersFileFilter());
            for (File temp : children) {
                File nomedia = new File(temp, ".nomedia");
                if (!excludedfolders.contains(temp) && !temp.isHidden() && !nomedia.exists()) {
                    //not excluded/hidden folder

                    File[] files = temp.listFiles(new ImageFileFilter());
                    if (files.length > 0) {
                        //valid folder
                        Album asd = new Album(temp.getAbsolutePath(), temp.getName(), files.length);
                        asd.setCoverPath(customAlbumsHandler.getPhotPrevieAlbum(asd.getPath()));

                        long lastMod = Long.MIN_VALUE;
                        File choice = null;
                        for (File file : files) {
                            if (file.lastModified() > lastMod) {
                                choice = file;
                                lastMod = file.lastModified();
                            }
                        }
                        if (choice != null)
                            asd.media.add(0, new Media(choice.getAbsolutePath(), choice.lastModified()));

                        dispAlbums.add(asd);
                    }
                    fetchRecursivelyFolder(temp);
                }
            }
        //}
    }

    private void fetchRecursivelyHiddenFolder(File dir) {
        if (!excludedfolders.contains(dir)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            for (File temp : folders) {
                File nomedia = new File(temp, ".nomedia");
                if (nomedia.exists() || temp.isHidden()) {
                    //hidden folder
                    File[] files = temp.listFiles(new ImageFileFilter());
                    if (files.length > 0) {
                        //valid folder
                        Album asd = new Album(temp.getAbsolutePath(), temp.getName(), files.length);
                        //TODO check for album cover
                        long lastMod = Long.MIN_VALUE;
                        File choice = null;
                        for (File file : files) {
                            if (file.lastModified() > lastMod) {
                                choice = file;
                                lastMod = file.lastModified();
                            }
                        }
                        if (choice != null) {
                            asd.media.add(0, new Media(choice.getAbsolutePath(), choice.lastModified()));
                            dispAlbums.add(asd);
                        }
                    }
                    fetchRecursivelyFolder(temp);
                } else fetchRecursivelyFolder(temp);
            }
        }
    }

    public void loadExcludedFolders(Context context) {
        excludedfolders = new ArrayList<File>();
        //forced excluded folder
        excludedfolders.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android"));
        CustomAlbumsHandler handler = new CustomAlbumsHandler(context);
        excludedfolders.addAll(handler.getExcludedFolders());
    }

    public int toggleSelectAlbum(int index) {
        if (dispAlbums.get(index) != null) {
            dispAlbums.get(index).setSelected(!dispAlbums.get(index).isSelected());
            if (dispAlbums.get(index).isSelected()) selectedAlbums.add(dispAlbums.get(index));
            else selectedAlbums.remove(dispAlbums.get(index));
        }
        return index;
    }

    public Album getAlbum(int index){ return dispAlbums.get(index); }

    public void selectAllAlbums() {
        for (Album dispAlbum : dispAlbums)
            if (!dispAlbum.isSelected()) {
                dispAlbum.setSelected(true);
                selectedAlbums.add(dispAlbum);
            }
    }

    public int getSelectedCount() {
        return selectedAlbums.size();
    }

    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums)
            dispAlbum.setSelected(false);

        selectedAlbums.clear();
    }

    public void InstallShortcutForSelectedAlbums(Context appCtx) {
        for (Album selectedAlbum : selectedAlbums) {

            Intent shortcutIntent;
            shortcutIntent = new Intent(appCtx, SplashScreen.class);
            shortcutIntent.setAction(SplashScreen.ACTION_OPEN_ALBUM);
            shortcutIntent.putExtra("albumPath", selectedAlbum.getPath());
            shortcutIntent.putExtra("albumName", selectedAlbum.getName());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedAlbum.getName());

            File image = new File(selectedAlbum.getCoverAlbum().getPath());
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap;

            if(image.toString().endsWith("jpg")
                    || image.toString().endsWith("png")
                    || image.toString().endsWith("jpeg")
                    || image.toString().endsWith("gif")) {
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);//,bmOptions
                bitmap = getCorpedBitmap(bitmap);

            } else {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(selectedAlbum.getCoverAlbum().getPath(),
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
    public void scanFile(Context context, String[] path) {   MediaScannerConnection.scanFile(context, path, null, null); }

    public void hideAlbum(String path, Context context) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (!file.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.flush();
                out.close();
                scanFile(context, new String[]{ file.getAbsolutePath() });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hideAlbum(final Album a, Context context) {
        hideAlbum(a.getPath(), context);
        dispAlbums.remove(a);
    }

    public void deleteSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums) {
            int index = dispAlbums.indexOf(selectedAlbum);
            deleteAlbum(selectedAlbum, context);
            dispAlbums.remove(index);
        }
    }

    public void deleteAlbum(Album album, Context context) {
        File[] files = new File(album.getPath()).listFiles(new ImageFileFilter());
        for (File file : files) {
            if (file.delete()){
                scanFile(context, new String[]{ file.getAbsolutePath() });
            }
        }
    }

    public void hideSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            hideAlbum(selectedAlbum, context);
        clearSelectedAlbums();
    }

    public void excludeSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            excludeAlbum(context, selectedAlbum);

        clearSelectedAlbums();
    }

    public void excludeAlbum(Context context, Album a) {
        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        h.excludeAlbum(a.getPath());
        dispAlbums.remove(a);
    }

    public int getColumnSortingMode() {
        return SP.getInt("column_sort", AlbumSettings.SORT_BY_DATE);
    }

    public boolean isAscending() {
        return SP.getBoolean("ascending_mode", false);
    }


    public void setDefaultSortingMode(int column) {
        SharedPreferences.Editor editor = SP.edit();
        editor.putInt("column_sort", column);
        editor.apply();
    }

    public void setDefaultSortingAscending(Boolean ascending) {
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("ascending_mode", ascending);
        editor.apply();
    }

    public void sortAlbums() {
        albumsComparators = new AlbumsComparators(isAscending());
        switch (getColumnSortingMode()) {
            case AlbumSettings.SORT_BY_NAME:
                Collections.sort(dispAlbums, albumsComparators.getNameComparator());
                break;
            case AlbumSettings.SORT_BY_SIZE:
                Collections.sort(dispAlbums, albumsComparators.getSizeComparator());
                break;
            case AlbumSettings.SORT_BY_DATE:
            default:
                Collections.sort(dispAlbums, albumsComparators.getDateComparator());
                break;
        }
    }

}
