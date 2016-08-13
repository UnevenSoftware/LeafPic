package org.horaapps.leafpic.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.horaapps.leafpic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dnld on 26/05/16.
 */
public class ContentHelper {

  private static final String TAG = "ContentHelper";
  private static final String PRIMARY_VOLUME_NAME = "primary";

  /**
   * Check is a file is writable. Detects write issues on external SD card.
   *
   * @param file The file
   * @return true if the file is writable.
   */
  private static boolean isWritable(@NonNull final File file) {
	boolean isExisting = file.exists();

	try {
	  FileOutputStream output = new FileOutputStream(file, true);
	  try {
		output.close();
	  }
	  catch (IOException e) {
		// do nothing.
	  }
	}
	catch (java.io.FileNotFoundException e) {
	  return false;
	}
	boolean result = file.canWrite();

	// Ensure that file is not created during this process.
	if (!isExisting) {
	  //noinspection ResultOfMethodCallIgnored
	  file.delete();
	}

	return result;
  }

  private static void scanFile(Context context, String[] paths) {
	MediaScannerConnection.scanFile(context, paths, null, null);
  }

  /**
   * Create a folder. The folder may even be on external SD card for Kitkat.
   *
   * @param dir The folder to be created.
   * @return True if creation was successful.
   */
  public static boolean mkdir(Context context, @NonNull final File dir) {
	boolean success = dir.exists();
	// Try the normal way
	if (!success) success = dir.mkdir();

	// Try with Storage Access Framework.
	if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  DocumentFile document = getDocumentFile(context, dir, true, true);
	  // getDocumentFile implicitly creates the directory.
	  success = document != null && document.exists();
	}

	//let MediaStore know that a dir was created
	if (success) scanFile(context, new String[] { dir.getPath() });

