package com.leafpic.app.Base;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
public class MadiaStoreHandler {

    Context context;

    private String TAG = "MEDIASTORE_HANDLER";

    public MadiaStoreHandler(Context ctx) {
        context = ctx;
    }


    public ArrayList<Album> getMediaStoreAlbums() {
        ArrayList<Album> list = new ArrayList<Album>();

        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        ArrayList<String> excludedAlbums = h.getExcludedALbumsIDs();


        String[] projection = new String[]{
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        Uri images = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +" or "+
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + " ) GROUP BY ( "+ MediaStore.Files.FileColumns.PARENT +" ";

        String selectionImages = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " ) GROUP BY ( "+ MediaStore.Files.FileColumns.PARENT +" ";

        String orderType = " " + MediaStore.Images.Media.DATE_TAKEN + " DESC";

        Cursor cur = context.getContentResolver().query(
                images, projection, selectionImages, null, orderType);

        if(cur != null) {
            if (cur.moveToFirst()) {
                int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
                int pathColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                do if (!excludedAlbums.contains(cur.getString(idColumn)))
                    list.add(new Album(cur.getString(idColumn),
                            cur.getString(pathColumn),
                            getAlbumPhotosCount(cur.getString(idColumn))));
                while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public ArrayList<Media> getAlbumPhotos(Album a) {

        ArrayList<Media> list = new ArrayList<Media>();

        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE
        };

        Uri images = MediaStore.Files.getContentUri("external");

        String selectionImages = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " and "+ MediaStore.Files.FileColumns.PARENT +"='"+a.ID+"'";

        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                selectionImages,
                null, " " + MediaStore.Images.Media.DATE_TAKEN + " DESC");


        if(cur != null) {
            if (cur.moveToFirst()) {
                int pathColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATA);
                int dateColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATE_TAKEN);
                int mimeColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.MIME_TYPE);
                int dateMdofied = cur.getColumnIndex(
                        MediaStore.Images.Media.DATE_MODIFIED);
                int width = cur.getColumnIndex(
                        MediaStore.Images.Media.WIDTH);
                int height = cur.getColumnIndex(
                        MediaStore.Images.Media.HEIGHT);
                int size = cur.getColumnIndex(
                        MediaStore.Images.Media.SIZE);

                do {
                    list.add(new Media(
                            cur.getString(pathColumn),
                            cur.getString(dateColumn),
                            cur.getString(dateMdofied),
                            cur.getString(mimeColumn),
                            cur.getInt(width),
                            cur.getInt(height),
                            cur.getInt(size)
                    ));
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public ArrayList<Media> getAlbumPhotos(Album a, String sort) {

        ArrayList<Media> list = new ArrayList<Media>();

        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE
        };

        Uri images = MediaStore.Files.getContentUri("external");

        String selectionImages = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " and "+ MediaStore.Files.FileColumns.PARENT +"='"+a.ID+"'";

        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                selectionImages,
                null, " " + sort);

        if(cur != null) {
            if (cur.moveToFirst()) {
                int pathColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATA);
                int dateColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATE_TAKEN);
                int mimeColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.MIME_TYPE);
                int dateMdofied = cur.getColumnIndex(
                        MediaStore.Images.Media.DATE_MODIFIED);
                int width = cur.getColumnIndex(
                        MediaStore.Images.Media.WIDTH);
                int height = cur.getColumnIndex(
                        MediaStore.Images.Media.HEIGHT);
                int size = cur.getColumnIndex(
                        MediaStore.Images.Media.SIZE);

                do {
                    list.add(new Media(
                            cur.getString(pathColumn),
                            cur.getString(dateColumn),
                            cur.getString(dateMdofied),
                            cur.getString(mimeColumn),
                            cur.getInt(width),
                            cur.getInt(height),
                            cur.getInt(size)
                    ));
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public ArrayList<Media> getAlbumPhotos(String id, String sort) {

        ArrayList<Media> list = new ArrayList<Media>();

        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE
        };

        Uri images = MediaStore.Files.getContentUri("external");

        String selectionImages = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " and "+ MediaStore.Files.FileColumns.PARENT +"='"+id+"'";

        Cursor cur = context.getContentResolver().query(
                images,
                projection,
              selectionImages,
                null, " " + sort);

        if(cur != null) {
            if (cur.moveToFirst()) {
                int pathColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATA);
                int dateColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATE_TAKEN);
                int mimeColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.MIME_TYPE);
                int dateMdofied = cur.getColumnIndex(
                        MediaStore.Images.Media.DATE_MODIFIED);
                int width = cur.getColumnIndex(
                        MediaStore.Images.Media.WIDTH);
                int height = cur.getColumnIndex(
                        MediaStore.Images.Media.HEIGHT);
                int size = cur.getColumnIndex(
                        MediaStore.Images.Media.SIZE);

                do {
                    list.add(new Media(
                            cur.getString(pathColumn),
                            cur.getString(dateColumn),
                            cur.getString(dateMdofied),
                            cur.getString(mimeColumn),
                            cur.getInt(width),
                            cur.getInt(height),
                            cur.getInt(size)
                    ));
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public int getAlbumPhotosCount(String id) {
        int c = 0;
        Uri images = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{ MediaStore.Files.FileColumns.PARENT };

        String selectionImages = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " and "+ MediaStore.Files.FileColumns.PARENT +"='"+id+"'";

        Cursor cur = context.getContentResolver().query(
                images, projection,
                selectionImages,
                null, null);
        if (cur != null) {
            c = cur.getCount();
            cur.close();
        }
        return c;
    }

    public ArrayList<Media> getFirstAlbumPhoto(Album a) {
        ArrayList<Media> list = new ArrayList<Media>();

        String[] projection = new String[]{MediaStore.Images.Media.DATA};

        Uri images = MediaStore.Files.getContentUri("external");

        String selectionImages = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " and "+ MediaStore.Files.FileColumns.PARENT +"='"+a.ID+"'";

        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                selectionImages,
                null, " " + MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT 1");

        if(cur != null) {
            if (cur.moveToFirst()) {
                int pathColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATA);
                do {
                    list.add(new Media(cur.getString(pathColumn)));
                } while (cur.moveToNext());
            }
            cur.close();
        }

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