package com.leafpic.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.File;
import java.io.IOException;

/**
 * Created by dnld on 25/02/16.
 */

public class ImageLoaderUtils {

    public static ImageLoader imageLoader;

    public static File getCacheDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return context.getExternalCacheDir();
        }
        return context.getCacheDir();
    }

    public static void initImageLoader(Context context) {
        long discCacheSize = 1024 * 1024;
        DiskCache discCache;
        File dir = getCacheDirectory(context);
        int threadPoolSize;
        discCacheSize *= 512;
        threadPoolSize = 7;
        if (discCacheSize > 0) {
            try {
                discCache = new LruDiskCache(dir, new Md5FileNameGenerator(), discCacheSize);
            } catch (IOException e) {
                discCache = new UnlimitedDiskCache(dir);
            }
        } else {
            discCache = new UnlimitedDiskCache(dir);
        }

        final int memory = (int) (Runtime.getRuntime().maxMemory() / 8);

        fullSizeOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.NONE)
                .cacheInMemory(false)
                .displayer(new FadeInBitmapDisplayer(200))
                .build();

        cropedOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheInMemory(false)
                .displayer(new FadeInBitmapDisplayer(200))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(threadPoolSize)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(discCache)
                .defaultDisplayImageOptions(fullSizeOptions)
                .memoryCacheSize(memory)
                .imageDownloader(new BaseImageDownloader(context))
                .build();

        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

    }
    public static DisplayImageOptions fullSizeOptions;
    public static DisplayImageOptions cropedOptions;

}