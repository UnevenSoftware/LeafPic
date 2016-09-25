package org.horaapps.leafpic.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.horaapps.leafpic.data.base.SortingMode;
import org.horaapps.leafpic.data.base.SortingOrder;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 2/1/16.
 */


public class CustomAlbumsHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 16;
    private static final String DATABASE_NAME = "album_settings.db";

    private static final String TABLE_ALBUMS = "albums";
    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_EXCLUDED = "excluded";
    private static final String ALBUM_PINNED = "pinned";
    private static final String ALBUM_COVER_PATH = "cover_path";
    private static final String ALBUM_SORTING_MODE = "sorting_mode";
    private static final String ALBUM_SORTING_ORDER = "sort_ascending";
    private static final String ALBUM_COLUMN_COUNT = "sorting_order";

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

        // NOTE: excluded music folder by default
        ContentValues values = new ContentValues();
        values.put(ALBUM_PATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        values.put(ALBUM_EXCLUDED, 1);

        db.insert(TABLE_ALBUMS, null, values);
    }

    private void createAlbumsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                           TABLE_ALBUMS + "(" +
                           ALBUM_PATH + " TEXT," +
                           ALBUM_EXCLUDED + " INTEGER," +
                           ALBUM_PINNED + " INTEGER," +
                           ALBUM_COVER_PATH + " TEXT, " +
                           ALBUM_SORTING_MODE + " INTEGER, " +
                           ALBUM_SORTING_ORDER + " INTEGER, " +
                           ALBUM_COLUMN_COUNT + " TEXT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        onCreate(db);
    }

    private void checkAndCreateAlbum(SQLiteDatabase db, String path) {

        Cursor cursor = db.query(TABLE_ALBUMS, null,  ALBUM_PATH+"=?",
                new String[]{ path }, null, null, null);

        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(ALBUM_PATH, path);
            values.put(ALBUM_SORTING_MODE, SortingMode.DATE.getValue());
            values.put(ALBUM_SORTING_ORDER, SortingOrder.DESCENDING.getValue());
            values.put(ALBUM_EXCLUDED, 0);
            db.insert(TABLE_ALBUMS, null, values);
        }

        cursor.close();
    }

    AlbumSettings getSettings(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, path);

        AlbumSettings s = null;

        String[] selection = new  String[] { ALBUM_COVER_PATH, ALBUM_SORTING_MODE,
                ALBUM_SORTING_ORDER, ALBUM_PINNED };
        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=?",
                new String[]{ path }, null, null, null);

        if (cursor.moveToFirst())
            s = new AlbumSettings(path, cursor.getString(0), cursor.getInt(1), cursor.getInt(2),cursor.getInt(3));
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

    public void excludeAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, path);
        ContentValues values = new ContentValues();
        values.put(ALBUM_EXCLUDED, 1);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path});
        db.close();
    }

    void pinAlbum(String path, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        checkAndCreateAlbum(db, path);
        ContentValues values = new ContentValues();
        values.put(ALBUM_PINNED, status ? 1 : 0);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path });
        db.close();
    }

    public String getCoverPathAlbum(String path) {
        String s = null;
        SQLiteDatabase db = this.getWritableDatabase();

        String[] selection = new  String[] {ALBUM_COVER_PATH};

        Cursor cursor = db.query(TABLE_ALBUMS, selection, ALBUM_PATH+"=?",
                new String[]{ path }, null, null, null);
        if (cursor.moveToFirst())
            s = cursor.getString(0);

        cursor.close();
        db.close();
        return s;
    }

    void setAlbumPhotoPreview(String path, String mediaPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_COVER_PATH, mediaPath);
        db.update(TABLE_ALBUMS, values, ALBUM_PATH+"=?", new String[]{ path});
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
}
