package org.horaapps.leafpic.util;

import android.webkit.MimeTypeMap;

public class MimeTypeUtils {

    public static final String UNKNOWN_MIME_TYPE = "unknown/unknown";

    public static String getMimeType(String path) {
        int index;
        if (path == null || (index = path.lastIndexOf('.')) == -1)
            return UNKNOWN_MIME_TYPE;
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(path.substring(index + 1).toLowerCase());
        return mime != null ? mime : UNKNOWN_MIME_TYPE;
    }

    public static String getGenericMIME(String mime) {
        return mime.split("/")[0] + "/*";
    }

    public static String getTypeMime(String mime) {
        return mime.split("/")[0];
    }
}
