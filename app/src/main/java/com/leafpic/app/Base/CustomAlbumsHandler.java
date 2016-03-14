package com.leafpic.app.Base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.leafpic.app.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by dnld on 2/1/16.
 */


public class CustomAlbumsHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CustomAlbums";

    private static final String TABLE_ALBUMS = "albums";
    private static final String ALBUM_ID = "_id";
    private static final String ALBUM_EXCLUDED = "excluded";
    private static final String ALBUM_COVER = "cover_path";
    private static final String ALBUM_DEAFAULT_SORTMODE = "sort_mode";
    private static final String ALBUM_DEAFAULT_SORT_ASCENDING = "sort_ascending";
    private static final String ALBUM_COLUMN_COUNT = "column_count";

    Context context;

    public CustomAlbumsHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        context = ctx;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE_ALBUMS + "(" +
                ALBUM_ID + " TEXT," +
                ALBUM_EXCLUDED + " BOOLEAN," +
                ALBUM_COVER + " TEXT, " +
                ALBUM_DEAFAULT_SORTMODE + " TEXT, " +
                ALBUM_DEAFAULT_SORT_ASCENDING + " BOOLEAN, " +
                ALBUM_COLUMN_COUNT + " TEXT)");
    }

    public AlbumSettings getSettings(String id) {
        checkAndCreateAlbum(id);
        AlbumSettings s = new AlbumSettings();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ALBUM_COVER + ", " + ALBUM_DEAFAULT_SORTMODE + ", " + ALBUM_DEAFAULT_SORT_ASCENDING + " FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'", null);
        if (cursor.moveToFirst())
            s = new AlbumSettings(cursor.getString(0), cursor.getString(1), Boolean.valueOf(cursor.getString(2)));
        cursor.close();
        db.close();
        return s;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        onCreate(db);
    }

    public void checkAndCreateAlbum(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ALBUM_ID + " FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'", null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_ID, StringUtils.quoteReplace(id));
            db.insert(TABLE_ALBUMS, null, values);
        }
        cursor.close();
        db.close();
    }

    public void excludeAlbum(String id) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_EXCLUDED + "='true' WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public String getPhotPrevieAlbum(String id) {
        String s = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ALBUM_COVER + " FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id)
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
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_COVER + "='" + path + "' WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void clearAlbumPreview(String id) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_COVER + "=NULL WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void setAlbumSortingMode(String id, String column) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_DEAFAULT_SORTMODE + "='" + column +
                "' WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public void setAlbumSortingAscending(String id, Boolean asc) {
        checkAndCreateAlbum(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_DEAFAULT_SORT_ASCENDING + "='" + String.valueOf(asc) +
                "' WHERE " + ALBUM_ID + "='" + StringUtils.quoteReplace(id) + "'");
        db.close();
    }

    public ArrayList<String> getExcludedALbumsIDs() {
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(TABLE_ALBUMS, new String[]{ALBUM_ID}, ALBUM_EXCLUDED + "='true'", null, null,
                null, null);

        if (cur.moveToFirst())
            do list.add(cur.getString(0));
            while (cur.moveToNext());

        cur.close();
        db.close();
        return list;
    }

    public void LogEXCLUDEALBUMS() {
        for (String s : getExcludedALbumsIDs()) {
            Log.wtf("dsfsdf", s);
        }
    }

}
