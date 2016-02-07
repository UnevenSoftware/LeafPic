package com.leafpic.app.utils;

import android.content.Context;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * Created by dnld on 1/3/16.
 */
public class StringUtils {

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getBucketNamebyImagePath(String path) {
        String b[] = path.split("/");
        return b[b.length - 2];
    }

    public static String getBucketNamebyBucketPath(String path) {
        String b[] = path.split("/");
        return b[b.length - 1];
    }

    public static String getPhotoNamebyPath(String path) {
        String b[] = path.split("/");
        return b[b.length - 1];
    }

    public static String getPhotoPathByFolderPathAndName(String folderPath, String name) {
        return folderPath + "/" + name;
    }

    public static String[] getPhotoFolderPathAndNameByPath(String path) {
        String rb[] = new String[2];
        String b[] = path.split("/");
        for (int x = 0; x < b.length - 1; x++) rb[0] += b[x] + "/";
        rb[0] = rb[0].substring(0, rb[0].length() - 1);
        rb[1] = b[b.length - 1];
        return rb;
    }

    public static String getPhotoPathByFolderPathAndName(String folderPath, String name, boolean injection_sql_reverse) {
        if (injection_sql_reverse)
            return quoteReverse(folderPath + "/" + name);
        else
            return quoteReplace(folderPath + "/" + name);
    }


    public static String getAlbumPathRenamed(String olderPath, String newName) {
        return olderPath.substring(0, olderPath.lastIndexOf('/')) + "/" + newName;
    }

    public static String getPhotoPathRenamed(String olderPath, String newName) {
        String c = "", b[] = olderPath.split("/");
        for (int x = 0; x < b.length - 2; x++) c += b[x] + "/";
        c += newName + "/";
        c += b[b.length - 1];
        return c;
    }

    public static String getBucketPathbyImagePath(String path) {
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

    public static String quoteReplace(String psString) {
        return psString.replace("'", "{*~^]");
    }

    public static String quoteReverse(String psString) {
        return psString.replace("{*~^]", "'");
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
