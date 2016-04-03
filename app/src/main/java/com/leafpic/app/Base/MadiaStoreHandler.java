package com.leafpic.app.Base;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.leafpic.app.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by dnld on 12/31/15.
 */
public class MadiaStoreHandler {

    Context context;

    private String TAG = "MEDIASTORE_HANDLER";

    public static final int FILTER_ALL=45;
    public static final int FILTER_IMAGE=55;
    public static final int FILTER_VIDEO=75;
    public static final int FILTER_GIF=555;

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

        String orderType = " " + MediaStore.Images.Media.DATE_TAKEN + " DESC";

        Cursor cur = context.getContentResolver().query(
                images, projection, selection, null, orderType);

        if(cur != null) {
            if (cur.moveToFirst()) {
                int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
                int pathColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                do if (!excludedAlbums.contains(cur.getString(idColumn))){
                    Album album = new Album(cur.getString(idColumn),
                            cur.getString(pathColumn),
                            getAlbumPhotosCount(cur.getString(idColumn)));
                    album.setCoverPath(h.getPhotPrevieAlbum(album.ID));
                    list.add(album);
                }
                while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public ArrayList<Media> getAlbumPhotos(Album a) {
        return getAlbumPhotos(a.ID, -1, null,FILTER_ALL);
    }

    public ArrayList<Media> getAlbumPhotos(Album a, String sort) {
        return getAlbumPhotos(a.ID, -1, sort,FILTER_ALL);
    }

    public ArrayList<Media> getAlbumPhotos(String id, String sort,int filter) {
        return getAlbumPhotos(id, -1, sort, filter);
    }

    public ArrayList<Media> getAlbumPhotos(String id, String sort) {
        return getAlbumPhotos(id, -1, sort,FILTER_ALL);
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

    public ArrayList<Media> getFirstAlbumPhoto(String ID) {
        return getAlbumPhotos(ID, 1, null, FILTER_ALL);
    }

    public ArrayList<Media> getAlbumPhotos(String ID, int n, String order, int filter) {

        String limit = n == -1 ? "" : " DESC LIMIT " + n;
        String orderStyle = order != null ? order : MediaStore.Images.Media.DATE_TAKEN;
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
        String selection;

        switch (filter){

            case FILTER_IMAGE:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + ID + "'";
                break;
            case FILTER_VIDEO:
                selection = "( "+  MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + ID + "'";
                break;
            case FILTER_GIF:
                selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and "
                        + MediaStore.Files.FileColumns.PARENT + "='" + ID + "' and "
                        + MediaStore.Images.Media.MIME_TYPE + "='image/gif'";
                break;
            case FILTER_ALL:
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
                            cur.getLong(dateColumn),
                            cur.getLong(dateMdofied),
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