package com.leafpic.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.leafpic.app.utils.string;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
class DatabaseHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "LeafPic";
    // Contacts table name
    private static final String TABLE_ALBUMS = "albums";
    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_NAME = "name";
    private static final String ALBUM_HIDDEN = "hidden";
    private static final String ALBUM_EXCULDED = "excluded";

    private static final String TABLE_PHOTOS = "photo";
    private static final String PHOTO_PATH = "path";
    private static final String PHOTO_MIME = "mime";
    private static final String PHOTO_FOLDER_PATH = "folderpath";
    private static final String PHOTO_DATE_TAKEN = "datetaken";
    private static final String PHOTO_EXCULDED = "excluded";
    private static final String PHOTO_HIDDEN = "hidden";
    Context context;

    public DatabaseHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        context = ctx;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ALBUMS_TABLE = "CREATE TABLE " + TABLE_ALBUMS + "("
                + ALBUM_PATH + " TEXT," + ALBUM_NAME + " TEXT, " + ALBUM_HIDDEN + " TEXT, " + ALBUM_EXCULDED + " TEXT)";

        String CREATE_PHOTOS_TABLE = "CREATE TABLE " + TABLE_PHOTOS + "(" +
                PHOTO_PATH + " TEXT," +
                PHOTO_MIME + " TEXT," +
                PHOTO_FOLDER_PATH + " TEXT, " +
                PHOTO_DATE_TAKEN + " TEXT, " +
                PHOTO_HIDDEN + " TEXT, " +
                PHOTO_EXCULDED + " TEXT)";

        db.execSQL(CREATE_PHOTOS_TABLE);
        db.execSQL(CREATE_ALBUMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        onCreate(db);
    }


    void addHiddenPhoto(Photo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PHOTO_FOLDER_PATH, string.quoteReplace(contact.FolderPath));
        values.put(PHOTO_PATH, string.quoteReplace(contact.name));
        values.put(PHOTO_HIDDEN, "true");
        values.put(PHOTO_EXCULDED, "false");
        db.insert(TABLE_PHOTOS, null, values);
        db.close();
    }

    void addHiddenAlbum(String Path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_NAME, string.quoteReplace(string.getBucketNamebyBucketPath(Path)));
        values.put(ALBUM_PATH, string.quoteReplace(Path));
        values.put(ALBUM_HIDDEN, "true");
        values.put(ALBUM_EXCULDED, "false");
        db.insert(TABLE_ALBUMS, null, values);
        db.close();
    }

    void loadHiddenALbums() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_HIDDEN + "='true'");
        db.execSQL("DELETE FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_HIDDEN + "='true'");
        db.close();
        File root = Environment.getExternalStorageDirectory();
        getHiddenAlbums(root);
    }

    public void getHiddenAlbums(File dir) {
        if (dir.isDirectory() &&
                !dir.getAbsolutePath().equals("/storage/emulated/0/Android") &&
                !dir.getAbsolutePath().contains("Voice") &&
                !dir.getAbsolutePath().contains("Audio")) {

            String[] children = dir.list();
            for (String child : children) {
                File temp = new File(dir, child);
                if (temp.isDirectory()) {
                    ArrayList<Photo> paths = gethiddenImagesFromFolder(temp);
                    if (paths.size() > 0)
                        for (Photo path : paths) {
                            checkHiddenAlbum(temp.getAbsolutePath());
                            addHiddenPhoto(path);
                        }
                    getHiddenAlbums(temp);
                }
            }
        }
    }

    private ArrayList<Photo> gethiddenImagesFromFolder(File dir) {
        ArrayList<Photo> paths = new ArrayList<Photo>();
        String[] children = dir.list();
        File nomediafile = new File(dir, ".nomedia");
        if (!nomediafile.exists())
            return paths;

        for (String child : children) {
            File temp = new File(dir, child);
            String mime = string.getMimeType(temp.getAbsolutePath());
            if (mime != null && mime.contains("image"))
                paths.add(new Photo(temp.getAbsolutePath(), String.valueOf(temp.lastModified())));
        }
        return paths;
    }

    public void checkHiddenAlbum(String Path) {
        String countQuery = "SELECT  * FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + string.quoteReplace(Path) + "'" +
                " AND " + ALBUM_HIDDEN + "='true'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor.getCount() == 0)
            addHiddenAlbum(Path);
        cursor.close();
    }

    public ArrayList<Album> getHiddenAlbums() {
        ArrayList<Album> contactList = new ArrayList<Album>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_HIDDEN + "='true'", null);

        if (cursor.moveToFirst()) {
            do contactList.add(new Album(
                    string.quoteReverse(cursor.getString(0)),
                    string.quoteReverse(cursor.getString(1)),
                    true, getHiddenPhotosCount(cursor.getString(0))));
            while (cursor.moveToNext());
        }
        return contactList;
    }

    public int getHiddenPhotosCount(String path) {
        int count;
        String countQuery = "SELECT  * FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_HIDDEN + "='true' AND " +
                "" + PHOTO_FOLDER_PATH + "='" + path + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    void excludeAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_EXCULDED + " = 'true' WHERE " + ALBUM_PATH + " ='" +
                string.quoteReplace(path) + "'";
        db.execSQL(sql);
        db.close();
    }


}


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