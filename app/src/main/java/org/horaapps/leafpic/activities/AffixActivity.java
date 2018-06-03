package org.horaapps.leafpic.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

import static android.content.ContentValues.TAG;

/**
 * The Affix Activity used to merge pictures into one.
 */
public class AffixActivity extends Activity implements OnClickListener {

    static final int PICKED_ONE = 0;
    static final int PICKED_TWO = 1;

    boolean onePicked = false;
    boolean twoPicked = false;

    Button choosePicture1, choosePicture2;
    ImageView compositeImageView;

    Bitmap bmp1, bmp2;
    Canvas canvas;
    Paint paint;

    private ArrayList<String> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affix);
        compositeImageView = (ImageView) this
                .findViewById(R.id.CompositeImageView);

        choosePicture1 = (Button) this.findViewById(R.id.ChoosePictureButton1);
        choosePicture2 = (Button) this.findViewById(R.id.ChoosePictureButton2);

        choosePicture1.setOnClickListener(this);
        choosePicture2.setOnClickListener(this);


    }

    public void onClick(View v) {

        int which = -1;

        if (v == choosePicture1) {
            which = PICKED_ONE;
        } else if (v == choosePicture2) {
            which = PICKED_TWO;
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
                onePicked = true;
            } else if (requestCode == PICKED_TWO) {
                bmp2 = loadBitmap(imageFileUri);
                twoPicked = true;
            }

            if (onePicked && twoPicked) {
                int w = bmp1.getWidth() + bmp2.getWidth();
                int h;
                if(bmp1.getHeight() >= bmp2.getHeight()){
                    h = bmp1.getHeight();
                }else{
                    h = bmp2.getHeight();
                }
                Bitmap.Config config = bmp1.getConfig();
                if(config == null){
                    config = Bitmap.Config.ARGB_8888;
                }

                Bitmap drawingBitmap = Bitmap.createBitmap(w,
                        h, config);
                canvas = new Canvas(drawingBitmap);
                canvas.drawBitmap(bmp1,  0, 0, null);
                canvas.drawBitmap(bmp2, bmp1.getWidth(), 0, null);;

                try {
                    compositeImageView.setImageBitmap(drawingBitmap);
                    BitmapDrawable draw = (BitmapDrawable) compositeImageView.getDrawable();
                    Bitmap bitmap1 = draw.getBitmap();

                    FileOutputStream outStream = null;
                    File sdCard = Environment.getExternalStorageDirectory();
                    File dir = new File(sdCard.getAbsolutePath() + "/YourFolderName");
                    dir.mkdirs();
                    String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    File outFile = new File(dir, fileName);
                    outStream = new FileOutputStream(outFile);
                    bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                }catch (IOException e) {
                    Log.v(TAG, "FileNotFoundExceptionError " + e.toString());
                }
            }
        }
    }
    private Bitmap loadBitmap(Uri imageFileUri) {
        Display currentDisplay = getWindowManager().getDefaultDisplay();

        float dw = currentDisplay.getWidth();
        float dh = currentDisplay.getHeight();

        Bitmap returnBmp = Bitmap.createBitmap((int) dw, (int) dh,
                Bitmap.Config.ARGB_4444);

        try {
            /** Load up the image's dimensions not the image itself */
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            returnBmp = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(imageFileUri), null, bmpFactoryOptions);

            int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / dh);
            int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / dw);

            Log.v("HEIGHTRATIO", "" + heightRatio);
            Log.v("WIDTHRATIO", "" + widthRatio);

            /** If both of the ratios are greater than 1, one of the sides of the
             *  image is greater than the screen */
            if (heightRatio > 1 && widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    // Height ratio is larger, scale according to it
                    bmpFactoryOptions.inSampleSize = heightRatio;
                } else {
                    // Width ratio is larger, scale according to it
                    bmpFactoryOptions.inSampleSize = widthRatio;
                }
            }


            /** Decode it for real */
            bmpFactoryOptions.inJustDecodeBounds = false;
            returnBmp = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(imageFileUri), null, bmpFactoryOptions);
        } catch (FileNotFoundException e) {
            Log.v("ERROR", e.toString());
        }

        return returnBmp;
    }

}