	return success;
  }

  public static boolean copyFile(Context context, @NonNull final File source, @NonNull final File targetDir) {
	InputStream inStream = null;
	OutputStream outStream = null;
	FileChannel inChannel = null;
	FileChannel outChannel = null;
	boolean success = false;
	File target = new File(targetDir, source.getName());

	try {
	  inStream = new FileInputStream(source);

	  // First try the normal way
	  if (isWritable(target)) {
		// standard way
		outStream = new FileOutputStream(target);
		inChannel = ((FileInputStream) inStream).getChannel();
		outChannel = ((FileOutputStream) outStream).getChannel();
		inChannel.transferTo(0, inChannel.size(), outChannel);
		success = true;
	  } else {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		  if (isFileOnSdCard(context, source)) {
			DocumentFile sourceDocument = getDocumentFile(context, source, false, false);
			if (sourceDocument != null) {
			 	inStream = context.getContentResolver().openInputStream(sourceDocument.getUri());
			}
		  }
		  // Storage Access Framework
		  DocumentFile targetDocument = getDocumentFile(context, target, false, false);
		  if (targetDocument != null) {
			outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
		  }
		}
		else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
		  // TODO: 13/08/16 test this
		  // Workaround for Kitkat ext SD card
		  Uri uri = getUriFromFile(context,target.getAbsolutePath());
		  if (uri != null) {
			outStream = context.getContentResolver().openOutputStream(uri);
		  }
		}

		if (outStream != null) {
		  // Both for SAF and for Kitkat, write to output stream.
		  byte[] buffer = new byte[4096]; // MAGIC_NUMBER
		  int bytesRead;
		  while ((bytesRead = inStream.read(buffer)) != -1) outStream.write(buffer, 0, bytesRead);
		  success = true;
		}

	  }
	} catch (Exception e) {
	  Log.e(TAG, "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
	  return false;
	}
	finally {
	  try { inStream.close(); } catch (Exception ignored) { }
	  try { outStream.close(); } catch (Exception ignored) { }
	  try { inChannel.close(); } catch (Exception ignored) { }
	  try { outChannel.close(); } catch (Exception ignored) { }
	}

	if (success) scanFile(context, new String[] { target.getPath() });
	return success;
  }

  public static boolean isFileOnSdCard(Context context, File file) {
	String[] extSdCardPaths = getExtSdCardPaths(context);
	for(String extSdCardPath : extSdCardPaths) {
	  if(file.getPath().startsWith(extSdCardPath)) return true;
	}
	return false;
  }

  /**
   * Move a file. The target file may even be on external SD card.
   *
   * @param source The source file
   * @param targetDir The target Directory
   * @return true if the copying was successful.
   */
  public static boolean moveFile(Context context, @NonNull final File source, @NonNull final File targetDir) {
	// First try the normal rename.
	File target = new File(targetDir, source.getName());

	boolean success = source.renameTo(target);

	if (!success) {
	  success = copyFile(context, source, target);
	  if (success) {
		success = deleteFile(context, source);
	  }
	}

	//if (success) scanFile(context, new String[]{ source.getPath(), target.getPath() });
	return success;
  }

  /**
   * Get an Uri from an file path.
   *
   * @param path The file path.
   * @return The Uri.
   */
  private static Uri getUriFromFile(Context context, final String path) {
	ContentResolver resolver = context.getContentResolver();

	Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
			new String[] {BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
			new String[] {path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
	if (filecursor == null) {
	  return null;
	}
	filecursor.moveToFirst();

	if (filecursor.isAfterLast()) {
	  filecursor.close();
	  ContentValues values = new ContentValues();
	  values.put(MediaStore.MediaColumns.DATA, path);
	  return resolver.insert(MediaStore.Files.getContentUri("external"), values);
	}
	else {
	  int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
	  Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
			  Integer.toString(imageId)).build();
	  filecursor.close();
	  return uri;
	}
  }

  /**
   * Delete all files in a folder.
   *
   * @param folder the folder
   * @return true if successful.
   */

  public static boolean deleteFilesInFolder(Context context, @NonNull final File folder) {
	boolean totalSuccess = true;

	String[] children = folder.list();
	if (children != null) {
	  for (String child : children) {
		File file = new File(folder, child);
		if (!file.isDirectory()) {
		  boolean success = deleteFile(context, file);
		  if (!success) {
			Log.w(TAG, "Failed to delete file" + child);
			totalSuccess = false;
		  }
		}
	  }
	}
	return totalSuccess;
  }



  /**
   * Delete a file. May be even on external SD card.
   *
   * @param file the file to be deleted.
   * @return True if successfully deleted.
   */
  public static boolean deleteFile(Context context, @NonNull final File file) {


	//W/DocumentFile: Failed query: java.lang.IllegalArgumentException: Failed to determine if A613-F0E1:.android_secure is child of A613-F0E1:: java.io.FileNotFoundException: Missing file for A613-F0E1:.android_secure at /storage/sdcard1/.android_secure
	// First try the normal deletion.
	boolean success = file.delete();

	// Try with Storage Access Framework.
	if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  DocumentFile document = getDocumentFile(context, file, false, false);
	  success = document != null && document.delete();
	}

	// Try the Kitkat workaround.
	if (!success && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
	  ContentResolver resolver = context.getContentResolver();

	  try {
		Uri uri = null;//MediaStoreUtil.getUriFromFile(file.getAbsolutePath());
		if (uri != null) {
		  resolver.delete(uri, null, null);
		}
		success = !file.exists();
	  }
	  catch (Exception e) {
		Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
		return false;
	  }
	}

	if(success) scanFile(context, new String[]{ file.getPath() });
	return success;
  }

  public static String[] getExtSdCardPaths(Context context) {
	List<String> paths = new ArrayList<String>();
	for (File file : context.getExternalFilesDirs("external")) {
	  if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
		int index = file.getAbsolutePath().lastIndexOf("/Android/data");
		if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
		else {
		  String path = file.getAbsolutePath().substring(0, index);
		  try { path = new File(path).getCanonicalPath(); }
		  // Keep non-canonical path.
		  catch (IOException ignored) { }
		  paths.add(path);
		}
	  }
	}
	return paths.toArray(new String[paths.size()]);
  }

  /**
   * Delete a folder.
   *
   * @param file The folder name.
   * @return true if successful.
   */
  public static boolean rmdir(Context context, @NonNull final File file) {

	if(!file.exists() && !file.isDirectory()) return false;

	String[] fileList = file.list();

	if(fileList != null && fileList.length > 0)
	  // Delete only empty folder.
	  return false;

	// Try the normal way
	if (file.delete()) return true;


	// Try with Storage Access Framework.
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  DocumentFile document = getDocumentFile(context, file, true, true);
	  return document != null && document.delete();
	}

	// Try the Kitkat workaround.
	if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
	  ContentResolver resolver = context.getContentResolver();
	  ContentValues values = new ContentValues();
	  values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
	  resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

	  // Delete the created entry, such that content provider will delete the file.
	  resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
			  new String[] {file.getAbsolutePath()});
	}

	return !file.exists();
  }

  /**
   * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
   * existing, it is created.
   *
   * @param file              The file.
   * @param isDirectory       flag indicating if the file should be a directory.
   * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
   * @return The DocumentFile
   */
  private static DocumentFile getDocumentFile(Context context, @NonNull final File file, final boolean isDirectory, final boolean createDirectories) {

	Uri treeUri = getTreeUri(context);

	if (treeUri == null) return null;

	DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);

	String[] parts = file.getPath().split("/");
	for (int i = findMagicNumber(parts, treeUri); i < parts.length; i++) { // 3 is the magic number todo
	  // change
	  // this
	  DocumentFile tmp = document.findFile(parts[i]);
	  if (tmp != null)
		document = document.findFile(parts[i]);
	  else {
		if (i < parts.length - 1) {
		  if (createDirectories) document = document.createDirectory(parts[i]);
		  else return null;
		}
		else if (isDirectory) document = document.createDirectory(parts[i]);
		else return document.createFile("image", parts[i]);
	  }
	}

	return document;
  }

  private static int findMagicNumber(String[] parts, Uri treUri){
	return 3;
  }

  /**
   * Get the stored tree URIs.
   *
   * @return The tree URIs.
   * @param context context
   */
  private static Uri getTreeUri(Context context) {
	String uriString = PreferenceUtil.getInstance(context).getString(context.getString(R.string.preference_internal_uri_extsdcard_photos), null);

	if (uriString == null) return null;
	return Uri.parse(uriString);
  }

  /**
   * Set a shared preference for an Uri.
   *
   * @param context context
   * @param preferenceId the id of the shared preference.
   * @param uri          the target value of the preference.
   */
  public static void setSharedPreferenceUri(Context context, final int preferenceId, @Nullable final Uri uri) {
	PreferenceUtil.getInstance(context).putString(context.getString(preferenceId), uri == null ? null :uri.toString());
  }


  public static String getMediaPath(final Context context, final Uri uri)
  {
	// DocumentProvider
	if (DocumentsContract.isDocumentUri(context, uri)) {

	  // ExternalStorageProvider
	  if (isExternalStorageDocument(uri)) {
		final String docId = DocumentsContract.getDocumentId(uri);
		final String[] split = docId.split(":");
		final String type = split[0];

		if ("primary".equalsIgnoreCase(type)) {
		  return Environment.getExternalStorageDirectory() + "/" + split[1];
		}

		// TODO handle non-primary volumes
	  }
	  // DownloadsProvider
	  else if (isDownloadsDocument(uri)) {
		final String id = DocumentsContract.getDocumentId(uri);
		final Uri contentUri = ContentUris.withAppendedId(
				Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

		return getDataColumn(context, contentUri, null, null);
	  }
	  // MediaProvider
	  else if (isMediaDocument(uri)) {
		final String docId = DocumentsContract.getDocumentId(uri);
		final String[] split = docId.split(":");
		final String type = split[0];

		Uri contentUri = null;
		if ("image".equals(type)) {
		  contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		} else if ("video".equals(type)) {
		  contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		} else if ("audio".equals(type)) {
		  contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		}

		final String selection = "_id=?";
		final String[] selectionArgs = new String[] {
				split[1]
		};

		return getDataColumn(context, contentUri, selection, selectionArgs);
	  }
	}
	else if ("downloads".equals(uri.getAuthority())) { //download for chrome-dev workaround
	  String[] seg = uri.toString().split("/");
	  final String id = seg[seg.length - 1];
	  final Uri contentUri = ContentUris.withAppendedId(
			  Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	  return getDataColumn(context, contentUri, null, null);
	}
	// MediaStore (and general)
	else if ("content".equalsIgnoreCase(uri.getScheme())) {
	  try {
		return getDataColumn(context, uri, null, null);
	  } catch (Exception ignored){ }

	}
	// File
	else if ("file".equalsIgnoreCase(uri.getScheme())) {
	  return uri.getPath();
	}

	return null;
  }

  /**
   * Get the value of the data column for this Uri. This is useful for
   * MediaStore Uris, and other file-based ContentProviders.
   *
   * @param context The context.
   * @param uri The Uri to query.
   * @param selection (Optional) Filter used in the query.
   * @param selectionArgs (Optional) Selection arguments used in the query.
   * @return The value of the _data column, which is typically a file path.
   */
  private static String getDataColumn(Context context, Uri uri, String selection,
									  String[] selectionArgs) {

	Cursor cursor = null;
	final String column = "_data";
	final String[] projection = { column };

	try {
	  cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
	  if (cursor != null && cursor.moveToFirst()) {
		final int column_index = cursor.getColumnIndexOrThrow(column);
		return cursor.getString(column_index);
	  }
	} finally {
	  if (cursor != null)
		cursor.close();
	}
	return null;
  }


  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is ExternalStorageProvider.
   */
  private static boolean isExternalStorageDocument(Uri uri) {
	return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  private static boolean isDownloadsDocument(Uri uri) {
	return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  private static boolean isMediaDocument(Uri uri) {
	return "com.android.providers.media.documents".equals(uri.getAuthority());
  }



  /** unused methods **/


  //region old stuff
  /**
   * Determine the main folder of the external SD card containing the given file.
   * @param file the file.
   * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
   * null is returned.
   */
  /*@TargetApi(Build.VERSION_CODES.KITKAT)
  private static String getExtSdCardFolder(Context context, @NonNull final File file) {
	String[] extSdPaths = getExtSdCardPaths(context);
	try {
	  for (String extSdPath : extSdPaths)
		if (file.getCanonicalPath().startsWith(extSdPath)) return extSdPath;
	}
	catch (IOException e) { return null; }
	return null;
  }
  *//**
   * Get the full path of a document from its tree URI.
   *
   * @param treeUri The tree RI.
   * @return The path (without trailing file separator).
   *//*
  @Nullable
  private static String getFullPathFromTreeUri(Context context, @Nullable final Uri treeUri) {
	if (treeUri == null) return null;
	String volumePath = getVolumePath(context, getVolumeIdFromTreeUri(treeUri));
	if (volumePath == null) return File.separator;

	if (volumePath.endsWith(File.separator))
	  volumePath = volumePath.substring(0, volumePath.length() - 1);

	String documentPath = getDocumentPathFromTreeUri(treeUri);
	if (documentPath.endsWith(File.separator))
	  documentPath = documentPath.substring(0, documentPath.length() - 1);

	if (documentPath.length() > 0) {
	  if (documentPath.startsWith(File.separator)) return volumePath + documentPath;
	  else return volumePath + File.separator + documentPath;
	}
	else return volumePath;
  }

  *//**
   * Get the volume ID from the tree URI.
   *
   * @param treeUri The tree URI.
   * @return The volume ID.
   *//*
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static String getVolumeIdFromTreeUri(final Uri treeUri) {
	final String docId = DocumentsContract.getTreeDocumentId(treeUri);
	final String[] split = docId.split(":");

	if (split.length > 0) return split[0];
	else return null;
  }*/

/*  *//**
   * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
   *
   * @param treeUri The tree URI.
   * @return the document path.
   *//*
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static String getDocumentPathFromTreeUri(final Uri treeUri) {
	final String docId = DocumentsContract.getTreeDocumentId(treeUri);
	final String[] split = docId.split(":");
	if ((split.length >= 2) && (split[1] != null)) return split[1];
	else return File.separator;
  }

  *//**
   * Get the path of a certain volume.
   *
   *//*
  private static String getVolumePath(Context context, final String volumeId) {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;

	try {
	  StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

	  Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

	  Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
	  Method getUuid = storageVolumeClazz.getMethod("getUuid");
	  Method getPath = storageVolumeClazz.getMethod("getMediaPath");
	  Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
	  Object result = getVolumeList.invoke(mStorageManager);

	  final int length = java.lang.reflect.Array.getLength(result);
	  for (int i = 0; i < length; i++) {
		Object storageVolumeElement = Array.get(result, i);
		String uuid = (String) getUuid.invoke(storageVolumeElement);
		Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

		// primary volume?
		if (primary && PRIMARY_VOLUME_NAME.equals(volumeId))
		  return (String) getPath.invoke(storageVolumeElement);

		// other volumes?
		if (uuid != null && uuid.equals(volumeId))
		  return (String) getPath.invoke(storageVolumeElement);
	  }
	  return null;
	}
	catch (Exception ex) { return null; }
  }*/
  //endregion
}
