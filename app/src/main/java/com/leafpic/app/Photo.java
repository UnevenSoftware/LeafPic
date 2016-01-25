package com.leafpic.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import com.leafpic.app.utils.string;

public class Photo implements Parcelable {


    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    public String Path;
    String ID;
    String FolderPath;
    String DateTaken;
    String name;
    boolean selected;

    public Photo(String path) {
        Path = path;
        FolderPath = string.getBucketPathbyImagePath(path);
        name = string.getPhotoNamebyPath(path);
    }

    public Photo(String path, String dateTaken) {
        Path = path;
        FolderPath = string.getBucketPathbyImagePath(path);
        name = string.getPhotoNamebyPath(path);
        DateTaken = dateTaken;
    }


    public Photo(String nome, String folderPath, String dateTaken) {

        Path = string.getPhotoPathByFolderPathAndName(folderPath, nome);
        FolderPath = folderPath;
        DateTaken = dateTaken;
        name = nome;
    }

    protected Photo(Parcel in) {
        Path = in.readString();
        ID = in.readString();
        FolderPath = in.readString();
        DateTaken = in.readString();
        name = in.readString();
        selected = in.readByte() != 0x00;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean val) {
        selected = val;
    }

    public int getDominantColor() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(Path, options);//.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, 1, 1);
        options.inJustDecodeBounds = false;
        Bitmap b = BitmapFactory.decodeFile(Path, options);
        return b.getPixel(0, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Path);
        dest.writeString(ID);
        dest.writeString(FolderPath);
        dest.writeString(DateTaken);
        dest.writeString(name);
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }
}