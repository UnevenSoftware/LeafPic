package org.horaapps.leafpic.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Jibo on 09/05/2016.
 */
public class Affix {

    private static final String DIRECTORY_NAME = "AffixedPictures";

    public static void AffixBitmapList(Context ctx, ArrayList<Bitmap> bitmapArray, Options options){

        Bitmap unionBitmap;
        if (options.isVertical())
            unionBitmap = Bitmap.createBitmap(getMaxBitmapWidth(bitmapArray),getBitmapsHeight(bitmapArray), Bitmap.Config.ARGB_8888);
        else
            unionBitmap = Bitmap.createBitmap(getBitmapsWidth(bitmapArray), getMaxBitmapHeight(bitmapArray), Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(unionBitmap);
        combineBitmap(comboImage, bitmapArray, options.isVertical());
        saveFile(ctx, unionBitmap, options);
    }

    private static Canvas combineBitmap(Canvas cs, ArrayList<Bitmap> bpmList, boolean vertical){
        if (vertical){
            int height = bpmList.get(0).getHeight();
            cs.drawBitmap(bpmList.get(0), 0f, 0f, null);

            for (int i = 1; i < bpmList.size(); i++) {
                cs.drawBitmap(bpmList.get(i), 0f, height, null);
                height += bpmList.get(i).getHeight();
            }
            return cs;
        } else {
            int width = bpmList.get(0).getWidth();
            cs.drawBitmap(bpmList.get(0), 0f, 0f, null);

            for (int i = 1; i < bpmList.size(); i++) {
                cs.drawBitmap(bpmList.get(i), width, 0f, null);
                width += bpmList.get(i).getWidth();
            }
            return cs;
        }
    }

    private static void saveFile(Context context, Bitmap bmp, Options options){
        try {
            File file = new File(options.getFolderPath(), System.currentTimeMillis() + "." + options.getExtensionFormat());
            if (file.createNewFile()) {
                OutputStream os = new FileOutputStream(file);
                bmp.compress(options.getFormat(), options.getQuality(), os);
                os.close();
                MediaScannerConnection.scanFile(context, new String[]{ file.getAbsolutePath() }, null, null);
            }
        } catch(IOException e) {
            Log.e("combineImages", "problem combining images", e);
        }
    }

    private static int getMaxBitmapWidth(ArrayList<Bitmap> bpmHeightArray){
        int width = bpmHeightArray.get(0).getWidth();
        for (int i=1;i<bpmHeightArray.size();i++){
            if(width<bpmHeightArray.get(i).getWidth())
                width=bpmHeightArray.get(i).getWidth();
        }
        return width;
    }

    private static int getBitmapsWidth(ArrayList<Bitmap> bpmHeightArray){
        int width=0;
        for (int i=0;i<bpmHeightArray.size();i++){
            width+=bpmHeightArray.get(i).getWidth();
        }
        return width;
    }

    private static int getMaxBitmapHeight(ArrayList<Bitmap> bpmHeightArray){
        int height = bpmHeightArray.get(0).getHeight();
        for (int i=1;i<bpmHeightArray.size();i++){
            if(height<bpmHeightArray.get(i).getHeight())
                height=bpmHeightArray.get(i).getHeight();
        }
        return height;
    }

    private static int getBitmapsHeight(ArrayList<Bitmap> bpmHeightArray){
        int height=0;
        for (int i=0;i<bpmHeightArray.size();i++){
            height+=bpmHeightArray.get(i).getHeight();
        }
        return height;
    }

    public static String getDefaultDirectoryPath() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + DIRECTORY_NAME);
        if (!dir.exists())
            dir.mkdir();
        return dir.getAbsolutePath();
    }

    public static class Options {

        /**
         * Specifies the known formats a bitmap can be compressed into
         */

        private String folderPath = null;
        private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        private int quality = 90;
        private boolean vertical = false;

        public Options(String folderPath, Bitmap.CompressFormat format, int quality, boolean vertical) {
            this.folderPath = folderPath;
            this.format = format;
            this.quality = quality;
            this.vertical = vertical;
        }

        String getFolderPath() {
            return folderPath;
        }

        public boolean isVertical() {
            return vertical;
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

        int getQuality() {
            return quality;
        }
    }

}