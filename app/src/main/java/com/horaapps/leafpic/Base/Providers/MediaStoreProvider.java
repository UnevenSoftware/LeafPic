package com.horaapps.leafpic.Base.Providers;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.CustomAlbumsHandler;
import com.horaapps.leafpic.Base.ImageFileFilter;
import com.horaapps.leafpic.Base.Media;
import com.horaapps.leafpic.utils.StringUtils;

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
	String[] projection = new String[]{ MediaStore.Files.FileColumns.DATA };
	Uri uri = MediaStore.Files.getContentUri("external");
	String selection = MediaStore.Files.FileColumns.DATA +" LIKE '%.nomedia'";
	Cursor cur = context.getContentResolver().query(uri, projection, selection, null, null);
	if(cur.moveToFirst()) do {
	  File folder = new File(cur.getString(0)).getParentFile();
	  File[] files = folder.listFiles(new ImageFileFilter(true));
	  if(files != null && files.length > 0) {
		Album album = new Album(folder.getAbsolutePath(), folder.getName(), files.length);
		//asd.setCoverPath(customAlbumsHandler.getCoverPathAlbum(asd.getPath()));

		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (File file : files) {
		  if (file.lastModified() > lastMod) {
			choice = file;
			lastMod = file.lastModified();
		  }
		}
		if (choice != null){
		  album.media.add(0, new Media(choice.getAbsolutePath(), choice.lastModified()));
		  list.add(album);
		}
	  }
	  Log.wtf("nomedia", cur.getString(0));
	} while (cur.moveToNext());
	cur.close();
	return list;
  }

  private static ArrayList<Album> getAlbums(Context context) {
	ArrayList<Album> list = new ArrayList<Album>();

	CustomAlbumsHandler h = new CustomAlbumsHandler(context);
	ArrayList<String> excludedAlbums = h.getExcludedFolderIds();


	String[] projection = new String[]{
			MediaStore.Files.FileColumns.PARENT,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME
	};

	Uri images = MediaStore.Files.getContentUri("external");


	String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
							   MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
							   + " ) GROUP BY ( " + MediaStore.Files.FileColumns.PARENT + " ";


	Cursor cur = context.getContentResolver().query(
			images, projection, selection, null, null);

	if (cur != null) {
	  if (cur.moveToFirst()) {
		int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
		int nameColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
		do if (!excludedAlbums.contains(cur.getString(idColumn))) {
		  Media firstAlbumPhoto = getFirstAlbumPhoto(context, cur.getLong(idColumn));
		  if (firstAlbumPhoto != null) {
			String path = StringUtils.getBucketPathbyImagePath(firstAlbumPhoto.getPath());
			Album album = new Album(path, cur.getLong(idColumn), cur.getString(nameColumn),
										   getAlbumPhotosCount(context, cur.getLong(idColumn)));
			album.setCoverPath(h.getCoverPathAlbum(album.getPath()));
			album.media.add(getFirstAlbumPhoto(context, album.getId()));
			list.add(album);
		  }
		}
		while (cur.moveToNext());
	  }
	  cur.close();
	}
	return list;
  }

  private static int getAlbumPhotosCount(Context context, long id) {
	int c = 0;

	Uri images = MediaStore.Files.getContentUri("external");
	String[] projection = new String[]{MediaStore.Files.FileColumns.PARENT};

	String selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
							   " or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ") and " +
							   MediaStore.Files.FileColumns.PARENT + "='" + id + "'";

	Cursor cur = context.getContentResolver().query(
			images, projection, selection, null, null);
	if (cur != null) {
	  c = cur.getCount();
	  cur.close();
	}
	return c;
  }

  private static Media getFirstAlbumPhoto(Context context, long id) {
	ArrayList<Media> list = getAlbumPhotos(context, id, 1, ImageFileFilter.FILTER_ALL);
	return list.size() > 0 ? list.get(0) : null;
  }

  public static ArrayList<Media> getAlbumPhotos(Context context, long id, boolean includeVideo) {
	return getAlbumPhotos(context, id, -1, includeVideo ? ImageFileFilter.FILTER_ALL : ImageFileFilter.FILTER_NO_VIDEO);
  }

  public static ArrayList<Media> getAlbumPhotos(Context context, long id, int n, int filter) {

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
	String selection;

	switch (filter) {
	  case ImageFileFilter.FILTER_IMAGES:
		selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id + "'";
		break;

	  case ImageFileFilter.FILTER_ALL:
	  default:
		selection = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
							MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
							+ ") and " + MediaStore.Files.FileColumns.PARENT + "='" + id + "'";
		break;
	}


	Cursor cur = context.getContentResolver().query(
			images, projection, selection, null,
			" " + MediaStore.Images.Media.DATE_TAKEN + " DESC " + limit);

	if (cur != null) {
	  if (cur.moveToFirst()) {
		//int idColumnIndex =cur.getColumnIndex(MediaStore.Files.FileColumns._ID);
		do {
		  Media m = new Media(cur);
		  /*iCursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
				  context.getContentResolver(), cur.getLong(idColumnIndex),
				  MediaStore.Images.Thumbnails.MINI_KIND,
				  new String[]{ MediaStore.Images.Thumbnails.DATA } );
		  if( cursor != null && cursor.getCount() > 0 ) {
			cursor.moveToFirst();//**EDIT**
			String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
			Log.wtf("thumb","found:: " + path);
		  } else
			Log.wtf("thumb","NOT found");*/
		  list.add(m);
		  //Log.wtf("thumb", m.getPath() +" - "+ m.getDateModified());
		} while (cur.moveToNext());
	  }
	  cur.close();
	}
	return list;
  }
}


//https://github.com/HoraApps/LeafPic/blob/dcbee1b9aeb3f7c7df2c4a20527727819b90584c/app/src/main/java/com/leafpic/app/Base/MediaStoreHandler.java
//
//    public void getThumnails(){
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

//
//    public String getAlbumPhoto(String a) {
//        String asd = null;
//
//        String[] projection = new String[]{
//                MediaStore.Images.Media.DATA,
//                MediaStore.Files.FileColumns.PARENT
//        };
//
//        Uri images = MediaStore.Files.getContentUri("external");
//
//        String selectionImages = "( " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " or " +
//                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
//                + ") and " + MediaStore.Images.ImageColumns.DATA + "='" + a + "'";
//
//        Cursor cur = context.getContentResolver().query(
//                images,
//                projection,
//                selectionImages,
//                null, null);
//
//        if (cur != null) {
//            if (cur.moveToFirst()) {
//                int pathColumn = cur.getColumnIndex(
//                        MediaStore.Files.FileColumns.PARENT);
//                asd =cur.getString(pathColumn);
//            }
//            cur.close();
//        }
//
//        return asd;
//    }

