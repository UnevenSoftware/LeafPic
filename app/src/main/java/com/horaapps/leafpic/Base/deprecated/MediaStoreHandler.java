package com.horaapps.leafpic.Base.deprecated;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.horaapps.leafpic.Base.CustomAlbumsHandler;

import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
@Deprecated
public class MediaStoreHandler {

    Context context;

    private String TAG = "MEDIASTORE_HANDLER";



    public MediaStoreHandler(Context ctx) {
        context = ctx;
    }

    public ArrayList<deprecatedAlbum> getMediaStoreAlbums(String order) {
        ArrayList<deprecatedAlbum> list = new ArrayList<deprecatedAlbum>();

        CustomAlbumsHandler h = new CustomAlbumsHandler(context);
        ArrayList<String> excludedAlbums = new ArrayList<String>();//h.getExcludedALbumsIDs();


        String[] projection = new String[]{
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        Uri images = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE +"="+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +" or "+
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + " ) GROUP BY ( "+ MediaStore.Files.FileColumns.PARENT +" ";


        Cursor cur = context.getContentResolver().query(
                images, projection, selection, null, order);

        if(cur != null) {
            if (cur.moveToFirst()) {
                int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
                int pathColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                do if (!excludedAlbums.contains(cur.getString(idColumn))){
                    deprecatedAlbum deprecatedAlbum = new deprecatedAlbum(cur.getString(idColumn),
                            cur.getString(pathColumn),
                            getAlbumPhotosCount(cur.getString(idColumn)));
                    deprecatedAlbum.setCoverPath(h.getPhotPrevieAlbum(deprecatedAlbum.ID));
                    list.add(deprecatedAlbum);
                }
                while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }


    public ArrayList<deprecatedMedia> getAlbumPhotos(String id, String sort, int filter) {
        return getAlbumPhotos(id, -1, sort, filter);
    }

    public AlbumMediaCount getAlbumPhotosCount(String id) {
        AlbumMediaCount c = new AlbumMediaCount();
        Uri images = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{ MediaStore.Files.FileColumns.PARENT };

        String selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id + "'";

        Cursor cur = context.getContentResolver().query(
                images, projection, selection, null, null);
        if (cur != null) {
            c.photos = cur.getCount();
            cur.close();
        }
        selection = "( "+  MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id + "'";

        cur = context.getContentResolver().query(
                images, projection, selection, null, null);
        if (cur != null) {
            c.videos = cur.getCount();
            cur.close();
        }
        return c;
    }

    public ArrayList<deprecatedMedia> getFirstAlbumPhoto(String ID) {
        return getAlbumPhotos(ID, 1, null, deprecatedAlbum.FILTER_ALL);
    }

    public void getThumnails(){

        String[] projection = new String[]{
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.IMAGE_ID
        };

        Uri images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
Cursor s = MediaStore.Images.Thumbnails.query(context.getContentResolver(),images,projection);
        Cursor cur = MediaStore.Images.Thumbnails.query(context.getContentResolver(),images,projection);
                /*context.getContentResolver().query(
                images,
                projection,
                null, null, null);*/
        if (cur.moveToFirst()){
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Thumbnails.DATA);
            int ThumbCOlumn = cur.getColumnIndex(
                    MediaStore.Images.Thumbnails.IMAGE_ID);
            do {
                Log.wtf("data",cur.getString(pathColumn));
                Log.wtf("data-thumb",cur.getString(ThumbCOlumn));
            }while (cur.moveToNext());
        }
        cur.close();
    }

    public static void deleteAlbumMedia(deprecatedAlbum a, Context context1){
        String[] projection = { MediaStore.Images.Media._ID };

        String selection = MediaStore.Files.FileColumns.PARENT + " = ?";
        String[] selectionArgs = new String[] { a.ID };

        Uri queryUri = MediaStore.Files.getContentUri("external");
        ContentResolver contentResolver = context1.getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            int columnID = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            do {
                long id = c.getLong(columnID);
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                contentResolver.delete(deleteUri, null, null);
            }while (c.moveToNext());
        }
        c.close();

    }

    public ArrayList<deprecatedMedia> getAlbumPhotos(String ID, int n, String order, int filter) {

        String limit = n == -1 ? "" : " DESC LIMIT " + n;
        String orderStyle = order != null ? order : MediaStore.Images.Media.DATE_TAKEN;
        ArrayList<deprecatedMedia> list = new ArrayList<deprecatedMedia>();

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.ORIENTATION
        };

        Uri images = MediaStore.Files.getContentUri("external");
        String selection;

        switch (filter){

            case deprecatedAlbum.FILTER_IMAGE:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + ID + "'";
                break;
            case deprecatedAlbum.FILTER_VIDEO:
                selection = "( "+  MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + ID + "'";
                break;
            case deprecatedAlbum.FILTER_GIF:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and "
                        + MediaStore.Files.FileColumns.PARENT + "='" + ID + "' and "
                        + MediaStore.Images.Media.MIME_TYPE + "='image/gif'";
                break;
            case deprecatedAlbum.FILTER_ALL:
            default:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
                        MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + ID + "'";
                break;
        }

        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                selection,
                null, " " + orderStyle + limit);

        if (cur != null) {
            if (cur.moveToFirst()) {

                int idColumn = cur.getColumnIndex(
                        MediaStore.Images.Media._ID);
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
                int orientation = cur.getColumnIndex(
                        MediaStore.Images.Media.ORIENTATION);
                do {
                    list.add(new deprecatedMedia(
                            cur.getLong(idColumn),
                            cur.getString(pathColumn),
                            cur.getLong(dateColumn),
                            cur.getLong(dateMdofied),
                            cur.getString(mimeColumn),
                            cur.getInt(width),
                            cur.getInt(height),
                            cur.getInt(size),
                            cur.getInt(orientation)
                    ));
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public String getAlbumPhoto(String a) {
        String asd = null;

        String[] projection = new String[]{
                MediaStore.Images.Media.DATA,
                MediaStore.Files.FileColumns.PARENT
        };

        Uri images = MediaStore.Files.getContentUri("external");

        String selectionImages = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + ") and " + MediaStore.Images.ImageColumns.DATA + "='" + a + "'";

        Cursor cur = context.getContentResolver().query(
                images,
                projection,
                selectionImages,
                null, null);

        if (cur != null) {
            if (cur.moveToFirst()) {
                int pathColumn = cur.getColumnIndex(
                        MediaStore.Files.FileColumns.PARENT);
                asd =cur.getString(pathColumn);
            }
            cur.close();
        }

        return asd;
    }
}