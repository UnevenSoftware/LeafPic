package org.horaapps.leafpic.data;

import android.content.Context;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.data.provider.CPHelper;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 27/04/16.
 */
public class HandlingAlbums {

    public static final String TAG = "handling-albums";

    public static final String KEY_EXCLUDED_FOLDERS = "excluded_folders";
    public static final String KEY_INCLUDED_FOLDERS = "included_folders";


    private static final String CACHE_KEY_LOCAL_ALBUMS = "local";
    private static final String CACHE_KEY_HIDDEN_ALBUMS = "hidden";

    private static HandlingAlbums mInstance = null;
    private Context context;

    private HashMap<String, ArrayList<Album>> cachedAlbums;
    private Disposable cachedAlbumsSubscibe;


    private HandlingAlbums(Context context) {
        this.context = context;
        cachedAlbums = new HashMap<>(2);
    }

    public static HandlingAlbums getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HandlingAlbums(context.getApplicationContext());
        }
        return mInstance;
    }

    public Observable<Album> getAlbums(boolean hidden, SortingMode sortingMode, SortingOrder sortingOrder) {

        String cacheKey = hidden ? CACHE_KEY_HIDDEN_ALBUMS : CACHE_KEY_LOCAL_ALBUMS;

        if (cachedAlbums.containsKey(cacheKey)) {
            Log.v(TAG, "using cached albums");
            return getCachedAlums(cacheKey);
        } else {
            Log.v(TAG, "retrieving and caching albums");
            return getAndCacheAlbums(hidden, sortingMode, sortingOrder);
        }
    }

    private Observable<Album> getCachedAlums(String cacheKey) {
        return Observable.create((pub) -> {
            for (Album cachedAlbum : cachedAlbums.get(cacheKey)) {
                pub.onNext(cachedAlbum);
            }

            pub.onComplete();
        });
    }


    private Observable<Album> getAndCacheAlbums(boolean hidden, SortingMode sortingMode, SortingOrder sortingOrder) {

        if (cachedAlbumsSubscibe != null && !cachedAlbumsSubscibe.isDisposed())
            cachedAlbumsSubscibe.dispose();

        ArrayList<Album> albums = new ArrayList<>();
        Observable<Album> observable = hidden ? CPHelper.getHiddenAlbums(context, getExcludedFolders()) : CPHelper.getAlbums(context, getExcludedFolders(), sortingMode, sortingOrder);

        return Observable.create((pub) -> {

            cachedAlbumsSubscibe = observable.subscribeOn(Schedulers.io())
                    .map(album -> album.withSettings(getSettings(album.getPath())))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext((album) -> {
                        pub.onNext(album);
                        albums.add(album);
                    })
                    .doOnError(pub::onError)
                    .doOnComplete(() -> {
                        cachedAlbums.put(hidden ? CACHE_KEY_HIDDEN_ALBUMS : CACHE_KEY_LOCAL_ALBUMS, albums);
                        pub.onComplete();
                    })
                    .subscribe();

        });
    }

    private void invalidateAlbumsCache() {
        cachedAlbums = new HashMap<>(2);
    }

    private HashSet<String> getFolders(String key) {
        return Hawk.get(key, new HashSet<String>());
    }

    public void removeFolderFormExcluded(String path) {
        HashSet<String> folders = getFolders(KEY_EXCLUDED_FOLDERS);
        folders.remove(path);
        Hawk.put(KEY_EXCLUDED_FOLDERS, folders);
        invalidateAlbumsCache();
    }

    public void excludeFolder(String path) {
        HashSet<String> folders = getFolders(KEY_EXCLUDED_FOLDERS);
        folders.add(path);
        Hawk.put(KEY_EXCLUDED_FOLDERS, folders);
        invalidateAlbumsCache();
    }

    public void removeFolderFromIncluded(String path) {
        HashSet<String> folders = getFolders(KEY_INCLUDED_FOLDERS);
        folders.remove(path);
        Hawk.put(KEY_INCLUDED_FOLDERS, folders);
    }

    public void includeFolder(String path) {
        HashSet<String> folders = getFolders(KEY_INCLUDED_FOLDERS);
        folders.add(path);
        Hawk.put(KEY_INCLUDED_FOLDERS, folders);
    }

    public ArrayList<String> getExcludedFolders(Context context) {

        ArrayList<String> list = new ArrayList<>(getFolders(KEY_EXCLUDED_FOLDERS));
        HashSet<File> storageRoots = StorageHelper.getStorageRoots(context);
        for(File file : storageRoots)
            // it has a lot of garbage
            list.add(new File(file.getPath(), "Android").getPath());

        return list;
    }

    public ArrayList<String> getExcludedFolders() {
        return getExcludedFolders(context);
    }

    public ArrayList<String> getIncludedFolders() {
        return new ArrayList<>(getFolders(KEY_INCLUDED_FOLDERS));
    }

    public void setPined(String path, boolean pinned) {
        AlbumSettings settings = getSettings(path);
        settings.pinned = pinned;
        saveSettings(path, settings);
    }

    public void setCover(String path, String mediaPath) {
        AlbumSettings settings = getSettings(path);
        settings.coverPath = mediaPath;
        saveSettings(path, settings);
    }

    public void setSortingMode(String path, int column) {
        AlbumSettings settings = getSettings(path);
        settings.sortingMode = column;
        saveSettings(path, settings);
    }

    public void setSortingOrder(String path, int sortingOrder) {
        AlbumSettings settings = getSettings(path);
        settings.sortingOrder = sortingOrder;
        saveSettings(path, settings);
    }

    private static AlbumSettings getSettings(String path) {
//        Timer timer = new Timer("new-settings");
//        timer.start();
        return Hawk.get(getSettingsKey(path), AlbumSettings.getDefaults());
//        timer.stop();
        //albumSettings;
    }

    private static void saveSettings(String path, AlbumSettings settings) {
        Hawk.put(getSettingsKey(path), settings);
    }

    private static String getSettingsKey(String path) {
        return String.format("%s_%s", "albums_settings", path);
    }

}
