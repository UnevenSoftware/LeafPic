package org.horaapps.leafpic.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.SplashScreen;
import org.horaapps.leafpic.model.base.AlbumsComparators;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by dnld on 27/04/16.
 */
public class HandlingAlbums extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "tracked_albums.db";
    private static final String TABLE_ALBUMS = "tracked_albums";

    public static final int EXCLUDED = 1;
    public static final int INCLUDED = 2;

    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_ID = "id";
    private static final String ALBUM_PINNED = "pinned";
    private static final String ALBUM_COVER_PATH = "cover_path";
    private static final String ALBUM_STATUS = "status";
    private static final String ALBUM_SORTING_MODE = "sorting_mode";
    private static final String ALBUM_SORTING_ORDER = "sort_ascending";
    private static final String ALBUM_COLUMN_COUNT = "sorting_order";

    private static final String backupFile = "albums2.bck";
    private static HandlingAlbums mInstance = null;

    public ArrayList<Album> albums = null;
    private ArrayList<Album> selectedAlbums = null;
    private HashSet<String> excludedFolders = null;

    private PreferenceUtil SP;

    private int current = 0;
    private boolean hidden;

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_ALBUMS + "(" +
                ALBUM_PATH + " TEXT," +
                ALBUM_ID + " INTEGER," +
                ALBUM_PINNED + " INTEGER," +
                ALBUM_COVER_PATH + " TEXT, " +
                ALBUM_STATUS + " INTEGER, " +
                ALBUM_SORTING_MODE + " INTEGER, " +
                ALBUM_SORTING_ORDER + " INTEGER, " +
                ALBUM_COLUMN_COUNT + " TEXT)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        onCreate(db);
    }

    public void addFolderToWhiteList(String path) {
        SQLiteDatabase db = getWritableDatabase();
        changeSatusAlbum(db, Album.withPath(path), INCLUDED);
        db.close();
    }

    public void excludeAlbum(String path) {
        SQLiteDatabase db = getWritableDatabase();
        excludeAlbum(db, Album.withPath(path));
        db.close();
    }

    private void excludeAlbum(SQLiteDatabase db, Album album) {
        changeSatusAlbum(db, album, EXCLUDED);
        excludedFolders.add(album.getPath());
    }

    private void changeSatusAlbum(SQLiteDatabase db, Album album, int status) {
        ContentValues values = new ContentValues();
        values.put(ALBUM_STATUS, status);
        if (exist(db, album.getPath())) {
            db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ album.getPath() });
        } else {
            values.put(ALBUM_PATH, album.getPath());
            values.put(ALBUM_PINNED, 0);
            values.put(ALBUM_SORTING_MODE, SortingMode.DATE.getValue());
            values.put(ALBUM_SORTING_ORDER, SortingOrder.DESCENDING.getValue());
            values.put(ALBUM_ID, album.getId());
            db.insert(TABLE_ALBUMS, null, values);
        }
    }

    public void clearStatusFolder(String path) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_STATUS, "");
        if (exist(db, path))
            db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        // NOTE: it make no difference if the folder was included
        excludedFolders.remove(path);
        db.close();
    }

    public void excludeSelectedAlbums() {
        SQLiteDatabase db = getWritableDatabase();
        for (Album album : selectedAlbums) {
            excludeAlbum(db, album);
            excludedFolders.add(album.getPath());
            albums.remove(album);
        }
        db.close();
    }

    /**
     *
     * @param status 1 for EXCLUDED, 2 for INCLUDED
     * @return
     */
    public ArrayList<String> getFolders(int status) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ALBUM_PATH}, ALBUM_STATUS + "=?", new String[]{String.valueOf(status)}, null, null, null);
        if (cur.moveToFirst())
            do list.add(cur.getString(0)); while (cur.moveToNext());
        cur.close();
        db.close();
        return list;
    }

    public int getFoldersCount(int status) {
        int c = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{"count(*)"}, ALBUM_STATUS + "=?", new String[]{String.valueOf(status)}, null, null, null);
        if (cur.moveToFirst())
            c = cur.getInt(0);
        cur.close();
        db.close();
        return c;
    }

    private boolean exist(SQLiteDatabase db, String path) {
        Cursor cur = db.rawQuery("SELECT EXISTS(SELECT 1 FROM "+TABLE_ALBUMS+" WHERE "+ALBUM_PATH+"=? LIMIT 1);",
                new String[]{ path });
        boolean tracked = cur.moveToFirst() &&  cur.getInt(0) == 1;
        cur.close();
        return  tracked;
    }

    void pinAlbum(String path, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_PINNED, status ? 1 : 0);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        db.close();
    }

    void setAlbumPhotoPreview(String path, String mediaPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_COVER_PATH, mediaPath);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        db.close();
    }

    void setAlbumSortingMode(String path, int column) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_SORTING_MODE, column);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        db.close();
    }

    void setAlbumSortingOrder(String path, int sortingOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_SORTING_ORDER, sortingOrder);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        db.close();
    }

    @NonNull AlbumSettings getSettings(String path) {
        return getSettings(path, null);
    }

    @NonNull public static AlbumSettings getSettings(SQLiteDatabase db, String path) {
        AlbumSettings settings = null;
        String[] selection = new  String[] { ALBUM_COVER_PATH, ALBUM_SORTING_MODE,
                ALBUM_SORTING_ORDER, ALBUM_PINNED };
        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=?",
                new String[]{ path }, null, null, null);

        if (cursor.moveToFirst())
            return new AlbumSettings(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3));
        cursor.close();
        return settings == null ? AlbumSettings.getDefaults() : settings;
    }

    @NonNull AlbumSettings getSettings(String path, String defaultCover) {
        SQLiteDatabase db = this.getReadableDatabase();

        AlbumSettings settings = null;
        String[] selection = new  String[] { ALBUM_COVER_PATH, ALBUM_SORTING_MODE,
                ALBUM_SORTING_ORDER, ALBUM_PINNED };
        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=?",
                new String[]{ path }, null, null, null);

        if (cursor.moveToFirst())
            return new AlbumSettings(
                    cursor.getString(0) == null ? defaultCover : cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3));
        cursor.close();
        db.close();
        return settings == null ? AlbumSettings.getDefaults() : settings;
    }

    private HandlingAlbums(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SP = PreferenceUtil.getInstance(context);
        albums = new ArrayList<>();
        selectedAlbums = new ArrayList<>();
        excludedFolders = getExcludedFolders(context);
    }

    private HashSet<String> getExcludedFolders(Context context) {
        HashSet<String>  list = new HashSet<>();
        HashSet<File> storageRoots = ContentHelper.getStorageRoots(context);
        for(File file : storageRoots)
            // it has a lot of garbage
            list.add(new File(file.getPath(), "Android").getPath());

        list.addAll(getFolders(EXCLUDED));
        return list;
    }

    public static HandlingAlbums getInstance(Context context) {
        if(mInstance == null)
            mInstance = new HandlingAlbums(context);

        return mInstance;
    }

    public void loadAlbums(Context context, boolean hidden) {
        this.hidden = hidden;
        this.albums = ContentProviderHelper.getAlbums(context, excludedFolders, hidden);
        sortAlbums(context);
    }

    public void addAlbum(int position, Album album) {
        albums.add(position, album);
        setCurrentAlbum(album);
    }

    public void setCurrentAlbum(Album album) {
        current = albums.indexOf(album);
    }

    public Album getCurrentAlbum() {
        return albums.get(current);
    }

    public void saveBackup(final Context context) {
        if (!hidden) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        ObjectOutputStream objectOutStream = new ObjectOutputStream(new FileOutputStream(new File(context.getCacheDir(), backupFile)));
                        objectOutStream.writeObject(albums);
                        objectOutStream.close();
                    } catch (Exception e) {
                        Log.wtf("asd", "Unable to save backup", e);
                    }
                }
            }).start();
        }
    }

    public int getCount() {
        if(albums != null) return albums.size();
        return 0;
    }

    public static void addAlbumToBackup(final Context context, final Album album) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    boolean success = false;
                    File f = new File(context.getCacheDir(), backupFile);
                    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(f));
                    Object o = reader.readObject();
                    ArrayList<Album> list = null;
                    if (o != null) {
                        list = (ArrayList<Album>) o;
                        for(int i = 0; i < list.size(); i++) {
                            if (list.get(i).equals(album)) {
                                list.set(i, album);
                                success = true;
                            }
                        }
                    }

                    if (success) {
                        ObjectOutputStream objectOutStream = new ObjectOutputStream(new FileOutputStream(f));
                        objectOutStream.writeObject(list);
                        objectOutStream.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public boolean restoreBackup(Context context) {
        boolean success = false;
        ObjectInputStream objectInStream = null;
        try {
            File f = new File(context.getCacheDir(), backupFile);
            objectInStream = new ObjectInputStream(new FileInputStream(f));
            Object o = objectInStream.readObject();
            if(o.getClass().equals(ArrayList.class)) {
                albums = (ArrayList<Album>) o;
                success = true;
            }
        } catch (Exception e) {
            albums.clear();
            success = false;
        } finally {
            if (objectInStream != null)
                try { objectInStream.close(); }
                catch (IOException ignored) { }
        }
        return success;
    }

    private int toggleSelectAlbum(int index) {
        if (albums.get(index) != null) {
            albums.get(index).setSelected(!albums.get(index).isSelected());
            if (albums.get(index).isSelected()) selectedAlbums.add(albums.get(index));
            else selectedAlbums.remove(albums.get(index));
        }
        return index;
    }

    @Deprecated
    private int toggleSelectAlbum(Album album) {
        return toggleSelectAlbum(albums.indexOf(album));
    }

    public Album getAlbum(int index){ return albums.get(index); }

    @Deprecated
    private void selectAllAlbums() {
        for (Album dispAlbum : albums)
            if (!dispAlbum.isSelected()) {
                dispAlbum.setSelected(true);
                selectedAlbums.add(dispAlbum);
            }
    }

    public void removeCurrentAlbum(){ albums.remove(current); }

    @Deprecated
    private int getSelectedCount() {
        return selectedAlbums.size();
    }

    @Deprecated
    private void clearSelectedAlbums() {
        for (Album dispAlbum : albums)
            dispAlbum.setSelected(false);

        selectedAlbums.clear();
    }

    public void installShortcutForSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums) {

            Intent shortcutIntent;
            shortcutIntent = new Intent(context, SplashScreen.class);
            shortcutIntent.setAction(SplashScreen.ACTION_OPEN_ALBUM);
            shortcutIntent.putExtra("albumPath", selectedAlbum.getPath());
            shortcutIntent.putExtra("albumId", selectedAlbum.getId());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedAlbum.getName());

            Media coverAlbum = selectedAlbum.getCover();
            File image = new File(coverAlbum.getPath());
            Bitmap bitmap = coverAlbum.isVideo() ? ThumbnailUtils.createVideoThumbnail(coverAlbum.getPath(), MediaStore.Images.Thumbnails.MINI_KIND)
                    : BitmapFactory.decodeFile(image.getAbsolutePath(), new BitmapFactory.Options());

            if (bitmap == null) {
                Toast.makeText(context, R.string.error_thumbnail, Toast.LENGTH_SHORT).show();
                // TODO: 12/31/16
                return;
            }

            bitmap = Bitmap.createScaledBitmap(getCroppedBitmap(bitmap), 128, 128, false);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, addWhiteBorder(bitmap, 5));

            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            context.sendBroadcast(addIntent);
        }
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    private Bitmap getCroppedBitmap(Bitmap srcBmp){
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

    private void scanFile(Context context, String[] path) {  MediaScannerConnection.scanFile(context, path, null, null); }

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
    public void hideSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            hideAlbum(selectedAlbum, context);
        clearSelectedAlbums();
    }

    private void hideAlbum(final Album a, Context context) {
        hideAlbum(a.getPath(), context);
        albums.remove(a);
    }

    public void unHideAlbum(String path, Context context) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (file.exists()) {
            if (file.delete())
                scanFile(context, new String[]{ file.getAbsolutePath() });
        }
    }
    public void unHideSelectedAlbums(Context context) {
        for (Album selectedAlbum : selectedAlbums)
            unHideAlbum(selectedAlbum, context);
        clearSelectedAlbums();
    }

    private void unHideAlbum(final Album a, Context context) {
        unHideAlbum(a.getPath(), context);
        albums.remove(a);
    }

    @Deprecated
    public boolean deleteSelectedAlbums(Context context) {
        boolean success = true;

        for (Album selectedAlbum : selectedAlbums) {
            int index = albums.indexOf(selectedAlbum);
            if(deleteAlbum(selectedAlbum, context))
                albums.remove(index);
            else success = false;
        }
        return success;
    }

    public boolean deleteAlbum(Album album, Context context) {
        return ContentHelper.deleteFilesInFolder(context, new File(album.getPath()));
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(SP.getInt("albums_sorting_mode", SortingMode.DATE.getValue()));
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(SP.getInt("albums_sorting_order", SortingOrder.DESCENDING.getValue()));
    }

    public void setDefaultSortingMode(SortingMode sortingMode) {
        SP.putInt("albums_sorting_mode", sortingMode.getValue());
    }

    public void setDefaultSortingAscending(SortingOrder sortingOrder) {
        SP.putInt("albums_sorting_order", sortingOrder.getValue());
    }

    @Deprecated
    public void sortAlbums(final Context context) {

        Album camera = null;

        Iterator<Album> iterator = albums.iterator();
        while (iterator.hasNext()) {
            Album a = iterator.next();
            if (a.getName().equals("Camera")) {
                camera = a;
                iterator.remove();
                break;
            }
        }

        Collections.sort(albums, AlbumsComparators.getComparator(getSortingMode(), getSortingOrder()));

        if (camera != null) {
            camera.setName(context.getString(R.string.camera));
            albums.add(0, camera);
        }
    }
    @Deprecated
    private Album getSelectedAlbum(int index) { return selectedAlbums.get(index); }
}
