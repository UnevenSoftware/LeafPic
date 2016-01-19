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
    private static final String PHOTO_NAME = "name";
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
                + PHOTO_NAME + " TEXT," + PHOTO_FOLDER_PATH + " TEXT, " + PHOTO_DATE_TAKEN + " TEXT, " + PHOTO_HIDDEN
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
    public Album getAlbum(String path) {
        String selectQuery = "SELECT  * FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + path + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Album a = new Album();
        if (cursor.moveToFirst()) {
            a = new Album(
                    string.quoteReverse(cursor.getString(0)),
                    string.quoteReverse(cursor.getString(1)),
                    Boolean.getBoolean(cursor.getString(2)));
            a.photos = getPhotosByAlbum(a.Path);
        }
        return a;
    }

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
                    Boolean.getBoolean(cursor.getString(2)),
                    getDBPhotosCountByAlbum(cursor.getString(0))));
            while (cursor.moveToNext());
        }
        return contactList;
    }

    void deletePhotoByPath(String path) {
        String s[] = string.getPhotoFolderPathAndNameByPath(path);
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + string.quoteReplace(s[0]) +
                "' AND " + PHOTO_NAME + "='" + string.quoteReplace(s[1]) + "'";
        db.execSQL(sql);
        db.close();
    }

    void deletePhoto(Photo f) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + string.quoteReplace(f.FolderPath) +
                "' AND " + PHOTO_NAME + "='" + string.quoteReplace(f.name) + "'";
        db.execSQL(sql);
        db.close();
    }

    void deletePhotoaByFolderPath(String path) {
        string.getPhotoFolderPathAndNameByPath(path);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTOS, PHOTO_FOLDER_PATH + " = ?",
                new String[]{string.quoteReplace(path)});
        db.close();
    }

    public void deleteAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_ALBUMS + " WHERE " + ALBUM_PATH + "='" + string.quoteReplace(path) + "'";
        db.execSQL(sql);
        /*sql = "DELETE FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + path + "'";
        db.execSQL(sql);*/
        db.close();
    }

    public void hideAlbum(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_HIDDEN + "='true' WHERE " + ALBUM_PATH + "='" + string.quoteReplace(path) + "'";
        db.execSQL(sql);
        db.close();
    }

    public void renameAlbum(String olderPath, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_NAME + "='" +
                string.quoteReplace(newName) + "' WHERE " + ALBUM_PATH + "='" +
                string.quoteReplace(olderPath) + "'";
        db.execSQL(sql);

        sql = "UPDATE " + TABLE_ALBUMS + " SET " + ALBUM_PATH + "='" +
                string.quoteReplace(string.getAlbumPathRenamed(olderPath, newName)) + "' WHERE " + ALBUM_PATH
                + "='" + string.quoteReplace(olderPath) + "'";
        db.execSQL(sql);

        sql = "UPDATE " + TABLE_PHOTOS + " SET " + PHOTO_FOLDER_PATH + "='" +
                string.quoteReplace(string.getAlbumPathRenamed(olderPath, newName)) + "' WHERE " + PHOTO_FOLDER_PATH
                + "='" + string.quoteReplace(olderPath) + "'";
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

        try {
            int dbc = getDataBasePhotosCount(), mdc = getMediastorePhotosCount();
            Log.i("asd_dasa", dbc + " - " + mdc);
            if (dbc != mdc) {
                if (dbc < mdc) { //added Photo
                    try {
                        loadPhotosFrom(dbc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    checkDeletedPhotos();
            } else {
                // checkDeletedPhotos();
                //checkMovedPhotos();
                //checkDeletedPhotos();
            }
            //  Log();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void checkMovedPhotos() {
        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);
        ArrayList<String> dbPhotoPaths = getAllPhotosPaths();

        if (cur.moveToFirst()) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int DateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);
            do {

                if (!checkPhoto(string.quoteReverse(cur.getString(pathColumn))))
                    addPhoto(new Photo(
                            string.quoteReverse(cur.getString(pathColumn)),
                            cur.getString(DateColumn)));
                else
                    dbPhotoPaths.remove(string.quoteReverse(cur.getString(pathColumn)));

                checkalbum(string.getBucketPathbyImagePath(string.quoteReverse(cur.getString(pathColumn))));
            } while (cur.moveToNext());

            for (String dbPhotoPath : dbPhotoPaths) {
                deletePhotoByPath(dbPhotoPath);
                String path = string.getBucketPathbyImagePath(dbPhotoPath);

                if (getDBPhotosCountByAlbum(path) == 0)
                    deleteAlbum(path);
            }
        }
    }

    public void loadPhotosFrom(int i) {
        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
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
        cur.close();
    }

    public void LogPhotosMediaStoreByFolderPath() {
        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?",
                new String[]{"Slide"}, null);


        if (cur.moveToFirst()) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int DateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);
            do {
                Log.i("folder_image", cur.getString(pathColumn));
            } while (cur.moveToNext());
        }
        cur.close();
    }

    public ArrayList<String> getAllPhotosPaths() {
        ArrayList<String> contactList = new ArrayList<String>();
        String selectQuery = "SELECT " + PHOTO_FOLDER_PATH + ", " + PHOTO_NAME + " FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_HIDDEN +
                "='false'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
            do contactList.add(string.getPhotoPathByFolderPathAndName(cursor.getString(0), cursor.getString(1)));
            while (cursor.moveToNext());

        return contactList;
    }

    public void checkDeletedPhotos() {
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(images, projection, null, null, null);
        ArrayList<String> dbPhotoPaths = getAllPhotosPaths();

        if (cur.moveToFirst()) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            do dbPhotoPaths.remove(string.quoteReplace(cur.getString(pathColumn)));
            while (cur.moveToNext());
        }

        for (String dbPhotoPath : dbPhotoPaths) {
            deletePhotoByPath(dbPhotoPath);
            String path = string.getBucketPathbyImagePath(dbPhotoPath);

            if (getDBPhotosCountByAlbum(path) == 0)
                deleteAlbum(path);
        }

    }

    public int getDBPhotosCountByAlbum(String path) {
        String countQuery = "SELECT * FROM " + TABLE_PHOTOS + " WHERE " + PHOTO_FOLDER_PATH + "='" + string
                .quoteReplace(path) + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
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
    boolean checkPhoto(String path) {

        String nameAndPath[] = string.getPhotoFolderPathAndNameByPath(path);
        boolean b = true;
        String selectQuery = "SELECT  " + PHOTO_FOLDER_PATH + ", " + PHOTO_NAME + " FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_NAME + "='" + string.quoteReplace(nameAndPath[1]) + "' AND " + PHOTO_FOLDER_PATH + " ='" + string
                .quoteReplace(nameAndPath[0]) + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0)
            b = false;
        //addHiddenAlbum(Path);
        cursor.close();
        return b;
    }

    void addPhoto(Photo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PHOTO_FOLDER_PATH, string.quoteReplace(contact.FolderPath));
        values.put(PHOTO_NAME, string.quoteReplace(contact.name));
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
        values.put(PHOTO_NAME, string.quoteReplace(contact.name));
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
                    true, getDBPhotosCountByAlbum(cursor.getString(0))));
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

    public Photo getPhoto(String path) {
        String selectQuery = "SELECT  " + PHOTO_NAME + ", " + PHOTO_FOLDER_PATH + ", " + PHOTO_DATE_TAKEN + " FROM " +
                TABLE_PHOTOS + " WHERE " +
                PHOTO_NAME + "='" + path + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Photo a = null;
        if (cursor.moveToFirst()) {
            a = new Photo(
                    string.quoteReverse(cursor.getString(0)),
                    string.quoteReverse(cursor.getString(1)),
                    cursor.getString(2));
        }
        return a;
    }

    public ArrayList<Photo> getPhotosByAlbum(String path) {
        ArrayList<Photo> contactList = new ArrayList<Photo>();
        String selectQuery = "SELECT  " + PHOTO_NAME + ", " + PHOTO_FOLDER_PATH + ", " + PHOTO_DATE_TAKEN + " FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_FOLDER_PATH +
                "='" + string.quoteReplace(path) + "' ORDER BY " + PHOTO_DATE_TAKEN + " DESC";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                contactList.add(new Photo(
                        string.quoteReverse(cursor.getString(0)),
                        string.quoteReverse(cursor.getString(1)),
                        cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        return contactList;
    }

    public ArrayList<Photo> getFirstPhotosByAlbum(String path) {
        ArrayList<Photo> contactList = new ArrayList<Photo>();
        String selectQuery = "SELECT " + PHOTO_NAME + ", " + PHOTO_FOLDER_PATH + ", " + PHOTO_DATE_TAKEN + " " +
                "FROM " + TABLE_PHOTOS + " WHERE " +
                PHOTO_FOLDER_PATH +
                "='" + string.quoteReplace(path) + "' ORDER BY " + PHOTO_DATE_TAKEN + " DESC  LIMIT 1";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            contactList.add(new Photo(
                    string.quoteReverse(cursor.getString(0)),
                    string.quoteReverse(cursor.getString(1)),
                    cursor.getString(2)));
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