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
    private static final String TABLE_ALBUMS = "Album";
    private static final String ALBUM_PATH = "path";
    private static final String ALBUM_NAME = "name";
    private static final String ALBUM_HIDDEN = "hidden";
    private static final String ALBUM_EXCULDED = "excluded";

    private static final String TABLE_PHOTOS = "Photo";
    private static final String PHOTO_PATH = "path";
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

        String CREATE_PHOTOS_TABLE = "CREATE TABLE " + TABLE_PHOTOS + "("
                + PHOTO_PATH + " TEXT," + PHOTO_FOLDER_PATH + " TEXT, " + PHOTO_DATE_TAKEN + " TEXT, " + PHOTO_HIDDEN
                + " TEXT, " + PHOTO_EXCULDED + " TEXT)";

        db.execSQL(CREATE_PHOTOS_TABLE);
        db.execSQL(CREATE_ALBUMS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        onCreate(db);
    }

    /***
     * Normal Albums
     ***/

    public ArrayList<Album> getAllAlbums() {
        ArrayList<Album> contactList = new ArrayList<Album>();
        String selectQuery = "SELECT  * FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_HIDDEN + "='false' AND " +
                ALBUM_EXCULDED + "='false'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do contactList.add(new Album(
                    string.quoteReverse(cursor.getString(0)),
                    string.quoteReverse(cursor.getString(1)),
                    Boolean.getBoolean(cursor.getString(2))));
            while (cursor.moveToNext());
        }
        return contactList;
    }

    void deletePhotoByPath(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTOS, PHOTO_PATH + " = ?",
                new String[]{string.quoteReplace(path)});
        db.close();
    }

    public void deleteAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + string.quoteReplace(path) + "'";
        db.execSQL(sql);
       /* sql = "DELETE FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + path + "'";
        db.execSQL(sql);*/
        db.close();
    }

    public void hideAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_HIDDEN + "='true' WHERE " + ALBUM_PATH + "='" + string.quoteReplace(path) + "'";
        db.execSQL(sql);
        db.close();
    }

    public void unHideAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_HIDDEN + " = 'false' WHERE " + ALBUM_PATH + " ='" + string.quoteReplace(path)
                + "'";
        db.execSQL(sql);
        db.close();
    }

    public void checkalbum(String path) {
        String countQuery = "SELECT  * FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + string.quoteReplace(path)
                + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor.getCount() == 0)
            addAlbum(new Album(path));
        cursor.close();
    }

    public void updatePhotos() {
        int dbc = getDataBasePhotosCount(), mdc = getMediastorePhotosCount();
        if (dbc != mdc) {
            if (dbc < mdc) { //added Photo
                try {
                    loadPhotosFrom(getDataBasePhotosCount());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                checkDeletedPhotos();
            }
        }
        Log();
    }

    public ArrayList<String> getAllPhotosPaths() {
        ArrayList<String> contactList = new ArrayList<String>();
        String selectQuery = "SELECT " + PHOTO_PATH + " FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_HIDDEN + "='false'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
            do contactList.add(string.quoteReverse(cursor.getString(0)));
            while (cursor.moveToNext());

        return contactList;
    }

    public void checkDeletedPhotos() {
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);
        ArrayList<String> photoPaths = new ArrayList<String>();

        if (cur.moveToFirst()) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            do {
                photoPaths.add(cur.getString(pathColumn));
            } while (cur.moveToNext());
        }

        ArrayList<String> dbPhotoPaths = getAllPhotosPaths();
        for (String photoPath : photoPaths)
            dbPhotoPaths.remove(photoPath);


        for (String dbPhotoPath : dbPhotoPaths) {
            deletePhotoByPath(dbPhotoPath);
            String path = string.getBucketPathbyImagePath(dbPhotoPath);

            if (getDBPhotosCountByAlbum(path) == 0)
                deleteAlbum(path);
        }

    }

    public int getDBPhotosCountByAlbum(String path) {
        String countQuery = "SELECT  * FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + string
                .quoteReplace(path) + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void loadPhotosFrom(int i) {
        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);

        if (cur.moveToPosition(i)) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int DateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);
            do {
                Photo f = new Photo(
                        string.quoteReverse(cur.getString(pathColumn)),
                        cur.getString(DateColumn));
                addPhoto(f);
                checkalbum(f.FolderPath);
            } while (cur.moveToNext());
        }
    }

    public void loadPhots() throws Exception {
        loadPhotosFrom(0);
    }

    public int getDataBasePhotosCount() {
        int count;
        String countQuery = "SELECT  * FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_HIDDEN + "='false'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    /***
     * Hidden Albums
     ***/

    void addPhoto(Photo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PHOTO_FOLDER_PATH, string.quoteReplace(contact.FolderPath));
        values.put(PHOTO_PATH, string.quoteReplace(contact.Path));
        values.put(PHOTO_DATE_TAKEN, contact.DateTaken);
        values.put(PHOTO_HIDDEN, "false");
        values.put(PHOTO_EXCULDED, "false");
        db.insert(TABLE_PHOTOS, null, values);
        db.close();
    }

    void addAlbum(Album contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALBUM_NAME, string.quoteReplace(contact.DisplayName));
        values.put(ALBUM_PATH, string.quoteReplace(contact.Path));
        values.put(ALBUM_EXCULDED, "false");
        values.put(ALBUM_HIDDEN, "false");
        db.insert(TABLE_ALBUMS, null, values);
        db.close();
    }

    void addHiddenPhoto(Photo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PHOTO_FOLDER_PATH, string.quoteReplace(contact.FolderPath));
        values.put(PHOTO_PATH, string.quoteReplace(contact.Path));
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
        if (dir.isDirectory() && !dir.getAbsolutePath().equals("/storage/emulated/0/Android")) {
            Log.i("storiage", dir.getAbsolutePath());
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File temp = new File(dir, children[i]);
                if (temp.isDirectory()) {
                    ArrayList<String> paths = gethiddenImagesFromFolder(temp);
                    if (paths.size() > 0)
                        for (String path : paths) {
                            Photo f = new Photo(path);
                            checkHiddenAlbum(temp.getAbsolutePath());
                            addHiddenPhoto(f);
                        }
                    getHiddenAlbums(temp);
                }
            }
        }
    }

    private ArrayList<String> gethiddenImagesFromFolder(File dir) {
        boolean matchFoltder = false;
        ArrayList<String> paths = new ArrayList<String>();
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            File temp = new File(dir, children[i]);
            String mime = string.getMimeType(temp.getAbsolutePath());
            if (mime != null && mime.contains("image"))
                paths.add(temp.getAbsolutePath());
            if (temp.getAbsolutePath().contains(".nomedia"))
                matchFoltder = true;
        }
        if (!matchFoltder)
            paths = new ArrayList<String>();
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
                    true));
            while (cursor.moveToNext());
        }
        return contactList;
    }

    public int getDataBaseHiddenPhotosCount() {
        int count;
        String countQuery = "SELECT  * FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_HIDDEN + "='true'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    /***
     * albums utils functions
     ***/

    void excludeAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_EXCULDED + " = 'true' WHERE " + ALBUM_PATH + " ='" +
                string.quoteReplace(path) + "'";
        db.execSQL(sql);
        db.close();
    }

    public ArrayList<Photo> getPhotosByAlbum(String path) {
        ArrayList<Photo> contactList = new ArrayList<Photo>();
        String selectQuery = "SELECT  " + PHOTO_PATH + ", " + PHOTO_DATE_TAKEN + " FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_FOLDER_PATH +
                "='" +
                string
                        .quoteReplace(path) + "' ORDER BY " + PHOTO_DATE_TAKEN + " DESC";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                contactList.add(new Photo(
                        string.quoteReverse(cursor.getString(0)),
                        cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        return contactList;
    }

    public int getMediastorePhotosCount() {
        int count;
        String[] projection = new String[]{MediaStore.Images.Media._ID};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);
        count = cur.getCount();
        cur.close();
        return count;
    }

    public void Log() {
        /*ArrayList<String> a = getAllPhotosPaths();
        for (String asd : a) {
            Log.i("asdfghj", asd);
        }*/
        ArrayList<Album> dispAlbums = getAllAlbums();
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.photos = getPhotosByAlbum(dispAlbum.DisplayName);
        }
        Log.i("numero album", "" + dispAlbums.size());
        for (int i = 0; i < dispAlbums.size(); i++) {
            Log.i("album", dispAlbums.get(i).DisplayName + " " + dispAlbums.get(i).getImagesCount());
            for (Photo photo : dispAlbums.get(i).photos) {
                Log.i("foto", "PHOTO_FOLDER_PATH->>" + photo.FolderPath);
                Log.i("foto", "PHOTO_PATH->>" + photo.Path);
            }
        }
    }
}