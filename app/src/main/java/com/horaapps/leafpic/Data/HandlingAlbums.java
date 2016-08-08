package com.horaapps.leafpic.Data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.horaapps.leafpic.Data.Providers.StorageProvider;
import com.horaapps.leafpic.Data.Providers.MediaStoreProvider;
import com.horaapps.leafpic.R;
import com.horaapps.leafpic.SplashScreen;
import com.horaapps.leafpic.utils.ContentHelper;
import com.horaapps.leafpic.utils.PreferenceUtil;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dnld on 27/04/16.
 */
public class HandlingAlbums {

  public final static String TAG = "HandlingAlbums";
  private String backupFile = "albums.dat";

  public ArrayList<Album> dispAlbums;
  private ArrayList<Album> selectedAlbums;
  private String nameProvider = MediaStoreProvider.class.getName();


  private PreferenceUtil SP;

  private int indexCamera = -1;
  private int current = 0;
  private boolean hidden;

  public HandlingAlbums(Context context) {
    SP = PreferenceUtil.getInstance(context);
    dispAlbums = new ArrayList<Album>();
    selectedAlbums = new ArrayList<Album>();
  }


  public void loadAlbums(Context context, boolean hidden) {
    this.hidden = hidden;

    ArrayList<Album> list = new ArrayList<Album>();
    clearCameraIndex();
    if (SP.getBoolean(context.getString(R.string.preference_use_media_store), true)) {
      list.addAll(MediaStoreProvider.getAlbums(context, hidden));
      nameProvider = MediaStoreProvider.class.getName();
    } else {
      StorageProvider p = new StorageProvider(context);
      list = p.getAlbums(hidden);
      nameProvider = StorageProvider.class.getName();
    }
    dispAlbums = list;
    sortAlbums(context);
  }

  public void addAlbum(int position, Album album) {
    dispAlbums.add(position, album);
    setCurrentAlbumIndex(dispAlbums.indexOf(album));
  }

  public void setCurrentAlbumIndex(int index) {
    current = index;
  }

  public Album getCurrentAlbum() {
    return dispAlbums.get(current);
  }

  private void clearCameraIndex() {
    indexCamera = -1;
  }


  public boolean isContentFromMediaStore() {
    return nameProvider.equals(MediaStoreProvider.class.getName());
  }


  public static ArrayList<String> getSubFolders(File dir) {
    ArrayList<String> array = new ArrayList<String>();
    File[] children = dir.listFiles(new FoldersFileFilter());
    if (children != null)
      for (File child : children)
        array.add(child.getName());
    return array;
  }

