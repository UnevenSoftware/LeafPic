package com.horaapps.leafpic.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.horaapps.leafpic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
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


	/**
	 * Create a folder. The folder may even be on external SD card for Kitkat.
	 *
	 * @param file The folder to be created.
	 * @return True if creation was successful.
	 */
	public static boolean mkdir(Context context, @NonNull final File file) {
	  if (file.exists()) {
		// nothing to create.
		return file.isDirectory();
	  }

	  // Try the normal way
	  if (file.mkdir()) {
		return true;
	  }

	  // Try with Storage Access Framework.
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		DocumentFile document = getDocumentFile(context, file, true, true);
		// getDocumentFile implicitly creates the directory.
		return document != null && document.exists();
	  }

	  return file.exists();
	}

  public static boolean copyFile(Context context, @NonNull final File source, @NonNull final File target) {
		FileInputStream inStream = null;
		OutputStream outStream = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		boolean success = false;
		try {
			inStream = new FileInputStream(source);

			// First try the normal way
			if (isWritable(target)) {
				// standard way
				outStream = new FileOutputStream(target);
				inChannel = inStream.getChannel();
				outChannel = ((FileOutputStream) outStream).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
			  	success = true;
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					// Storage Access Framework
					DocumentFile targetDocument = getDocumentFile(context,target, false, true);
					if (targetDocument != null) {
						outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
					}
				}
				else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
					// Workaround for Kitkat ext SD card
					Uri uri = getUriFromFile(context,target.getAbsolutePath());
					if (uri != null) {
						outStream = context.getContentResolver().openOutputStream(uri);
					}
				}
				else {
					success = false;
				}

				if (outStream != null) {
					// Both for SAF and for Kitkat, write to output stream.
					byte[] buffer = new byte[4096]; // MAGIC_NUMBER
					int bytesRead;
					while ((bytesRead = inStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, bytesRead);
					}
				  success = true;
				}

			}
		}
		catch (Exception e) {
			Log.e(TAG, "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
			return false;
		}
		finally {
			try {
				inStream.close();
			}
			catch (Exception e) {
				// ignore exception
			}
			try {
				outStream.close();
			}
			catch (Exception e) {
				// ignore exception
			}
			try {
				inChannel.close();
			}
			catch (Exception e) {
				// ignore exception
			}
			try {
				outChannel.close();
			}
			catch (Exception e) {
				// ignore exception
			}
		}
		return success;
	}

  /**
   * Move a file. The target file may even be on external SD card.
   *
   * @param source The source file
   * @param target The target file
   * @return true if the copying was successful.
   */
  public static boolean moveFile(Context context, @NonNull final File source, @NonNull final File
																				target) {
	// First try the normal rename.
	boolean success = source.renameTo(target);

	if (!success) {
	  success = copyFile(context, source, target);
	  if (success) {
		success = deleteFile(context, source);
	  }
	}

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
     * Delete a folder.
     *
     * @param file The folder name.
     * @return true if successful.
     */
    public static boolean rmdir(Context context, @NonNull final File file) {
        if (!file.exists()) {
            return true;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] fileList = file.list();
        if (fileList != null && fileList.length > 0) {
            // Delete only empty folder.
            return false;
        }

        // Try the normal way
        if (file.delete()) {
            return true;
        }

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
        Uri[] treeUris = getTreeUris(context);
        Uri treeUri = null;

        if (treeUris.length == 0) {
            return null;
        }

        String fullPath;
        try {
            fullPath = file.getCanonicalPath();
        }
        catch (IOException e) {
            return null;
        }

        String baseFolder = null;

        // First try to get the base folder via unofficial StorageVolume API from the URIs.

        for (int i = 0; baseFolder == null && i < treeUris.length; i++) {
            String treeBase = getFullPathFromTreeUri(context,treeUris[i]);
            if (treeBase != null && fullPath.startsWith(treeBase)) {
                treeUri = treeUris[i];
                baseFolder = treeBase;
            }
        }

        if (baseFolder == null) {
            // Alternatively, take root folder from device and assume that base URI works.
            treeUri = treeUris[0];
            baseFolder = getExtSdCardFolder(context, file);
        }

        if (baseFolder == null) {
            return null;
        }

        String relativePath = fullPath.substring(baseFolder.length() + 1);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if (i < parts.length - 1) {
                    if (createDirectories) {
                        nextDocument = document.createDirectory(parts[i]);
                    }
                    else {
                        return null;
                    }
                }
                else if (isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                }
                else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

  /**
   * Get the stored tree URIs.
   *
   * @return The tree URIs.
   * @param context context
   */
  private static Uri[] getTreeUris(Context context) {
	List<Uri> uris = new ArrayList<Uri>();

	Uri uri1 = getSharedPreferenceUri(context, R.string.preference_internal_uri_extsdcard_photos);
	if (uri1 != null) {
	  uris.add(uri1);
	}

	/*
	Uri uri2 = getSharedPreferenceUri(R.string.key_internal_uri_extsdcard_input);
	if (uri2 != null) {
	  uris.add(uri2);
	}
	*/
	return uris.toArray(new Uri[uris.size()]);
  }

  /**
   * Retrieve the default shared preferences of the application.
   *
   * @return the default shared preferences.
   */
  private static PreferenceUtil getSharedPreferences(Context context) {
	return PreferenceUtil.getInstance(context);
  }

  /**
   * Retrieve an Uri shared preference.
   *
   * @param preferenceId the id of the shared preference.
   * @return the corresponding preference value.
   */
  private static Uri getSharedPreferenceUri(Context context , int preferenceId) {
	String uriString = getSharedPreferences(context).getString(context.getString(preferenceId), null);

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
	getSharedPreferences(context).putString(context.getString(preferenceId), uri == null ? null :uri.toString());
  }
    /**
     * Determine the main folder of the external SD card containing the given file.
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
	private static String getExtSdCardFolder(Context context, @NonNull final File file) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
    }
    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree RI.
     * @return The path (without trailing file separator).
     */
    @Nullable
    private static String getFullPathFromTreeUri(Context context, @Nullable final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = getVolumePath(context, getVolumeIdFromTreeUri(treeUri));
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            }
            else {
                return volumePath + File.separator + documentPath;
            }
        }
        else {
            return volumePath;
        }
    }

    /**
     * Get the volume ID from the tree URI.
     *
     * @param treeUri The tree URI.
     * @return The volume ID.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        }
        else {
            return null;
        }
    }

    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        }
        else {
            return File.separator;
        }
    }

    /**
	 * Get the path of a certain volume.
	 *
	 * @param volumeId The volume id.
	 * @return The path.
	 */
	private static String getVolumePath(Context context, final String volumeId) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return null;
		}

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
				if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
					return (String) getPath.invoke(storageVolumeElement);
				}

				// other volumes?
				if (uuid != null) {
					if (uuid.equals(volumeId)) {
						return (String) getPath.invoke(storageVolumeElement);
					}
				}
			}

			// not found.
			return null;
		}
		catch (Exception ex) {
			return null;
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
	// First try the normal deletion.
	if (file.delete()) {
	  return true;
	}

	// Try with Storage Access Framework.
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  DocumentFile document = getDocumentFile(context, file, false, true);
	  return document != null && document.delete();
	}

	// Try the Kitkat workaround.
	if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
	  ContentResolver resolver = context.getContentResolver();

	  try {
		Uri uri = null;//MediaStoreUtil.getUriFromFile(file.getAbsolutePath());
		if (uri != null) {
		  resolver.delete(uri, null, null);
		}
		return !file.exists();
	  }
	  catch (Exception e) {
		Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
		return false;
	  }
	}

	return !file.exists();
  }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }


    public static String getMediaPath(final Context context, final Uri uri)
    {
	  Log.wtf("asd", uri+"");
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
        final String[] projection = {
                column
        };

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

}
