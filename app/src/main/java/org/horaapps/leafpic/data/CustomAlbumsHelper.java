package org.horaapps.leafpic.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.horaapps.leafpic.data.base.SortingMode;
import org.horaapps.leafpic.data.base.SortingOrder;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dnld on 2/1/16.
 */


public class CustomAlbumsHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "album_settings.db";

    private static final String TABLE_ALBUMS = "albums";
    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_ID = "id";
    private static final String ALBUM_EXCLUDED = "excluded";
    private static final String ALBUM_PINNED = "pinned";
    private static final String ALBUM_COVER_PATH = "cover_path";
    private static final String ALBUM_SORTING_MODE = "sorting_mode";
    private static final String ALBUM_SORTING_ORDER = "sort_ascending";
    private static final String ALBUM_COLUMN_COUNT = "sorting_order";

    private static final String TABLE_MEDIA = "media";
    private static final String EXCLUDED_MEDIA = "excluded";

    private static CustomAlbumsHelper instance;

    public static CustomAlbumsHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (CustomAlbumsHelper.class) {
                if (instance == null)
                    instance = new CustomAlbumsHelper(context);
            }
        }
        return instance;
    }

    private CustomAlbumsHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        this.createAlbumsTable(db);
        this.createMediaTable(db);

        // NOTE: excluded music folder by default
        ContentValues values = new ContentValues();
        values.put(ALBUM_ID, -1);
        values.put(ALBUM_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        values.put(ALBUM_EXCLUDED, 1);

        db.insert(TABLE_ALBUMS, null, values);
    }

    private void createAlbumsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                           TABLE_ALBUMS + "(" +
                           ALBUM_PATH + " TEXT," +
                           ALBUM_ID + " INTEGER," +
                           ALBUM_EXCLUDED + " INTEGER," +
                           ALBUM_PINNED + " INTEGER," +
                           ALBUM_COVER_PATH + " TEXT, " +
                           ALBUM_SORTING_MODE + " INTEGER, " +
                           ALBUM_SORTING_ORDER + " INTEGER, " +
                           ALBUM_COLUMN_COUNT + " TEXT)");
    }

    private void createMediaTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                           TABLE_MEDIA + "(" +
                           ALBUM_ID + " INTEGER," +
                           ALBUM_PATH + " TEXT," +
                           EXCLUDED_MEDIA + " TEXT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
        onCreate(db);
    }

    private void checkAndCreateAlbum(SQLiteDatabase db, String path, long id) {

        Cursor cursor = db.query(TABLE_ALBUMS, null,  ALBUM_PATH+"=? AND "+ALBUM_ID+"=?",
                new String[]{ path, String.valueOf(id) }, null, null, null);

        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_ID, id);
            values.put(ALBUM_PATH, path);
            values.put(ALBUM_SORTING_MODE, SortingMode.DATE.getValue());
            values.put(ALBUM_SORTING_ORDER, SortingOrder.DESCENDING.getValue());
            values.put(ALBUM_EXCLUDED, 0);
            db.insert(TABLE_ALBUMS, null, values);
        }
        cursor.close();


        cursor = db.query(TABLE_MEDIA, null, ALBUM_PATH + "=? AND " + ALBUM_ID + "=?",
                new String[]{ path, String.valueOf(id)}, null, null, null);

        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_ID, id);
            values.put(ALBUM_PATH, path);
            values.put(EXCLUDED_MEDIA, new JSONArray().toString());
            db.insert(TABLE_MEDIA, null, values);
        }

        cursor.close();
    }

    AlbumSettings getSettings(String path, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, path, id);

        AlbumSettings s = null;

        String[] selection = new  String[] { ALBUM_COVER_PATH, ALBUM_SORTING_MODE,
                ALBUM_SORTING_ORDER, ALBUM_PINNED };
        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?",
                new String[]{ path, String.valueOf(id) }, null, null, null);

        if (cursor.moveToFirst())
            s = new AlbumSettings(path, id, cursor.getString(0), cursor.getInt(1), cursor.getInt(2),cursor.getInt(3));
        cursor.close();
        db.close();
        return s;
    }

    public ArrayList<File> getExcludedFolders() {
        ArrayList<File> list = new ArrayList<File>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ALBUM_EXCLUDED + "=1";
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ ALBUM_PATH }, selection, null, null, null, null);
        if (cur.moveToFirst())
            do list.add(new File(cur.getString(0))); while (cur.moveToNext());
        cur.close();
        db.close();
        return list;
    }

    public ArrayList<String> getPinnedAlbums() {
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ALBUM_PINNED + "=1";
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ ALBUM_PATH }, selection, null, null, null, null);
        if (cur.moveToFirst())
            do list.add(cur.getString(0)); while (cur.moveToNext());
        cur.close();
        db.close();
        return list;
    }

    public ArrayList<String> getExcludedFoldersPaths() {
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = ALBUM_EXCLUDED + "=1";
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ ALBUM_PATH }, selection, null, null, null, null);
        if (cur.moveToFirst())
            do list.add(cur.getString(0)); while (cur.moveToNext());
        cur.close();
        db.close();
        return list;
    }

    public void clearAlbumExclude(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_EXCLUDED, 0);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        db.close();
    }

    public void excludeAlbum(String path, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, path, id);
        ContentValues values = new ContentValues();
        values.put(ALBUM_EXCLUDED, 1);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, id+"" });
        db.close();
    }

    public void pinAlbum(String path, long id, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, path, id);
        ContentValues values = new ContentValues();
        values.put(ALBUM_PINNED, status ? 1 : 0);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, id+"" });
        db.close();
    }

    public void unExcludePhoto(String photoPath, String albumPath, long albumId) {
        Set<String> excludedPhotos = this.getExcludedPhotos(albumPath, albumId);
        if (!excludedPhotos.contains(photoPath))
            return;

        excludedPhotos.remove(photoPath);

        Object[] currentExcludedPathObjects = excludedPhotos.toArray();
        String[] currentExcludedPathStrings = Arrays.copyOf(currentExcludedPathObjects, currentExcludedPathObjects.length, String[].class);
        writeExcludedPhotoArrayToDatabase(currentExcludedPathStrings, albumPath, albumId);
    }

    public List<SimpleMediaIdentifier> getExcludedMedias() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_MEDIA, new String[] {ALBUM_PATH, ALBUM_ID, EXCLUDED_MEDIA},
                null, null, null, null, null);

        List<SimpleMediaIdentifier> mediaList = new ArrayList<SimpleMediaIdentifier>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String albumPath = cursor.getString(0);
            long albumId = cursor.getLong(1);
            try {
                JSONArray excludedMedia = new JSONArray(cursor.getString(2));
                for (int i = 0; i < excludedMedia.length(); i++) {
                    try {
                        String mediaPath = excludedMedia.getString(i);
                        SimpleMediaIdentifier newIdentifier = new SimpleMediaIdentifier(albumPath,
                                                                                               albumId, mediaPath);
                        mediaList.add(newIdentifier);

                    } catch (JSONException ignored) { }
                }
            } catch (JSONException e) {
                continue;
            }

            cursor.moveToNext();
        }
        cursor.close();
        return mediaList;
    }

    Set<String> getExcludedPhotos(String albumPath, long albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, albumPath, albumId);

        Cursor cursor = db.query(TABLE_MEDIA, new String[] {EXCLUDED_MEDIA},
                ALBUM_PATH + "=? AND " + ALBUM_ID + "=?", new String[] {albumPath, String.valueOf(albumId)}, null, null, null);

        Set<String> excludedPhotos = new HashSet<String>();
        if (cursor.moveToFirst()) {
            JSONArray excludedPhotoJsonBlob = null;
            try {
                excludedPhotoJsonBlob = new JSONArray(cursor.getString(0));
            } catch (JSONException ignored) {  }

            if (excludedPhotoJsonBlob != null) {
                for (int i = 0; i < excludedPhotoJsonBlob.length(); i++)
                    try {
                        excludedPhotos.add(excludedPhotoJsonBlob.getString(i));
                    } catch (JSONException ignored) {  }
            }
        }

        cursor.close();
        db.close();
        return excludedPhotos;
    }

    private void writeExcludedPhotoArrayToDatabase(String[] excludedPhotoPaths, String albumPath, long albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String newValue = null;
        try {
            newValue = new JSONArray(excludedPhotoPaths).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        values.put(EXCLUDED_MEDIA, newValue);

        db.update(TABLE_MEDIA, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ albumPath, albumId+"" });
        db.close();
    }

    void excludePhoto(Media photo, long albumId, String albumPath) {
        List<Media> singlePhotoList = new ArrayList<Media>();
        singlePhotoList.add(photo);

        this.excludePhotos(singlePhotoList, albumId, albumPath);
    }

    void excludePhotos(List<Media> photos, long albumId, String albumPath) {
        Set<String> currentExcludedPaths = getExcludedPhotos(albumPath, albumId);

        for (Media photo : photos) {
            if (!currentExcludedPaths.contains(photo.getPath())) {
                currentExcludedPaths.add(photo.getPath());
            }
        }

        Object[] currentExcludedPathObjects = currentExcludedPaths.toArray();
        String[] currentExcludedPathStrings = Arrays.copyOf(currentExcludedPathObjects, currentExcludedPathObjects.length, String[].class);
        writeExcludedPhotoArrayToDatabase(currentExcludedPathStrings, albumPath, albumId);
    }

    public String getCoverPathAlbum(String path, long id) {
        String s = null;
        SQLiteDatabase db = this.getWritableDatabase();

        String[] selection = new  String[] {ALBUM_COVER_PATH};

        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?",
                new String[]{ path, String.valueOf(id) }, null, null, null);
        if (cursor.moveToFirst())
            s = cursor.getString(0);

        cursor.close();
        db.close();
        return s;
    }

    void setAlbumPhotoPreview(String path, long id, String mediaPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_COVER_PATH, mediaPath);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, id+"" });
        db.close();
    }

    void clearAlbumPreview(String path, long id) {
        setAlbumPhotoPreview(path, id, null);
    }

    void setAlbumSortingMode(String path, long id, int column) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_SORTING_MODE, column);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, String.valueOf(id) });
        db.close();
    }

    void setAlbumSortingOrder(String path, long id, int sortingOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_SORTING_ORDER, sortingOrder);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, String.valueOf(id) });
        db.close();
    }
}