  public void saveBackup(final Context context) {
    new Thread(new Runnable() {
      public void run() {
        FileOutputStream outStream;
        try {
          File f = new File(context.getCacheDir(), backupFile);
          outStream = new FileOutputStream(f);
          ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
          objectOutStream.writeObject(dispAlbums);
          objectOutStream.close();
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }).start();
  }


  public void restoreBackup(Context context) {
    FileInputStream inStream;
    try {
      File f = new File(context.getCacheDir(), backupFile);
      inStream = new FileInputStream(f);
      ObjectInputStream objectInStream = new ObjectInputStream(inStream);

      dispAlbums = (ArrayList<Album>) objectInStream.readObject();

      objectInStream.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    } catch (OptionalDataException e1) {
      e1.printStackTrace();
    } catch (StreamCorruptedException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }


  public int toggleSelectAlbum(int index) {
    if (dispAlbums.get(index) != null) {
      dispAlbums.get(index).setSelected(!dispAlbums.get(index).isSelected());
      if (dispAlbums.get(index).isSelected()) selectedAlbums.add(dispAlbums.get(index));
      else selectedAlbums.remove(dispAlbums.get(index));
    }
    return index;
  }

  public Album getAlbum(int index){ return dispAlbums.get(index); }

  public void selectAllAlbums() {
    for (Album dispAlbum : dispAlbums)
      if (!dispAlbum.isSelected()) {
        dispAlbum.setSelected(true);
        selectedAlbums.add(dispAlbum);
      }
  }

  public void removeCurrentAlbum(){ dispAlbums.remove(current); }

  public int getSelectedCount() {
    return selectedAlbums.size();
  }

  public void clearSelectedAlbums() {
    for (Album dispAlbum : dispAlbums)
      dispAlbum.setSelected(false);

    selectedAlbums.clear();
  }

  public void installShortcutForSelectedAlbums(Context appCtx) {
    for (Album selectedAlbum : selectedAlbums) {

      Intent shortcutIntent;
      shortcutIntent = new Intent(appCtx, SplashScreen.class);
      shortcutIntent.setAction(SplashScreen.ACTION_OPEN_ALBUM);
      shortcutIntent.putExtra("albumPath", selectedAlbum.getPath());
      shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

      Intent addIntent = new Intent();
      addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
      addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedAlbum.getName());

      File image = new File(selectedAlbum.getCoverAlbum().getPath());
      Bitmap bitmap;

      String mime = StringUtils.getMimeType(image.getAbsolutePath());

      if(mime.startsWith("image")) {
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), new BitmapFactory.Options());
      } else if(mime.startsWith("video")) {
        bitmap = ThumbnailUtils.createVideoThumbnail(selectedAlbum.getCoverAlbum().getPath(),
                MediaStore.Images.Thumbnails.MINI_KIND);
      } else return;
      bitmap = Bitmap.createScaledBitmap(getCropedBitmap(bitmap), 128, 128, false);
      addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, addWhiteBorder(bitmap, 5));

      addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
      appCtx.sendBroadcast(addIntent);
    }
  }

  private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
    Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
    Canvas canvas = new Canvas(bmpWithBorder);
    canvas.drawColor(Color.WHITE);
    canvas.drawBitmap(bmp, borderSize, borderSize, null);
    return bmpWithBorder;
  }

  private Bitmap getCropedBitmap(Bitmap srcBmp){
    Bitmap dstBmp;
    if (srcBmp.getWidth() >= srcBmp.getHeight()){
      dstBmp = Bitmap.createBitmap(srcBmp,
              srcBmp.getWidth()/2 - srcBmp.getHeight()/2, 0,
              srcBmp.getHeight(), srcBmp.getHeight()
      );
    } else {
      dstBmp = Bitmap.createBitmap(srcBmp, 0,
              srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
              srcBmp.getWidth(), srcBmp.getWidth()
      );
    }
    return dstBmp;
  }

  private void scanFile(Context context, String[] path) {   MediaScannerConnection.scanFile(context, path, null, null); }

  public void hideAlbum(String path, Context context) {
    File dirName = new File(path);
    File file = new File(dirName, ".nomedia");
    if (!file.exists()) {
      try {
        FileOutputStream out = new FileOutputStream(file);
        out.flush();
        out.close();
        scanFile(context, new String[]{ file.getAbsolutePath() });
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  public void hideSelectedAlbums(Context context) {
    for (Album selectedAlbum : selectedAlbums)
      hideAlbum(selectedAlbum, context);
    clearSelectedAlbums();
  }

  private void hideAlbum(final Album a, Context context) {
    hideAlbum(a.getPath(), context);
    dispAlbums.remove(a);
  }

  public void unHideAlbum(String path, Context context) {
    File dirName = new File(path);
    File file = new File(dirName, ".nomedia");
    if (file.exists()) {
      if (file.delete())
        scanFile(context, new String[]{ file.getAbsolutePath() });
    }
  }
  public void unHideSelectedAlbums(Context context) {
    for (Album selectedAlbum : selectedAlbums)
      unHideAlbum(selectedAlbum, context);
    clearSelectedAlbums();
  }

  private void unHideAlbum(final Album a, Context context) {
    unHideAlbum(a.getPath(), context);
    dispAlbums.remove(a);
  }

  public boolean deleteSelectedAlbums(Context context) {
    boolean success = true;

    for (Album selectedAlbum : selectedAlbums) {
      int index = dispAlbums.indexOf(selectedAlbum);
      if(deleteAlbum(selectedAlbum, context))
        dispAlbums.remove(index);
      else success = false;
    }
    return success;
  }

  public boolean deleteAlbum(Album album, Context context) {
    return ContentHelper.deleteFilesInFolder(context, new File(album.getPath()));
  }

  public void excludeSelectedAlbums(Context context) {
    for (Album selectedAlbum : selectedAlbums)
      excludeAlbum(context, selectedAlbum);

    clearSelectedAlbums();
  }


  private void excludeAlbum(Context context, Album a) {
    CustomAlbumsHandler h = new CustomAlbumsHandler(context);
    Log.wtf("excluded:::", a.getPath() + " - " + a.getId());
    h.excludeAlbum(a.getPath(), a.getId());
    dispAlbums.remove(a);
  }

  public int getColumnSortingMode() {
    return SP.getInt("column_sort", AlbumSettings.SORT_BY_DATE);
  }

  public boolean isAscending() {
    return SP.getBoolean("ascending_mode", false);
  }


  public void setDefaultSortingMode(int column) {
    SP.putInt("column_sort", column);
  }

  public void setDefaultSortingAscending(Boolean ascending) {
    SP.putBoolean("ascending_mode", ascending);
  }

  public void sortAlbums(final Context context) {
    AlbumsComparators albumsComparators = new AlbumsComparators(isAscending());

    switch (getColumnSortingMode()) {
      case AlbumSettings.SORT_BY_NAME:
        Collections.sort(dispAlbums, albumsComparators.getNameComparator());
        break;
      case AlbumSettings.SORT_BY_SIZE:
        Collections.sort(dispAlbums, albumsComparators.getSizeComparator());
        break;
      case AlbumSettings.SORT_BY_DATE: default:
        Collections.sort(dispAlbums, albumsComparators.getDateComparator());
        break;
    }

    new Thread(new Runnable() {
      @Override
      public void run() {
        clearCameraIndex();
        for (int i = 0; i < dispAlbums.size(); i++)
          if (getAlbum(i).getName().equals("Camera")) {
            indexCamera = i; break;
          }

        if (indexCamera != -1) {
          Album camera = dispAlbums.remove(indexCamera);
          camera.setName(context.getString(R.string.camera));
          dispAlbums.add(0, camera);
        }
      }
    }).start();
  }

  public Album getSelectedAlbum(int index) { return selectedAlbums.get(index); }

  public void loadAlbums(Context applicationContext) {
    loadAlbums(applicationContext, hidden);
  }
}
