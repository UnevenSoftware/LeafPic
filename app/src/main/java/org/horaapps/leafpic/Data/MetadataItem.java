package org.horaapps.leafpic.Data;

import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.adobe.AdobeJpegDirectory;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifDescriptorBase;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileMetadataDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.icc.IccDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.jfif.JfifDirectory;
import com.drew.metadata.jfxx.JfxxDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.pcx.PcxDirectory;
import com.drew.metadata.photoshop.DuckyDirectory;
import com.drew.metadata.photoshop.PhotoshopDirectory;
import com.drew.metadata.photoshop.PsdHeaderDescriptor;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;


/**
 * Created by dnld on 14/08/16.
 */




public class MetadataItem {

  private static Set<Class<?>> usefullDirectories = new HashSet<Class<?>>();

  private static final int ORIENTATION_NORMAL = 1;
  private static final int ORIENTATION_ROTATE_180 = 3;
  private static final int ORIENTATION_ROTATE_90 = 6;  // rotate 90 cw to right it
  private static final int ORIENTATION_ROTATE_270 = 8;  // rotate 270 to right it

  static {
    usefullDirectories.add(ExifIFD0Directory.class);
    usefullDirectories.add(ExifSubIFDDirectory.class);
    usefullDirectories.add(BmpHeaderDirectory.class);

    usefullDirectories.add(GifHeaderDirectory.class);
    usefullDirectories.add(JpegDirectory.class);
    usefullDirectories.add(PngDirectory.class);
    usefullDirectories.add(WebpDirectory.class);
    usefullDirectories.add(GpsDirectory.class);
    usefullDirectories.add(XmpDirectory.class);

  }

  private int width = -1;
  private int height = -1;
  private String make = null;
  private String model = null;
  private String fNumber = null;
  private String iso = null;
  private String exposureTime = null;
  private Date dateOriginal = null;
  private GeoLocation location = null;
  private int orientation = -1;

