package org.horaapps.leafpic.util;

import android.content.Context;
import android.media.ExifInterface;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by dnld on 1/3/16.
 */
public class StringUtils {

    public static String getMimeType(String path) {
        String extension = path.substring(path.lastIndexOf('.')+1);
        return  MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static String getGenericMIME(String mime) {
        return mime.split("/")[0] + "/*";
    }

    public static String getPhotoNameByPath(String path) {
        String b[] = path.split("/");
        String fi = b[b.length - 1];
        return fi.substring(0, fi.lastIndexOf('.'));
    }

    public static String getPhotoPathRenamed(String olderPath, String newName) {
        String c = "", b[] = olderPath.split("/");
        for (int x = 0; x < b.length - 1; x++) c += b[x] + "/";
        c += newName;
        String name = b[b.length - 1];
        c += name.substring(name.lastIndexOf('.'));
        return c;
    }

    public static String getPhotoPathRenamedAlbumChange(String olderPath, String albumNewName) {
        String c = "", b[] = olderPath.split("/");
        for (int x = 0; x < b.length - 2; x++) c += b[x] + "/";
        c += albumNewName +"/"+b[b.length - 1];
        return c;
    }

    public static String getAlbumPathRenamed(String olderPath, String newName) {
        return olderPath.substring(0, olderPath.lastIndexOf('/')) + "/" + newName;
    }

    public static String getPhotoPathMoved(String olderPath, String folderPath) {
        String b[] = olderPath.split("/");
        String fi = b[b.length - 1];
        String path = folderPath + "/";
        path += fi;
        return path;
    }

    public static String getBucketPathByImagePath(String path) {
        String b[] = path.split("/");
        String c = "";
        for (int x = 0; x < b.length - 1; x++) c += b[x] + "/";
        c = c.substring(0, c.length() - 1);
        return c;
    }

    public static void showToast(Context x, String s) {
        Toast t = Toast.makeText(x, s, Toast.LENGTH_SHORT);
        t.show();
    }

     public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
