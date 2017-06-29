package org.horaapps.leafpic.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.SplashScreen;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static org.horaapps.leafpic.new_way.BitmapUtils.addWhiteBorder;
import static org.horaapps.leafpic.new_way.BitmapUtils.getCroppedBitmap;

/**
 * Created by dnld on 3/25/17.
 */

public class AlbumsHelper {

    public static void createShortcuts(Context context, List<Album> albums) {
        for (Album selectedAlbum : albums) {

            Intent shortcutIntent;
            shortcutIntent = new Intent(context, SplashScreen.class);
            shortcutIntent.setAction(SplashScreen.ACTION_OPEN_ALBUM);
            shortcutIntent.putExtra("albumPath", selectedAlbum.getPath());
            shortcutIntent.putExtra("albumId", selectedAlbum.getId());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedAlbum.getName());

            Media coverAlbum = selectedAlbum.getCover();
            File image = new File(coverAlbum.getPath());
            Bitmap bitmap = coverAlbum.isVideo() ? ThumbnailUtils.createVideoThumbnail(coverAlbum.getPath(), MediaStore.Images.Thumbnails.MINI_KIND)
                    : BitmapFactory.decodeFile(image.getAbsolutePath(), new BitmapFactory.Options());

            if (bitmap == null) {
                Toast.makeText(context, R.string.error_thumbnail, Toast.LENGTH_SHORT).show();
                // TODO: 12/31/16
                return;
            }

            bitmap = Bitmap.createScaledBitmap(getCroppedBitmap(bitmap), 128, 128, false);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, addWhiteBorder(bitmap, 5));

            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            context.sendBroadcast(addIntent);
        }
    }

    public static SortingMode getSortingMode(Context context) {
        return SortingMode.fromValue(Hawk.get("albums_sorting_mode", SortingMode.DATE.getValue()));
    }

    public static SortingOrder getSortingOrder(Context context) {
        return SortingOrder.fromValue(Hawk.get("albums_sorting_order", SortingOrder.DESCENDING.getValue()));
    }

    public static void setSortingMode(Context context, SortingMode sortingMode) {
        Hawk.put("albums_sorting_mode", sortingMode.getValue());
    }

    public static void setSortingOrder(Context context, SortingOrder sortingOrder) {
        Hawk.put("albums_sorting_order", sortingOrder.getValue());
    }

    public static void scanFile(Context context, String[] path) {  MediaScannerConnection.scanFile(context, path, null, null); }

    public static void hideAlbum(String path, Context context) {
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

    public static void unHideAlbum(String path, Context context) {
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (file.exists()) {
            if (file.delete())
                scanFile(context, new String[]{ file.getAbsolutePath() });
        }
    }

    public static boolean deleteAlbum(Album album, Context context) {
        return ContentHelper.deleteFilesInFolder(context, new File(album.getPath()));
    }

}