  MetadataItem(File file) {
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(file);

      for(Directory directory : metadata.getDirectories()) {

        if (usefullDirectories.contains(directory.getClass())) {
          if (directory.getClass().equals(ExifSubIFDDirectory.class) || directory.getClass().equals(ExifIFD0Directory.class)) {
            ExifDirectoryBase d = (ExifDirectoryBase) directory;

            if (d.containsTag(ExifDirectoryBase.TAG_IMAGE_WIDTH))
              setWidth(d.getInt(ExifDirectoryBase.TAG_IMAGE_WIDTH));
            if (d.containsTag(ExifDirectoryBase.TAG_IMAGE_HEIGHT))
              setHeight(d.getInt(ExifDirectoryBase.TAG_IMAGE_HEIGHT));

            if (d.containsTag(ExifDirectoryBase.TAG_MAKE))
              setMake(d.getString(ExifDirectoryBase.TAG_MAKE));
            if (d.containsTag(ExifDirectoryBase.TAG_MODEL))
              setModel(d.getString(ExifDirectoryBase.TAG_MODEL));

            if (d.containsTag(ExifDirectoryBase.TAG_ISO_EQUIVALENT))
              setIso(d.getString(ExifDirectoryBase.TAG_ISO_EQUIVALENT));
            if (d.containsTag(ExifDirectoryBase.TAG_EXPOSURE_TIME) && d.getRational(ExifDirectoryBase.TAG_EXPOSURE_TIME) != null)
              setExposureTime(new DecimalFormat("0.000").format(d.getRational(ExifDirectoryBase.TAG_EXPOSURE_TIME)));
            if (d.containsTag(ExifDirectoryBase.TAG_FNUMBER))
              setfNumber(d.getString(ExifDirectoryBase.TAG_FNUMBER));

            if (d.containsTag(ExifDirectoryBase.TAG_DATETIME_ORIGINAL))
              setDateOriginal(d.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL));

          } else if (directory.getClass().equals(JpegDirectory.class)) {
            JpegDirectory d = (JpegDirectory) directory;

            setWidth(d.getInt(JpegDirectory.TAG_IMAGE_WIDTH));
            setHeight(d.getInt(JpegDirectory.TAG_IMAGE_HEIGHT));
          } else if (directory.getClass().equals(PngDirectory.class)) {
            PngDirectory d = (PngDirectory) directory;

            setWidth(d.getInt(PngDirectory.TAG_IMAGE_WIDTH));
            setHeight(d.getInt(PngDirectory.TAG_IMAGE_HEIGHT));
          } else if (directory.getClass().equals(GifHeaderDirectory.class)) {
            GifHeaderDirectory d = (GifHeaderDirectory) directory;

            setWidth(d.getInt(GifHeaderDirectory.TAG_IMAGE_WIDTH));
            setHeight(d.getInt(GifHeaderDirectory.TAG_IMAGE_HEIGHT));
          } else if (directory.getClass().equals(ExifSubIFDDirectory.class)) {
            setDateOriginal(((ExifSubIFDDirectory) directory).getDateOriginal(TimeZone.getDefault()));
          } else if (directory.getClass().equals(WebpDirectory.class)) {
            WebpDirectory d = (WebpDirectory) directory;

            setWidth(d.getInt(WebpDirectory.TAG_IMAGE_WIDTH));
            setHeight(d.getInt(WebpDirectory.TAG_IMAGE_HEIGHT));
          } else if (directory.getClass().equals(XmpDirectory.class)) {
            XmpDirectory d = (XmpDirectory) directory;

            if (d.containsTag(XmpDirectory.TAG_DATETIME_ORIGINAL))
              setDateOriginal(d.getDate(XmpDirectory.TAG_DATETIME_ORIGINAL));

            if (d.containsTag(XmpDirectory.TAG_MAKE))
              setMake(d.getString(XmpDirectory.TAG_MAKE));
            if (d.containsTag(XmpDirectory.TAG_MODEL))
              setModel(d.getString(XmpDirectory.TAG_MODEL));

            if (d.containsTag(XmpDirectory.TAG_F_NUMBER))
              setfNumber(d.getString(XmpDirectory.TAG_F_NUMBER));
          }
          else if (directory.getClass().equals(GpsDirectory.class)) {
            GpsDirectory d = (GpsDirectory) directory;
            setLocation(d.getGeoLocation());
          }
        }
      }

    } catch (ImageProcessingException e) {
      Log.wtf("asd", "logMainTags: ImageProcessingException", e);
    } catch (IOException e) {
      Log.wtf("asd", "logMainTags: IOException", e);
    } catch (MetadataException e) {
      e.printStackTrace();
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

  public Date getDateOriginal() {
    return dateOriginal;
  }

  public void setDateOriginal(Date dateOriginal) {
    this.dateOriginal = dateOriginal;
  }
  public GeoLocation getLocation() {
    return location;
  }

  public void setLocation(GeoLocation location) {
    this.location = location;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public String getResolution() {
    if(width !=-1 && height != -1)
      return String.format("%dx%d", getWidth(), getHeight());
    return null;
  }

  public String getCameraInfo() {

    if (make != null && model != null) {
      if (model.contains(make)) return model;
      return String.format("%s %s", make, model);
    }
    return null;
  }

  public String getExifInfo() {
    StringBuilder result = new StringBuilder();
    String asd;
    if((asd = getfNumber()) != null) result.append(asd).append(" ");
    if((asd = getExposureTime()) != null) result.append(asd).append(" ");
    if((asd = getIso()) != null) result.append(asd).append(" ");
    return result.length() == 0 ? null : result.toString();
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getMake() {
    return make;
  }

  public void setMake(String make) {
    this.make = make;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getfNumber() {
    if(fNumber != null)
      return String.format("f/%s", fNumber);
    return null;
  }

  public void setfNumber(String fNumber) {
    this.fNumber = fNumber;
  }

  public String getIso() {
    if(iso != null)
      return String.format("ISO-%s", iso);
    return null;
  }

  public void setIso(String iso) {
    this.iso = iso;
  }

  public String getExposureTime() {
    if(exposureTime != null)
      return String.format("%ss", exposureTime);
    return null;
  }

  public void setExposureTime(String exposureTime) {
    this.exposureTime = exposureTime;
  }
}
