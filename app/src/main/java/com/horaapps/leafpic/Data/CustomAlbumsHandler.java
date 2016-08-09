package com.horaapps.leafpic.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

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

    public CustomAlbumsHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_ALBUMS + "(" +
                ALBUM_PATH + " TEXT," +
                ALBUM_ID + " INTEGER," +
                ALBUM_EXCLUDED + " INTEGER," +
                ALBUM_COVER + " TEXT, " +
                ALBUM_DEFAULT_SORTMODE + " INTEGER, " +
                ALBUM_DEFAULT_SORT_ASCENDING + " INTEGER, " +
                ALBUM_COLUMN_COUNT + " TEXT)");

        // NOTE: excluded music folder by default
        ContentValues values = new ContentValues();
        values.put(ALBUM_ID, -1);
        values.put(ALBUM_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        values.put(ALBUM_EXCLUDED, 1);

        db.insert(TABLE_ALBUMS, null, values);
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
            values.put(ALBUM_PATH, StringUtils.quoteReplace(path));
            values.put(ALBUM_EXCLUDED, 0);
            db.insert(TABLE_ALBUMS, null, values);
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

    public ArrayList<File> getExcludedFolders(boolean mediaStore) {
        ArrayList<File> list = new ArrayList<File>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = mediaStore ? ALBUM_EXCLUDED + "=1 AND "+ ALBUM_ID+"!=-1" : ALBUM_EXCLUDED + "=1 AND "+ ALBUM_ID+"=-1";
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ ALBUM_PATH }, selection, null, null, null, null);
        if (cur.moveToFirst())
            do list.add(new File(cur.getString(0))); while (cur.moveToNext());
        cur.close();
        db.close();
        return list;
    }

    public ArrayList<Long> getExcludedFolderIds() {
        ArrayList<Long> list = new ArrayList<Long>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ALBUM_ID}, ALBUM_EXCLUDED + "=1 AND "+ ALBUM_ID+"!=-1", null, null, null, null);

        if (cur.moveToFirst())
            do list.add(cur.getLong(0)); while (cur.moveToNext());

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
        values.put(ALBUM_DEFAULT_SORTMODE, asc ? 1 : 0);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=? AND "+ALBUM_ID+"=?", new String[]{ path, String.valueOf(id) });
        db.close();
    }
}
