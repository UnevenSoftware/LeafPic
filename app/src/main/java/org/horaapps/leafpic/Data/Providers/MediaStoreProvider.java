package org.horaapps.leafpic.Data.Providers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import org.horaapps.leafpic.Data.Album;
import org.horaapps.leafpic.Data.CustomAlbumsHandler;
import org.horaapps.leafpic.Data.ImageFileFilter;
import org.horaapps.leafpic.Data.Media;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;

import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 24/07/16.
 */

public class  MediaStoreProvider {


  public static ArrayList<Album> getAlbums(Context context, boolean hidden) {
	return hidden ? getHiddenAlbums(context) : getAlbums(context);
  }

  private static ArrayList<Album> getHiddenAlbums(Context context) {
	ArrayList<Album> list = new ArrayList<Album>();
	CustomAlbumsHandler h = new CustomAlbumsHandler(context);
	ArrayList<Long> excludedAlbums = h.getExcludedFolderIds();
	String[] projection = new String[]{ MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.PARENT };
	String selection = MediaStore.Files.FileColumns.MEDIA_TYPE+"="+MediaStore.Files.FileColumns.MEDIA_TYPE_NONE+" and "+
							   MediaStore.Files.FileColumns.DATA +" LIKE '%.nomedia'";
	Cursor cur = context.getContentResolver().query(MediaStore.Files.getContentUri("external"), projection, selection, null, null);
	if(cur.moveToFirst())
	  do if (!excludedAlbums.contains(cur.getLong(1))) {
		File folder = new File(cur.getString(0)).getParentFile();
		File[] files = folder.listFiles(new ImageFileFilter(true));
		if (files != null && files.length > 0) {
		  Album album = new Album(folder.getAbsolutePath(), folder.getName(), files.length);
		  album.setCoverPath(h.getCoverPathAlbum(album.getPath(), album.getId()));

		  long lastMod = Long.MIN_VALUE;
		  File choice = null;
		  for (File file : files) {
			if (file.lastModified() > lastMod) {
			  choice = file;
			  lastMod = file.lastModified();
			}
		  }
		  if (choice != null) {
			album.media.add(0, new Media(choice.getAbsolutePath(), choice.lastModified()));
			list.add(album);
		  }
		}
	  } while (cur.moveToNext());
	cur.close();
	return list;
  }

  private static ArrayList<Album> getAlbums(Context context) {
	ArrayList<Album> list = new ArrayList<Album>();

	CustomAlbumsHandler h = new CustomAlbumsHandler(context);
	ArrayList<Long> excludedAlbums = h.getExcludedFolderIds();


	String[] projection = new String[]{
			MediaStore.Files.FileColumns.PARENT,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME
	};

	String selection, selectionArgs[];

	PreferenceUtil SP = PreferenceUtil.getInstance(context);
	int filter = SP.getBoolean("set_include_video", true) ? ImageFileFilter.FILTER_ALL : ImageFileFilter.FILTER_NO_VIDEO;
	switch (filter) {
	  case ImageFileFilter.FILTER_NO_VIDEO:
		selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? ) GROUP BY ( " + MediaStore.Files.FileColumns.PARENT + " ";

		selectionArgs = new String[] {
				String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
		};
		break;

	  case ImageFileFilter.FILTER_ALL:
	  default:
		selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? or " +
							 MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
							 + " ) GROUP BY ( " + MediaStore.Files.FileColumns.PARENT + " ";

		selectionArgs = new String[] {
			  String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
			  String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
	  };
		break;
	}




	Cursor cur = context.getContentResolver().query(
			MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null);

	if (cur != null) {
	  if (cur.moveToFirst()) {
		int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
		int nameColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
		do if (!excludedAlbums.contains(cur.getLong(idColumn))) {
		  Media firstAlbumPhoto = getLastMedia(context, cur.getLong(idColumn));
		  if (firstAlbumPhoto != null) {
			String path = StringUtils.getBucketPathByImagePath(firstAlbumPhoto.getPath());
			Album album = new Album(path, cur.getLong(idColumn), cur.getString(nameColumn),
										   getAlbumCount(context, cur.getLong(idColumn)));
			album.setCoverPath(h.getCoverPathAlbum(album.getPath(), album.getId()));
			if (album.addMedia(getLastMedia(context, album.getId()))) list.add(album);
		  }
		}
		while (cur.moveToNext());
	  }
	  cur.close();
	}
	return list;
  }

