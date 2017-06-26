package org.horaapps.leafpic.data.provider;

import android.content.Context;
import android.provider.MediaStore;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.ContentHelper;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.filter.FoldersFileFilter;
import org.horaapps.leafpic.data.filter.ImageFileFilter;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.util.PreferenceUtil;

import java.io.File;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;


/**
 * Created by dnld on 24/07/16.
 */

public class CPHelper {


    public static Observable<Album> getAlbums(Context context, boolean hidden, SortingMode sortingMode, SortingOrder sortingOrder) {
        HashSet<String> excluded = new HashSet<>();
        return !hidden ? getAlbums(context, excluded, sortingMode, sortingOrder) : getHiddenAlbums(context, excluded);
    }

    private static Observable<Album> getAlbums(Context context, HashSet<String> excludedAlbums, SortingMode sortingMode, SortingOrder sortingOrder) {

        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Album.getProjection())
                .sort(sortingMode.getColumn())
                .ascending(sortingOrder.isAscending());

        if (PreferenceUtil.getBool(context, "set_include_video", true)) {
            query.selection(String.format("%s=? or %s=?) group by ( %s ",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT));

            query.args(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        } else {
            query.selection(String.format("%s=?) group by ( %s ",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT));
            query.args(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        }


        return QueryUtils.query(query.build(), context.getContentResolver(), Album::new);
    }


    public static Observable<Album> getHiddenAlbums(Context context, HashSet<String> excludedAlbums) {
        return Observable.create(subscriber -> {
            try {
                for (File storage : ContentHelper.getStorageRoots(context))
                    fetchRecursivelyHiddenFolder(context, storage, subscriber, excludedAlbums, PreferenceUtil.getBool(context, "set_include_video", true));
                subscriber.onComplete();
            } catch (Exception err) {
                subscriber.onError(err);
            }
        });
    }


    private static void fetchRecursivelyHiddenFolder(Context context, File dir, ObservableEmitter<Album> emitter, HashSet<String> excludedAlbums, boolean includeVideo) {
        if (!isExcluded(dir.getPath(), excludedAlbums)) {
            File[] folders = dir.listFiles(new FoldersFileFilter());
            if (folders != null) {
                for (File temp : folders) {
                    File nomedia = new File(temp, ".nomedia");
                    if (!isExcluded(temp.getPath(), excludedAlbums) && (nomedia.exists() || temp.isHidden()))
                        checkAndAddFolder(context, temp, emitter, includeVideo);

                    fetchRecursivelyHiddenFolder(context, temp, emitter, excludedAlbums, includeVideo);
                }
            }
        }
    }

    private static void checkAndAddFolder(Context context, File dir, ObservableEmitter<Album> emitter, boolean includeVideo) {
        File[] files = dir.listFiles(new ImageFileFilter(includeVideo));
        if (files != null && files.length > 0) {
            //valid folder
            Album asd = new Album( dir.getAbsolutePath(), dir.getName(), -1, files.length);
            if (!asd.hasCover()) {

                long lastMod = Long.MIN_VALUE;
                File choice = null;
                for (File file : files) {
                    if (file.lastModified() > lastMod) {
                        choice = file;
                        lastMod = file.lastModified();
                    }
                }
                if (choice != null)
                    asd.setCover(choice.getAbsolutePath());
            }

            emitter.onNext(asd);
        }
    }

    private static boolean isExcluded(String path, HashSet<String> excludedAlbums) {
        return false;
        /*for(String s : excludedAlbums) if (path.startsWith(s)) return true;
        return false;*/
    }


    //region Media
    public static Observable<Media> getLastMedia(Context context, long albumId) {
        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Media.getProjection())
                .sort(MediaStore.Images.Media.DATE_TAKEN)
                .ascending(false)
                .limit(1);

        if(PreferenceUtil.getBool(context, "set_include_video", true)) {
            query.selection(String.format("(%s=? or %s=?) and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT));
            query.args(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    albumId);
        } else {
            query.selection(String.format("%s=? and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT));
            query.args(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, albumId);
        }

        return QueryUtils.querySingle(query.build(), context.getContentResolver(), Media::new);
    }

    public static Observable<Media> getMedia(Context context, Album album, SortingMode sortingMode, SortingOrder sortingOrder) {

        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Media.getProjection())
                .sort(sortingMode.getColumn())
                .ascending(sortingOrder.isAscending());

        if(PreferenceUtil.getBool(context, "set_include_video", true)) {
            query.selection(String.format("(%s=? or %s=?) and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT));
            query.args(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    album.getId());
        } else {
            query.selection(String.format("%s=? and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT));
            query.args(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, album.getId());
        }

        return QueryUtils.query(query.build(), context.getContentResolver(), Media::new);
    }
    //endregion
}


