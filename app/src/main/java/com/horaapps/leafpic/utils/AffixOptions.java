package com.horaapps.leafpic.utils;

import android.graphics.Bitmap;

/**
 * Created by dnld on 15/05/16.
 */
public class AffixOptions {

    /**
     * Specifies the known formats a bitmap can be compressed into
     */


    String folderPath = null;
    Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
    int quality = 90;
    boolean vertical = false;

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

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Bitmap.CompressFormat getFormat() {
        return format;
    }

    public String getExtensionFormat() {
        switch (format) {
            case JPEG: default: return "jpg";
            case PNG: return "png";
            case WEBP: return "webp";
        }
    }

    public void setFormat(Bitmap.CompressFormat format) {
        this.format = format;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}