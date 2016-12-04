package org.horaapps.leafpic.model;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by dnld on 14/08/16.
 */

class MetaDataItem {

//  private static Set<Class<?>> usefullDirectories = new HashSet<Class<?>>();

    private static final int ORIENTATION_NORMAL = 1;
    private static final int ORIENTATION_ROTATE_180 = 3;
    private static final int ORIENTATION_ROTATE_90 = 6;  // rotate 90 cw to right it
    private static final int ORIENTATION_ROTATE_270 = 8;  // rotate 270 to right it

//  static {
//    usefullDirectories.add(ExifIFD0Directory.class);
//    usefullDirectories.add(ExifSubIFDDirectory.class);
//    usefullDirectories.add(GpsDirectory.class);
//    usefullDirectories.add(XmpDirectory.class);
//  }

    private String make = null;
    private String model = null;
    private String fNumber = null;
    private String iso = null;
    private String exposureTime = null;
    private Date dateOriginal = null;
    private GeoLocation location = null;
    private int orientation = -1;

    static MetaDataItem getMetadata(@NonNull File file) {
        return new MetaDataItem(file);
    }

    static String getResolution(InputStream stream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);

        return String.format(Locale.getDefault(),"%dx%d", options.outWidth, options.outHeight);

    }

    private MetaDataItem(File stream) {

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(stream);
            // TODO: 21/08/16 should I switch to ExifInterface or to any other lib?

            handleDirectoryBase(metadata.getFirstDirectoryOfType(ExifIFD0Directory.class));
            ExifSubIFDDirectory dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if(dir != null) {
                dateOriginal = dir.getDateOriginal(TimeZone.getDefault());
                handleDirectoryBase(dir);
            }

            XmpDirectory dir1 = metadata.getFirstDirectoryOfType(XmpDirectory.class);
            if(dir1 != null) {
                if (dir1.containsTag(XmpDirectory.TAG_DATETIME_ORIGINAL))
                    dateOriginal = dir1.getDate(XmpDirectory.TAG_DATETIME_ORIGINAL);

                if (dir1.containsTag(XmpDirectory.TAG_MAKE))
                    make = dir1.getString(XmpDirectory.TAG_MAKE);
                if (dir1.containsTag(XmpDirectory.TAG_MODEL))
                    model = dir1.getString(XmpDirectory.TAG_MODEL);

                if (dir1.containsTag(XmpDirectory.TAG_F_NUMBER))
                    fNumber = dir1.getString(XmpDirectory.TAG_F_NUMBER);
            }

            GpsDirectory d = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if(d != null) location  = d.getGeoLocation();
        } catch (ImageProcessingException e) {
            Log.wtf("asd", "logMainTags: ImageProcessingException", e);
        } catch (IOException e) {
            Log.wtf("asd", "logMainTags: IOException", e);
        }
    }

    private void handleDirectoryBase(ExifDirectoryBase d) {
        if(d != null) {
            if (d.containsTag(ExifDirectoryBase.TAG_MAKE))
                make =d.getString(ExifDirectoryBase.TAG_MAKE);
            if (d.containsTag(ExifDirectoryBase.TAG_MODEL))
                model = d.getString(ExifDirectoryBase.TAG_MODEL);

            if (d.containsTag(ExifDirectoryBase.TAG_ISO_EQUIVALENT))
                iso = d.getString(ExifDirectoryBase.TAG_ISO_EQUIVALENT);
            if (d.containsTag(ExifDirectoryBase.TAG_EXPOSURE_TIME) && d.getRational(ExifDirectoryBase.TAG_EXPOSURE_TIME) != null)
                exposureTime = new DecimalFormat("0.000").format(d.getRational(ExifDirectoryBase.TAG_EXPOSURE_TIME));
            if (d.containsTag(ExifDirectoryBase.TAG_FNUMBER))
                fNumber = d.getString(ExifDirectoryBase.TAG_FNUMBER);

            if (d.containsTag(ExifDirectoryBase.TAG_DATETIME_ORIGINAL))
                dateOriginal = d.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case ORIENTATION_NORMAL: this.orientation = 0;
            case ORIENTATION_ROTATE_90: this.orientation = 90;
            case ORIENTATION_ROTATE_180: this.orientation = 180;
            case ORIENTATION_ROTATE_270: this.orientation = 270;
        }
    }

    Date getDateOriginal() {
        return dateOriginal;
    }

    public GeoLocation getLocation() {
        return location;
    }


    String getCameraInfo() {
        if (make != null && model != null) {
            if (model.contains(make)) return model;
            return String.format("%s %s", make, model);
        }
        return null;
    }

    String getExifInfo() {
        StringBuilder result = new StringBuilder();
        String asd;
        if((asd = getfNumber()) != null) result.append(asd).append(" ");
        if((asd = getExposureTime()) != null) result.append(asd).append(" ");
        if((asd = getIso()) != null) result.append(asd).append(" ");
        return result.length() == 0 ? null : result.toString();
    }

    private String getfNumber() {
        if(fNumber != null)
            return String.format("f/%s", fNumber);
        return null;
    }

    private String getIso() {
        if(iso != null)
            return String.format("ISO-%s", iso);
        return null;
    }

    private String getExposureTime() {
        if(exposureTime != null)
            return String.format("%ss", exposureTime);
        return null;
    }
}
