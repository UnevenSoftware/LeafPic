package org.horaapps.leafpic.data;

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

import org.horaapps.leafpic.activities.SplashScreen;
import org.horaapps.leafpic.data.Providers.MediaStoreProvider;
import org.horaapps.leafpic.data.Providers.StorageProvider;
import org.horaapps.leafpic.data.base.AlbumsComparators;
import org.horaapps.leafpic.data.base.SortingMode;
import org.horaapps.leafpic.data.base.SortingOrder;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by dnld on 27/04/16.
 */
public class HandlingAlbums {

  public final static String TAG = "HandlingAlbums";
  private static String backupFile = "albums.dat";

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
    if (SP.getBoolean(context.getString(org.horaapps.leafpic.R.string.preference_use_alternative_provider), false)) {
      StorageProvider p = new StorageProvider(context);
      list = p.getAlbums(context, hidden);
      nameProvider = StorageProvider.class.getName();
    } else {
      MediaStoreProvider p = new MediaStoreProvider(context);
      list.addAll(p.getAlbums(hidden));
      nameProvider = MediaStoreProvider.class.getName();
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

  public void saveBackup(final Context context) {
    if (!hidden) {
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
  }

  public static void addAlbumToBackup(final Context context, final Album album) {
     new Thread(new Runnable() {
      public void run() {
        try {
          boolean success = false;
          File f = new File(context.getCacheDir(), backupFile);
          ObjectInputStream reader = new ObjectInputStream(new FileInputStream(f));
          Object o = reader.readObject();
          ArrayList<Album> list = null;
          if (o != null) {
            list = (ArrayList<Album>) o;
            for(int i = 0; i < list.size(); i++) {
              if (list.get(i).equals(album)) {
                list.remove(i);
                list.add(i, album);
                success = true;
              }
            }
          }

          if (success) {
            ObjectOutputStream objectOutStream = new ObjectOutputStream(new FileOutputStream(f));
            objectOutStream.writeObject(list);
            objectOutStream.close();
          }

        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
        } catch (IOException e1) {
          e1.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
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
      // TODO: 19/08/16 check this later
      shortcutIntent.putExtra("albumId", selectedAlbum.getId());
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
      bitmap = Bitmap.createScaledBitmap(getCroppedBitmap(bitmap), 128, 128, false);
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

  private Bitmap getCroppedBitmap(Bitmap srcBmp){
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
    // TODO: 19/08/16
    CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
    Log.wtf("excluded:::", a.getPath() + " - " + a.getId());
    h.excludeAlbum(a.getPath(), a.getId());
    dispAlbums.remove(a);
  }

  public SortingMode getSortingMode() {
    return SortingMode.fromValue(SP.getInt("albums_sorting_mode", SortingMode.DATE.getValue()));
  }

  public SortingOrder getSortingOrder() {
    return SortingOrder.fromValue(SP.getInt("albums_sorting_order", SortingOrder.DESCENDING.getValue()));
  }


  public void setDefaultSortingMode(SortingMode sortingMode) {
    SP.putInt("albums_sorting_mode", sortingMode.getValue());
  }

  public void setDefaultSortingAscending(SortingOrder sortingOrder) {
    SP.putInt("albums_sorting_order", sortingOrder.getValue());
  }

  public void sortAlbums(final Context context) {

    Collections.sort(dispAlbums, AlbumsComparators.getComparator(getSortingMode(), getSortingOrder()));

    // TODO: 19/08/16 look a way to pin albums

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
          camera.setName(context.getString(org.horaapps.leafpic.R.string.camera));
          dispAlbums.add(0, camera);
        }
      }
    }).start();
  }

  private void sort(ArrayList<ArrayList> albums, Comparator<ArrayList> comparator) {
    Collections.sort(albums, comparator);
  }

  public Album getSelectedAlbum(int index) { return selectedAlbums.get(index); }

  public void loadAlbums(Context applicationContext) {
    loadAlbums(applicationContext, hidden);
  }
}
