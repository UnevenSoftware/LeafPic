package com.horaapps.leafpic.Base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 2/1/16.
 */


public class CustomAlbumsHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "CustomAlbums";

    private static final String TABLE_ALBUMS = "albums";
    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_EXCLUDED = "excluded";
    private static final String ALBUM_COVER = "cover_path";
    private static final String ALBUM_DEAFAULT_SORTMODE = "sort_mode";
    private static final String ALBUM_DEAFAULT_SORT_ASCENDING = "sort_ascending";
    private static final String ALBUM_COLUMN_COUNT = "column_count";

    public CustomAlbumsHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_ALBUMS + "(" +
                ALBUM_PATH + " TEXT," +
                ALBUM_EXCLUDED + " TEXT," +
                ALBUM_COVER + " TEXT, " +
                ALBUM_DEAFAULT_SORTMODE + " TEXT, " +
                ALBUM_DEAFAULT_SORT_ASCENDING + " BOOLEAN, " +
                ALBUM_COLUMN_COUNT + " TEXT)");

        db.execSQL("INSERT INTO "+TABLE_ALBUMS+" ("+ALBUM_PATH+","+ALBUM_EXCLUDED+")" +
                "VALUES('"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android"+"', 'true')");
        db.execSQL("INSERT INTO "+TABLE_ALBUMS+" ("+ALBUM_PATH+","+ALBUM_EXCLUDED+")" +
                "VALUES('"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_MUSIC+"', 'true')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        onCreate(db);
    }

    public ArrayList<File>  getExcludedFolders() {
        ArrayList<File> list = new ArrayList<File>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ ALBUM_PATH }, ALBUM_EXCLUDED + "='true'", null, null, null, null);

        if (cur.moveToFirst())
            do list.add(new File(cur.getString(0)));
            while (cur.moveToNext());

        cur.close();
        db.close();
        return list;
    }

    public void clearAlbumExclude(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_EXCLUDED + "='false' WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void excludeAlbum(String path) {
        checkAndCreateAlbum(path);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_EXCLUDED + "='true' WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(path) + "'");
        db.close();
    }

    public AlbumSettings getSettings(String id) {
        checkAndCreateAlbum(id);
        AlbumSettings s = new AlbumSettings();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ALBUM_COVER + ", " + ALBUM_DEAFAULT_SORTMODE + ", " + ALBUM_DEAFAULT_SORT_ASCENDING + " FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id) + "'", null);
        if (cursor.moveToFirst())
            s = new AlbumSettings(cursor.getString(0), cursor.getString(1), Boolean.valueOf(cursor.getString(2)));
        cursor.close();
        db.close();
        return s;
    }

    public void checkAndCreateAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ALBUM_PATH + " FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(path) + "'", null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_PATH, StringUtils.quoteReplace(path));
            values.put(ALBUM_EXCLUDED,"false");
            db.insert(TABLE_ALBUMS, null, values);
        }
        cursor.close();
        db.close();
    }

    public String getPhotPrevieAlbum(String id) {
        String s = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ALBUM_COVER + " FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id)
                + "'", null);
        if (cursor.moveToFirst())
            s = cursor.getString(0);

        cursor.close();
        db.close();
        return s;
    }

    public void setAlbumPhotPreview(String id, String path) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_COVER + "='" + path + "' WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void clearAlbumPreview(String id) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_COVER + "=NULL WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void setAlbumSortingMode(String id, String column) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_DEAFAULT_SORTMODE + "='" + column +
                "' WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void setAlbumSortingAscending(String id, Boolean asc) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_DEAFAULT_SORT_ASCENDING + "='" + String.valueOf(asc) +
                "' WHERE " + ALBUM_PATH + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }
}
