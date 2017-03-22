package org.horaapps.leafpic.new_way;

import android.content.Context;
import android.provider.MediaStore;

import org.horaapps.leafpic.App;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.Media;
import org.horaapps.leafpic.model.base.FoldersFileFilter;
import org.horaapps.leafpic.model.base.ImageFileFilter;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.PreferenceUtil;

import java.io.File;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;


/**
 * Created by dnld on 24/07/16.
 */

public class CPHelper {


    public static Observable<Album> getAlbums(boolean hidden) {
        HashSet<String> excluded = new HashSet<>();
        return !hidden ? getAlbums(App.getInstance(), excluded) : getHiddenAlbums(App.getInstance(), excluded);
    }

    private static Observable<Album> getAlbums(Context context, HashSet<String> excludedAlbums) {

        String selection, selectionArgs[];

        if (PreferenceUtil.getBool(context, "set_include_video", true)) {
            selection = String.format("%s=? or %s=?) group by ( %s ",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);

            selectionArgs = new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
            };
        } else {
            selection = String.format("%s=?) group by ( %s ",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);
            selectionArgs = new String[] { String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) };
        }

        return QueryUtils.query(
                new Query.Builder()
                        .uri(MediaStore.Files.getContentUri("external"))
                        .projection(Album.projection)
                        .selection(selection)
                        .sort(MediaStore.Images.Media.DATE_MODIFIED)
                        .ascending(false)
                        .args(selectionArgs).build(), context.getContentResolver(), Album::new);
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


    //region Media
    public static Observable<Media> getLastMedia(Context context, long albumId) {
        return getMedia(context, albumId, 1);
    }

    private static Observable<Media> getMedia(Context context, long albumId, int n) {
        String selection, selectionArgs[];

        if(PreferenceUtil.getBool(context, "set_include_video", true)) {
            selection = String.format("(%s=? or %s=?) and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);
            selectionArgs = new String[] {
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                    String.valueOf(albumId)
            };
        } else {
            selection = String.format("%s=? and %s=?",
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.PARENT);
            selectionArgs = new String[] { String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String.valueOf(albumId) };
        }

        return QueryUtils.query(new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Media.getProjection())
                .selection(selection)
                .args(selectionArgs)
                .sort(MediaStore.Images.Media.DATE_MODIFIED)
                .ascending(false)
                .limit(n).build(), context.getContentResolver(), Media::new);
    }
    //endregion

    private static boolean isExcluded(String path, HashSet<String> excludedAlbums) {
        return false;
        /*for(String s : excludedAlbums) if (path.startsWith(s)) return true;
        return false;*/
    }
}


