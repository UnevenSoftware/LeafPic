package org.horaapps.leafpic.utils;

import android.graphics.Bitmap;

/**
 * Created by dnld on 15/05/16.
 */
public class AffixOptions {

    /**
     * Specifies the known formats a bitmap can be compressed into
     */

    private String folderPath = null;
    private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
    private int quality = 90;
    private boolean vertical = false;

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public AffixOptions(String folderPath, Bitmap.CompressFormat format, int quality, boolean vertical) {
        this.folderPath = folderPath;
        this.format = format;
        this.quality = quality;
        this.vertical = vertical;
    }

    String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Bitmap.CompressFormat getFormat() {
        return format;
    }

    String getExtensionFormat() {
        switch (format) {
            case JPEG: default: return "jpg";
            case PNG: return "png";
            case WEBP: return "webp";
        }
    }

    public void setFormat(Bitmap.CompressFormat format) {
        this.format = format;
    }

    int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}