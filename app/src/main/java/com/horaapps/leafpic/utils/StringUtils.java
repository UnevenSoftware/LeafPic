package com.horaapps.leafpic.utils;

import android.content.Context;
import android.media.ExifInterface;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognise a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }

    public static String getBucketNameByImagePath(String path) {
        String b[] = path.split("/");
        return b[b.length - 2];
    }

    public static String getBucketNameByBucketPath(String path) {
        String b[] = path.split("/");
        return b[b.length - 1];
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

    public static long getID(String uri){
        return Long.parseLong(uri.substring(uri.lastIndexOf('/')+1));
    }

    public static String getPhotoExtensionByPath(String path) {
        String b[] = path.split("/");
        String fi = b[b.length - 1];
        return fi.substring(fi.indexOf('.'), fi.length());
    }

    public static String getPhotoRenamed(String path,String newName){
        String res = null, fi;
        String b[] = path.split("/");
        fi = b[b.length - 1];
        for (int i = 0; i < b.length-1; i++) res+=b[i]+"/";
        res+=newName + fi.substring(fi.indexOf('.'), fi.length());
        return res;
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

    public static String quoteReplace(String psString) {
        return psString.replace("'", "{*~^]");
    }

    private static String quoteReverse(String psString) {
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
