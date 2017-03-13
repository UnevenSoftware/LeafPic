package org.horaapps.leafpic.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import org.horaapps.leafpic.model.base.FoldersFileFilter;
import org.horaapps.leafpic.model.base.ImageFileFilter;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by dnld on 24/07/16.
 */

public class ContentProviderHelper {

    @Nullable private static Media getLastMedia(Context context, long albumId) {
        ArrayList<Media> list = getMedia(context, albumId, 1, true);
        return list.size() > 0 ? list.get(0) : null;
    }

    static ArrayList<Media> getMedia(Context context, long albumId, boolean includeVideo) {
        return getMedia(context, albumId, -1, includeVideo);
    }

    private static ArrayList<Media> getMedia(Context context, long albumId, int n, boolean includeVideo) {

        String limit = n == -1 ? "" : "LIMIT " + n;
        ArrayList<Media> list = new ArrayList<Media>();

        String[] projection = new String[]{
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.ORIENTATION
        };

        Uri images = MediaStore.Files.getContentUri("external");
        String selection, selectionArgs[];

        if(includeVideo) {
            selection = String.format("(%s=? or %s=?) and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);
            selectionArgs = new String[] {
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                    String.valueOf(albumId)
            };
        } else {
            selection = String.format("%s=? and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);
            selectionArgs = new String[] { String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String.valueOf(albumId) };
        }

        Cursor cur = context.getContentResolver().query(
                images, projection, selection, selectionArgs,
                MediaStore.Images.Media.DATE_TAKEN + " DESC " + limit);

        if (cur != null) {
            if (cur.moveToFirst()) do list.add(new Media(cur)); while (cur.moveToNext());
            cur.close();
        }
        return list;
    }

    public static long getAlbumId(Context context, String mediaPath) {
        long id = -1;
        Cursor cur = context.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                new String[]{ MediaStore.Files.FileColumns.PARENT },
                MediaStore.Files.FileColumns.DATA+"=?", new String[]{ mediaPath }, null);

        if(cur != null && cur.moveToFirst()){
            id = cur.getLong(0);
            cur.close();
        }

        return id;
    }

    @TestOnly public static ArrayList<Media> getAllMedia(Context context) {
        ArrayList<Media> list = new ArrayList<Media>();
        // TODO: 11/21/16 implement
        return list;
    }

    @TestOnly private String getThumbnailPath(Context context, long id) {
        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
                context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND,
                new String[]{ MediaStore.Images.Thumbnails.DATA });
        if(cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
        return null;
    }

    static ArrayList<Media> getMedia(String path, boolean includeVideo) {
        ArrayList<Media> list = new ArrayList<Media>();
        File[] images = new File(path).listFiles(new ImageFileFilter(includeVideo));
        for (File image : images)
            list.add(new Media(image));
        return list;
    }

    static ArrayList<Album> getAlbums(Context context, HashSet<String> excludedAlbums, boolean hidden) {
        return hidden ? getHiddenAlbums(context, excludedAlbums) : getAlbums(context, excludedAlbums);
    }

    private static ArrayList<Album> getHiddenAlbums(Context context, HashSet<String> excludedAlbums) {
        ArrayList<Album> list = new ArrayList<Album>();
        for (File storage : ContentHelper.getStorageRoots(context))
            fetchRecursivelyHiddenFolder(context, storage, list, excludedAlbums, PreferenceUtil.getBool(context, "set_include_video", true));
        return list;
    }

    private static void fetchRecursivelyHiddenFolder(Context context, File dir, ArrayList<Album> albumArrayList, HashSet<String> excludedAlbums, boolean includeVideo) {
        if (!isExcluded(dir.getPath(), excludedAlbums)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            if (folders != null) {
                for (File temp : folders) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!isExcluded(temp.getPath(), excludedAlbums) && (nomedia.exists() || temp.isHidden()))
                        checkAndAddFolder(context, temp, albumArrayList, includeVideo);

                    fetchRecursivelyHiddenFolder(context, temp, albumArrayList, excludedAlbums, includeVideo);
                }
            }
        }
    }

    private static void checkAndAddFolder(Context context, File dir, ArrayList<Album> albumArrayList, boolean includeVideo) {
        File[] files = dir.listFiles(new ImageFileFilter(includeVideo));
        if (files != null && files.length > 0) {
            //valid folder
            Album asd = new Album(context, dir.getAbsolutePath(), -1, dir.getName(), files.length);
            if (!asd.hasCustomCover()) {

                long lastMod = Long.MIN_VALUE;
                File choice = null;
                for (File file : files) {
                    if (file.lastModified() > lastMod) {
                        choice = file;
                        lastMod = file.lastModified();
                    }
                }
                if (choice != null)
                    asd.addMedia(new Media(choice.getAbsolutePath(), choice.lastModified()));
            }

            albumArrayList.add(asd);
        }
    }

    private static boolean isExcluded(String path, HashSet<String> excludedAlbums) {
        for(String s : excludedAlbums) if (path.startsWith(s)) return true;
        return false;
    }

    public static Album getAlbumFromMedia(Context context, String mediaPath) {
        File parentFolder = new File(mediaPath).getParentFile();
        if (parentFolder == null || !parentFolder.isDirectory())
            return null;

       return new Album(context, parentFolder.getPath(), getAlbumId(context, mediaPath), parentFolder.getName(), 0);
    }

    private static ArrayList<Album> getAlbums(Context context, HashSet<String> excludedAlbums) {
        ArrayList<Album> list = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                "count(*)",
                MediaStore.Images.Media.DATA,
        };

        String selection, selectionArgs[];

        if (PreferenceUtil.getBool(context, "set_include_video", true)) {
            selection = String.format("%s=? or %s=?) group by ( %s ",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);

            selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            };
        } else {
            selection = String.format("%s=?) group by ( %s ",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);
            selectionArgs = new String[] { String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) };
        }

        Cursor cur = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null);

        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String path = StringUtils.getBucketPathByImagePath(cur.getString(3));
                    boolean excluded = isExcluded(path, excludedAlbums);
                    if (!excluded) {
                        Album album = new Album(context, path, cur.getLong(0), cur.getString(1), cur.getInt(2));
                        if(!album.hasCustomCover())
                            album.addMedia(getLastMedia(context, cur.getLong(0)));
                        list.add(album);
                    }
                }
                while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }


    public static void test(Context context) {
        getAllMedia(context).stream().filter(Media::isGif);
    }
}


