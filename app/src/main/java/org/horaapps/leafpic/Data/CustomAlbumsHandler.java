package org.horaapps.leafpic.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.horaapps.leafpic.util.PermissionUtils;
import org.horaapps.leafpic.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dnld on 2/1/16.
 */


public class CustomAlbumsHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "CustomAlbums";

    private static final String TABLE_ALBUMS = "albums";
    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_ID = "id";
    private static final String ALBUM_EXCLUDED = "excluded";
    private static final String ALBUM_COVER = "cover_path";
    private static final String ALBUM_DEFAULT_SORTMODE = "sort_mode";
    private static final String ALBUM_DEFAULT_SORT_ASCENDING = "sort_ascending";
    private static final String ALBUM_COLUMN_COUNT = "column_count";

    private static final String TABLE_MEDIA = "media";
    private static final String EXCLUDED_MEDIA = "excluded";

    public CustomAlbumsHandler(Context ctx) {
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

    void createAlbumsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_ALBUMS + "(" +
                ALBUM_PATH + " TEXT," +
                ALBUM_ID + " INTEGER," +
                ALBUM_EXCLUDED + " INTEGER," +
                ALBUM_COVER + " TEXT, " +
                ALBUM_DEFAULT_SORTMODE + " INTEGER, " +
                ALBUM_DEFAULT_SORT_ASCENDING + " INTEGER, " +
                ALBUM_COLUMN_COUNT + " TEXT)");
    }

    void createMediaTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_MEDIA + "(" +
                ALBUM_ID + " INTEGER," +
                ALBUM_PATH + " TEXT," +
                EXCLUDED_MEDIA + " TEXT," +
                "PRIMARY KEY (" + ALBUM_ID + "))");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        onCreate(db);
    }

    private void checkAndCreateAlbum(String path, long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_ALBUMS, null,  ALBUM_PATH+"=? AND "+ALBUM_ID+"=?",
                new String[]{ path, String.valueOf(id) }, null, null, null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_ID, id);
            values.put(ALBUM_PATH, path);
            values.put(ALBUM_EXCLUDED, 0);
            db.insert(TABLE_ALBUMS, null, values);
        }
        cursor.close();

        // Check to see if the media table entry exists
        cursor = null;
        try {
            cursor = db.query(TABLE_MEDIA, null, ALBUM_PATH + "=? AND " + ALBUM_ID + "=?",
                    new String[]{path, String.valueOf(id)}, null, null, null);
        }
        catch (SQLiteException ex) {
            this.createMediaTable(db);

            cursor = db.query(TABLE_MEDIA, null, ALBUM_PATH + "=? AND " + ALBUM_ID + "=?",
                    new String[]{path, String.valueOf(id)}, null, null, null);
        }

        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_ID, id);
            values.put(ALBUM_PATH, path);
            values.put(EXCLUDED_MEDIA, new JSONArray().toString());
            db.insert(TABLE_MEDIA, null, values);
        }

        cursor.close();
        db.close();
    }

    AlbumSettings getSettings(String path, long id) {
        checkAndCreateAlbum(path, id);
        AlbumSettings s = new AlbumSettings();
        SQLiteDatabase db = this.getWritableDatabase();

        String[] selection = new  String[] { ALBUM_COVER, ALBUM_DEFAULT_SORTMODE, ALBUM_DEFAULT_SORT_ASCENDING};
        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?",
                new String[]{ path, String.valueOf(id) }, null, null, null);

        if (cursor.moveToFirst())
            s = new AlbumSettings(cursor.getString(0), cursor.getInt(1), cursor.getInt(2) == 1);
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
        checkAndCreateAlbum(path, id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_EXCLUDED, 1);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, id+"" });
        db.close();
    }

    public void unexcludePhoto(String photoPath, String albumPath, long albumId) {
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

                    } catch (JSONException e) {
                        continue;
                    }
                }
            } catch (JSONException e) {
                continue;
            }

            cursor.moveToNext();
        }
        return mediaList;
    }

    public Set<String> getExcludedPhotos(String albumPath, long albumId) {
        checkAndCreateAlbum(albumPath, albumId);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_MEDIA, new String[] {EXCLUDED_MEDIA},
                ALBUM_PATH + "=? AND " + ALBUM_ID + "=?", new String[] {albumPath, String.valueOf(albumId)}, null, null, null);

        Set<String> excludedPhotos = new HashSet<String>();
        if (cursor.moveToFirst()) {
            JSONArray excludedPhotoJsonBlob = null;
            try {
                excludedPhotoJsonBlob = new JSONArray(cursor.getString(0));
            } catch (JSONException e) {
                // Shouldnt happen -- ignore for now
            }

            if (excludedPhotoJsonBlob != null) {
                for (int i = 0; i < excludedPhotoJsonBlob.length(); i++)
                    try {
                        excludedPhotos.add(excludedPhotoJsonBlob.getString(i));
                    } catch (JSONException e) {
                        //Ignore for now
                    }
            }
        }

        cursor.close();
        db.close();
        return excludedPhotos;
    }

    public void writeExcludedPhotoArrayToDatabase(String[] excludedPhotoPaths, String albumPath, long albumId) {
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

    public void excludePhoto(Media photo, long albumId, String albumPath) {
        List<Media> singlePhotoList = new ArrayList<Media>();
        singlePhotoList.add(photo);

        this.excludePhotos(singlePhotoList, albumId, albumPath);
    }

    public void excludePhotos(List<Media> photos, long albumId, String albumPath) {
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

        String[] selection = new  String[] { ALBUM_COVER };

        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?",
                new String[]{ path, String.valueOf(id) }, null, null, null);
        if (cursor.moveToFirst())
            s = cursor.getString(0);

        cursor.close();
        db.close();
        return s;
    }

    void setAlbumPhotoPreview(String path, long id, String mediaPath) {
        checkAndCreateAlbum(path, id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_COVER, mediaPath);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, id+"" });
        db.close();
    }

    public void clearAlbumPreview(String path, long id) {
        setAlbumPhotoPreview(path, id, null);
    }

    void setAlbumSortingMode(String path, long id, int column) {
        checkAndCreateAlbum(path, id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_DEFAULT_SORTMODE, column);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, String.valueOf(id) });
        db.close();
    }

    void setAlbumSortingAscending(String path, long id, boolean asc) {
        checkAndCreateAlbum(path, id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_DEFAULT_SORT_ASCENDING, asc ? 1 : 0);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, String.valueOf(id) });
        db.close();
    }
}
