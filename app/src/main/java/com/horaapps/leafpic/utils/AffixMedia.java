package com.horaapps.leafpic.utils;

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
public class AffixMedia {

    public static final String DIRECTORY_NAME = "AffixedPictures";

   /* public static void AffixBitmapList(Context ctx, ArrayList<Bitmap> bitmapArray, boolean
            vertical, String folderPath, String format){

        Bitmap unionBitmap;
        if (vertical)
            unionBitmap = Bitmap.createBitmap(getMaxBitmapWidth(bitmapArray),getBitmapsHeight(bitmapArray), Bitmap.Config.ARGB_8888);
        else
            unionBitmap = Bitmap.createBitmap(getBitmapsWidth(bitmapArray),getMaxBitmapHeight(bitmapArray), Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(unionBitmap);
        combineBitmap(comboImage,bitmapArray,vertical);
        saveFile(unionBitmap, folderPath, format);
    }*/

    public static void AffixBitmapList(Context ctx, ArrayList<Bitmap> bitmapArray, AffixOptions options){

        Bitmap unionBitmap;
        if (options.isVertical())
            unionBitmap = Bitmap.createBitmap(getMaxBitmapWidth(bitmapArray),getBitmapsHeight(bitmapArray), Bitmap.Config.ARGB_8888);
        else
            unionBitmap = Bitmap.createBitmap(getBitmapsWidth(bitmapArray),getMaxBitmapHeight(bitmapArray), Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(unionBitmap);
        combineBitmap(comboImage,bitmapArray,options.isVertical());
        saveFile(ctx, unionBitmap, options);
    }

    public static Canvas combineBitmap(Canvas cs, ArrayList<Bitmap> bpmList, boolean vertical){
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

    public static void saveFile(Context context, Bitmap bmp, AffixOptions options){
        Log .wtf("asd",String.format("%s/%d.%s",options.getFolderPath(),System.currentTimeMillis
                (),options.getExtensionFormat()));
        try {
            File file = new File(options.getFolderPath()+'/'+(System.currentTimeMillis()) + "." +
                    options.getExtensionFormat());
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

    public static int getMaxBitmapWidth(ArrayList<Bitmap> bpmHeightArray){
        int width = bpmHeightArray.get(0).getWidth();
        for (int i=1;i<bpmHeightArray.size();i++){
            if(width<bpmHeightArray.get(i).getWidth())
                width=bpmHeightArray.get(i).getWidth();
        }
        return width;
    }

    public static int getBitmapsWidth(ArrayList<Bitmap> bpmHeightArray){
        int width=0;
        for (int i=0;i<bpmHeightArray.size();i++){
            width+=bpmHeightArray.get(i).getWidth();
        }
        return width;
    }

    public static int getMaxBitmapHeight(ArrayList<Bitmap> bpmHeightArray){
        int height = bpmHeightArray.get(0).getHeight();
        for (int i=1;i<bpmHeightArray.size();i++){
            if(height<bpmHeightArray.get(i).getHeight())
                height=bpmHeightArray.get(i).getHeight();
        }
        return height;
    }

    public static int getBitmapsHeight(ArrayList<Bitmap> bpmHeightArray){
        int height=0;
        for (int i=0;i<bpmHeightArray.size();i++){
            height+=bpmHeightArray.get(i).getHeight();
        }
        return height;
    }

    public static String getDefaultDirectoryPath() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/"+DIRECTORY_NAME);
        if (!dir.exists())
            dir.mkdir();
        return dir.getAbsolutePath();
    }


}