  private static int getAlbumCount(Context context, long id) {
	int c = 0;
	String selection = "( "+MediaStore.Files.FileColumns.MEDIA_TYPE + "=? or "
						+ MediaStore.Files.FileColumns.MEDIA_TYPE + "=? ) and " + MediaStore.Files.FileColumns.PARENT + "=?";

	String[] selectionArgs = new String[] {
			String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
			String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
			String.valueOf(id)
	};

	Cursor cur = context.getContentResolver().query(MediaStore.Files.getContentUri("external"),
			new String[]{ MediaStore.Files.FileColumns.PARENT }, selection, selectionArgs, null);
	if (cur != null) c = cur.getCount();
	cur.close();
	return c;
  }

  @Nullable private static Media getLastMedia(Context context, long albumId) {
	ArrayList<Media> list = getMedia(context, albumId, 1, ImageFileFilter.FILTER_ALL);
	return list.size() > 0 ? list.get(0) : null;
  }

  public static ArrayList<Media> getMedia(Context context, long albumId, boolean includeVideo) {
	return getMedia(context, albumId, -1, includeVideo ? ImageFileFilter.FILTER_ALL : ImageFileFilter.FILTER_NO_VIDEO);
  }

  private static ArrayList<Media> getMedia(Context context, long albumId, int n, int filter) {

	String limit = n == -1 ? "" : "LIMIT " + n;
	ArrayList<Media> list = new ArrayList<Media>();

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
	String selection, selectionArgs[];

	switch (filter) {
	  case ImageFileFilter.FILTER_NO_VIDEO:
		selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?  and " + MediaStore.Files.FileColumns.PARENT + "=?";
		selectionArgs = new String[] { String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String.valueOf(albumId) };
		break;

	  case ImageFileFilter.FILTER_ALL:
	  default:
		selection = "( "+MediaStore.Files.FileColumns.MEDIA_TYPE + "=? or "
							+ MediaStore.Files.FileColumns.MEDIA_TYPE + "=? ) and " + MediaStore.Files.FileColumns.PARENT + "=?";

		selectionArgs = new String[] {
				String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
				String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
				String.valueOf(albumId)
		};
		break;
	}

	Cursor cur = context.getContentResolver().query(
			images, projection, selection, selectionArgs,
			" " + MediaStore.Images.Media.DATE_TAKEN + " DESC " + limit);

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
	if(cur.moveToNext())
	  id = cur.getLong(0);
	else Log.wtf("wtf", "Parent ID not for media: "+mediaPath);
	cur.close();
	return id;
  }

  @TestOnly
  private String getThumbnailPath(Context context, long id) {
	Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
			context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND,
			new String[]{ MediaStore.Images.Thumbnails.DATA });
	if(cursor.moveToFirst())
	  return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
	return null;
  }
}



//https://github.com/HoraApps/LeafPic/blob/dcbee1b9aeb3f7c7df2c4a20527727819b90584c/app/src/main/java/com/leafpic/app/Base/MediaStoreHandler.java
//
//    public void getThumbnails(){
//
//        String[] projection = new String[]{
//                MediaStore.Images.Thumbnails.DATA,
//                MediaStore.Images.Thumbnails.IMAGE_ID
//        };
//
//        Uri images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
//        Cursor s = MediaStore.Images.Thumbnails.query(context.getContentResolver(),images,projection);
//        Cursor cur = MediaStore.Images.Thumbnails.query(context.getContentResolver(),images,projection);
//                /*context.getContentResolver().query(
//                images,
//                projection,
//                null, null, null);*/
//        if (cur.moveToFirst()){
//            int pathColumn = cur.getColumnIndex(
//                    MediaStore.Images.Thumbnails.DATA);
//            int ThumbCOlumn = cur.getColumnIndex(
//                    MediaStore.Images.Thumbnails.IMAGE_ID);
//            do {
//                Log.wtf("data",cur.getString(pathColumn));
//                Log.wtf("data-thumb",cur.getString(ThumbCOlumn));
//            }while (cur.moveToNext());
//        }
//        cur.close();
//    }
//
//    public static void deleteAlbumMedia(Album a, Context context1){
//        String[] projection = { MediaStore.Images.Media._ID };
//
//        String selection = MediaStore.Files.FileColumns.PARENT + " = ?";
//        String[] selectionArgs = new String[] { a.id };
//
//        Uri queryUri = MediaStore.Files.getContentUri("external");
//        ContentResolver contentResolver = context1.getContentResolver();
//        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
//        if (c.moveToFirst()) {
//            int columnID = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
//            do {
//                long id = c.getLong(columnID);
//                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                contentResolver.delete(deleteUri, null, null);
//            }while (c.moveToNext());
//        }
//        c.close();
//
//    }
//

