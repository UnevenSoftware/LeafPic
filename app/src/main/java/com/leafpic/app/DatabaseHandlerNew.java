package com.leafpic.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.leafpic.app.utils.string;

import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
class DatabaseHandlerNew extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LeafPic.db";
    private static final String TABLE_NAME = "photos";
    private static final String ID = "_id";
    private static final String PATH = "path";
    private static final String FOLDER_PATH = "folder_path";
    private static final String DATE_TAKEN = "date_taken";
    private static final String DATE_LAST_MODIFIED = "date_last_modified";
    private static final String SIZE = "size";
    private static final String MIME_TYPE = "mime_type";
    private static final String EXCULDED = "excluded";
    private static final String HIDDEN = "hidden";
    private final String TAG = "database_new";
    Context context;

    public DatabaseHandlerNew(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        context = ctx;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + "(" +
                        ID + " INTEGER," +
                        PATH + " TEXT, " +
                        FOLDER_PATH + " TEXT, " +
                        DATE_TAKEN + " INTEGER, " +
                        DATE_LAST_MODIFIED + " INTEGER, " +
                        SIZE + " INTEGER, " +
                        MIME_TYPE + " TEXT, " +
                        EXCULDED + " BOOLEAN, " +
                        HIDDEN + " BOOLEAN)");
    }
    //public void addPhoto()

    public void loadPhotos() {
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);

        if (cur.moveToFirst()) {
            int idColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
            int pathColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            int dateTakenColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int dateModifiedColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
            int sizeColumn = cur.getColumnIndex(MediaStore.Images.Media.SIZE);
            int mimeTypeColumn = cur.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

            do {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(ID, cur.getInt(idColumn));
                values.put(PATH, cur.getString(pathColumn));
                values.put(FOLDER_PATH, string.getBucketPathbyImagePath(cur.getString(pathColumn)));
                values.put(DATE_TAKEN, cur.getInt(dateTakenColumn));
                values.put(DATE_LAST_MODIFIED, cur.getInt(dateModifiedColumn));
                values.put(SIZE, cur.getInt(sizeColumn));
                values.put(MIME_TYPE, cur.getString(mimeTypeColumn));
                values.put(HIDDEN, false);
                values.put(EXCULDED, false);
                db.insert(TABLE_NAME, null, values);
                db.close();
            } while (cur.moveToNext());
        }
        cur.close();
    }

    public void logPhotos() {
        String[] projection = new String[]{PATH, SIZE};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(false, TABLE_NAME, projection, null, null, null, null, null, null);

        if (cur.moveToFirst()) {
            do {
                Log.wtf("database_new", cur.getString(0) + " - " + cur.getString(1));
            } while (cur.moveToNext());
        }
        db.close();
    }

    public ArrayList<Album> getAlbums() {
        ArrayList<Album> list = new ArrayList<Album>();

        String[] projection = new String[]{FOLDER_PATH, HIDDEN};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(false, TABLE_NAME, projection, null, null, FOLDER_PATH, null, null, null);

        if (cur.moveToFirst()) {
            do
                list.add(new Album(cur.getString(0),
                        Boolean.getBoolean(cur.getString(1)),
                        getAlbumPhotosCount(cur.getString(0))));
            while (cur.moveToNext());
        }
        cur.close();
        db.close();

        return list;
    }

    public void logDeletedPhotos() {
        Log.wtf(TAG, "START DELETED PHOTOS");
        String[] projection = new String[]{MediaStore.Images.Media._ID};
        SQLiteDatabase db = this.getWritableDatabase();

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);

        db.execSQL("DROP TABLE IF EXISTS all_ids");
        db.execSQL("CREATE TABLE all_ids(" + ID + " INTEGER)");
        if (cur.moveToFirst()) {
            int idColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
            do {
                ContentValues values = new ContentValues();
                values.put(ID, cur.getInt(idColumn));
                db.insert("all_ids", null, values);
            } while (cur.moveToNext());
        }
        cur.close();

        String sql = " SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + ID + " NOT IN (SELECT * FROM all_ids)";
        cur = db.rawQuery(sql, null);

        Log.wtf(TAG, cur.getCount() + "");
        cur.close();
        db.close();

        //Cursor cur = context.getContentResolver().query(images, projection, null, null, null);


    }

    public ArrayList<Photo> getAlbumPhotos(String path) {
        String[] projection = new String[]{PATH, DATE_TAKEN};
        ArrayList<Photo> list = new ArrayList<Photo>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(false, TABLE_NAME, projection, FOLDER_PATH + "=?", new String[]{path}, null, null, null, null);

        if (cur.moveToFirst()) {
            do {
                list.add(new Photo(
                        cur.getString(0),
                        cur.getString(1)));
            } while (cur.moveToNext());
        }
        cur.close();
        db.close();
        return list;
    }

    public int getAlbumPhotosCount(String path) {
        int c;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(false, TABLE_NAME, null, FOLDER_PATH + "=?", new String[]{path}, null, null, null, null);
        c = cur.getCount();
        cur.close();
        db.close();
        return c;
    }


    public void logAlbums() {
        ArrayList<Album> dispAlbums = getAlbums();
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.photos = getAlbumPhotos(dispAlbum.Path);
            Log.wtf(TAG, dispAlbum.DisplayName + " - " + dispAlbum.getImagesCount());
            for (Photo photo : dispAlbum.photos) {
                Log.w(TAG, photo.Path);
            }
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}