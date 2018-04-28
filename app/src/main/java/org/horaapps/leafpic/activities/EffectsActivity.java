package org.horaapps.leafpic.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import org.horaapps.leafpic.R;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class EffectsActivity  extends Activity implements OnClickListener {

    static final int PICKED_ONE = 0;
    Button choosePicture;
    ImageView DisplayImageView;
    final double GS_RED = 0.299;
    final double GS_GREEN = 0.587;
    final double GS_BLUE = 0.114;




    Bitmap bmp1;
    Canvas canvas;
    Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects);
        DisplayImageView = (ImageView) this
                .findViewById(R.id.DisplayImageView);

        choosePicture = (Button) this.findViewById(R.id.choosePicture1);
        choosePicture.setOnClickListener(this);


    }

    public void onClick(View v) {

        int which = -1;

        if (v == choosePicture) {
            which = PICKED_ONE;
        }

        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, which);




    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Uri imageFileUri = intent.getData();

            if (requestCode == PICKED_ONE) {
                bmp1 = loadBitmap(imageFileUri);



                Bitmap result = invert(bmp1);

                int w = result.getWidth();
                int h = result.getHeight();
                Bitmap.Config config = result.getConfig();
                if (config == null) {
                    config = Bitmap.Config.ARGB_8888;
                }

                Bitmap drawingBitmap = Bitmap.createBitmap(w,
                        h, config);
                canvas = new Canvas(drawingBitmap);
                canvas.drawBitmap(result, 0, 0, null);
                DisplayImageView.setImageBitmap(drawingBitmap);


            }
        }

    }

    public Bitmap invert(Bitmap src) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private Bitmap loadBitmap(Uri imageFileUri) {
        Display currentDisplay = getWindowManager().getDefaultDisplay();

        float dw = currentDisplay.getWidth();
        float dh = currentDisplay.getHeight();

        Bitmap returnBmp = Bitmap.createBitmap((int) dw, (int) dh,
                Bitmap.Config.ARGB_4444);

        try {
            // Load up the image's dimensions not the image itself
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            returnBmp = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(imageFileUri), null, bmpFactoryOptions);

            int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / dh);
            int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / dw);

            Log.v("HEIGHTRATIO", "" + heightRatio);
            Log.v("WIDTHRATIO", "" + widthRatio);

            // If both of the ratios are greater than 1, one of the sides of the
            // image is greater than the screen
            if (heightRatio > 1 && widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    // Height ratio is larger, scale according to it
                    bmpFactoryOptions.inSampleSize = heightRatio;
                } else {
                    // Width ratio is larger, scale according to it
                    bmpFactoryOptions.inSampleSize = widthRatio;
                }
            }


            // Decode it for real
            bmpFactoryOptions.inJustDecodeBounds = false;
            returnBmp = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(imageFileUri), null, bmpFactoryOptions);
        } catch (FileNotFoundException e) {
            Log.v("ERROR", e.toString());
        }

        return returnBmp;
    }

}