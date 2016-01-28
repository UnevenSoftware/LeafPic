package com.leafpic.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
class MadiaStoreHandler {

    Context context;

    private String TAG = "MEDIASTORE_HANDLER";

    public MadiaStoreHandler(Context ctx) {
        context = ctx;
    }


    public ArrayList<Album> getMediaStoreAlbums() {
        ArrayList<Album> list = new ArrayList<Album>();

        String[] projection = new String[]{
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        //EPIC HACK YEAHHHHHHHHHHH
        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                "bucket_id=bucket_id ) GROUP BY ( " + MediaStore.Images.Media.BUCKET_ID + " ",
                null, " " + MediaStore.Images.Media.DATE_TAKEN + " DESC");

        if (cur.moveToFirst()) {
            int idColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            int pathColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            do

                list.add(new Album(cur.getString(idColumn),
                        cur.getString(pathColumn),
                        getAlbumPhotosCount(cur.getString(idColumn))));
            while (cur.moveToNext());
        }
        cur.close();

        return list;
    }

    public ArrayList<Photo> getAlbumPhotos(Album a) {

        ArrayList<Photo> list = new ArrayList<Photo>();

        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                MediaStore.Images.Media.BUCKET_ID + " = ?",
                new String[]{a.ID}, " " + MediaStore.Images.Media.DATE_TAKEN + " DESC");


        if (cur.moveToFirst()) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int dateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);
            int mimeColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.MIME_TYPE);
            do {
                list.add(new Photo(
                        cur.getString(pathColumn),
                        cur.getString(dateColumn),
                        cur.getString(mimeColumn)
                ));
            } while (cur.moveToNext());
        }
        cur.close();
        return list;
    }

    public int getAlbumPhotosCount(String id) {
        int c;
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(
                images, null,
                MediaStore.Images.Media.BUCKET_ID + " = ?",
                new String[]{id}, null);
        c = cur.getCount();
        cur.close();
        return c;
    }

    public ArrayList<Photo> getFirstAlbumPhoto(Album a) {
        ArrayList<Photo> list = new ArrayList<Photo>();

        String[] projection = new String[]{MediaStore.Images.Media.DATA};

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                MediaStore.Images.Media.BUCKET_ID + " = ?",
                new String[]{a.ID}, " " + MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT 1");


        if (cur.moveToFirst()) {
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);

            do {
                list.add(new Photo(cur.getString(pathColumn)));
            } while (cur.moveToNext());
        }
        cur.close();

        return list;
    }

    public void LogStuff() {

        ArrayList<Album> asd = getMediaStoreAlbums();
        for (Album album : asd) {
            getFirstAlbumPhoto(album);
            //Log.d(TAG,getAlbumPhotosCount(album)+"");
        }

    }

}