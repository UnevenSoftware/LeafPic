package org.horaapps.leafpic.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.media.ExifInterface;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dnld on 3/25/17.
 */

public class BitmapUtils {
    public static Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap getCroppedBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2, 0,
                    srcBmp.getHeight(), srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(srcBmp, 0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(), srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    public static int getOrientation(Uri imgPath, Context ctx){
        ExifInterface exif;
        InputStream in = null;
        try {
            in = ctx.getContentResolver().openInputStream(imgPath);
            exif = new ExifInterface( in );
            int orientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 1 );
            int rotation = 0;
            switch ( orientation ) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = SubsamplingScaleImageView.ORIENTATION_180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = SubsamplingScaleImageView.ORIENTATION_90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = SubsamplingScaleImageView.ORIENTATION_270;
                    break;
                default:
                    rotation = SubsamplingScaleImageView.ORIENTATION_0;
                    break;
            }
            return rotation;
        } catch ( IOException e ) {
            e.printStackTrace();
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }
        return 0;
    }
}
