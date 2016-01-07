package com.leafpic.app.utils;

import android.content.Context;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

/**
 * Created by dnld on 1/3/16.
 */
public class string {
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
        return psString.replace("'", "^");
    }

    public static String quoteReverse(String psString) {
        return psString.replace("^", "'");
    }
}
