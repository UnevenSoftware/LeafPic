package com.leafpic.app.Base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import com.leafpic.app.utils.string;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
public class HiddenPhotosHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LeafPic";

    private static final String TABLE_PHOTOS = "photo";
    private static final String PHOTO_PATH = "path";
    private static final String PHOTO_MIME = "mime";
    private static final String PHOTO_FOLDER_PATH = "folderpath";
    private static final String PHOTO_DATE_TAKEN = "datetaken";

    Context context;

    public HiddenPhotosHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " +
                TABLE_PHOTOS + "(" +
                PHOTO_PATH + " TEXT," +
                PHOTO_MIME + " TEXT," +
                PHOTO_FOLDER_PATH + " TEXT, " +
                PHOTO_DATE_TAKEN + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        onCreate(db);
    }

    public void loadHiddenALbums() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PHOTOS);
        db.close();
        getAlbums(Environment.getExternalStorageDirectory());
    }

    public void deleteAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + string.quoteReplace(path) + "'");
        db.close();

    }

    void getAlbums(File dir) {
        if (dir.isDirectory() &&
                !dir.getAbsolutePath().equals("/storage/emulated/0/Android") &&
                !dir.getAbsolutePath().contains("Voice") &&
                !dir.getAbsolutePath().contains("Audio")) {

            String[] children = dir.list();
            for (String child : children) {
                File temp = new File(dir, child);
                if (temp.isDirectory()) {
                    ArrayList<Photo> paths = getImagesFromFolder(temp);
                    for (Photo path : paths)
                        addPhoto(path);
                    getAlbums(temp);
                }
            }
        }
    }

    private ArrayList<Photo> getImagesFromFolder(File dir) {
        ArrayList<Photo> paths = new ArrayList<Photo>();
        String[] children = dir.list();
        File nomediafile = new File(dir, ".nomedia");
        if (!nomediafile.exists())
            return paths;

        for (String child : children) {
            File temp = new File(dir, child);

            String mime = string.getMimeType(temp.getAbsolutePath());
            if (mime != null && mime.contains("image"))
                paths.add(new Photo(
                        temp.getAbsolutePath(),
                        String.valueOf(temp.lastModified()),
                        mime, dir.getAbsolutePath()));
        }
        return paths;
    }

    void addPhoto(Photo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PHOTO_FOLDER_PATH, string.quoteReplace(contact.FolderPath));
        values.put(PHOTO_PATH, string.quoteReplace(contact.Path));
        values.put(PHOTO_MIME, contact.MIME);
        db.insert(TABLE_PHOTOS, null, values);
        db.close();
    }

    public ArrayList<Album> getAlbums() {
        ArrayList<Album> contactList = new ArrayList<Album>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PHOTO_FOLDER_PATH + " FROM " + TABLE_PHOTOS + " WHERE " +
                        PHOTO_FOLDER_PATH + "=" + PHOTO_FOLDER_PATH + " GROUP BY (" + PHOTO_FOLDER_PATH + ");",
                null);
        if (cursor.moveToFirst()) {
            do contactList.add(new Album(
                    string.quoteReverse(cursor.getString(0)),
                    string.getBucketNamebyImagePath(string.quoteReverse(cursor.getString(0))),
                    true, getHiddenPhotosCountByAlbum(cursor.getString(0))));
            while (cursor.moveToNext());
        }
        cursor.close();
        return contactList;
    }

    public ArrayList<Photo> getPhotosByAlbum(String path) {
        ArrayList<Photo> contactList = new ArrayList<Photo>();
        String selectQuery = "SELECT  " + PHOTO_PATH + ", " + PHOTO_FOLDER_PATH + ", " +
                PHOTO_MIME + ", " + PHOTO_DATE_TAKEN +
                " FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_FOLDER_PATH + "='" +
                string.quoteReplace(path) + "' ORDER BY " + PHOTO_DATE_TAKEN + " DESC";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                contactList.add(new Photo(
                        string.quoteReverse(cursor.getString(0)),
                        cursor.getString(3),
                        cursor.getString(2),
                        cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contactList;
    }

    public ArrayList<Photo> getFirstPhotosByAlbum(String path) {
        ArrayList<Photo> contactList = new ArrayList<Photo>();
        String selectQuery = "SELECT  " + PHOTO_PATH + ", " + PHOTO_DATE_TAKEN + " FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_FOLDER_PATH + "='" + string.quoteReplace(path) + "' ORDER BY " + PHOTO_DATE_TAKEN + " DESC";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
            contactList.add(new Photo(
                    string.quoteReverse(cursor.getString(0)),
                    cursor.getString(1)));

        cursor.close();
        return contactList;
    }

    public int getHiddenPhotosCountByAlbum(String path) {
        int count;
        String countQuery = "SELECT  * FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + path + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getPhotosCount() {
        int count;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  * FROM " + TABLE_PHOTOS, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }
}