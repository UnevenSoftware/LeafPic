package org.horaapps.leafpic.new_way;


import android.content.Context;

import com.jakewharton.rxrelay2.PublishRelay;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.provider.CPHelper;

import java.util.List;

import io.reactivex.Observable;


public class DataManager {

    private static final String TAG = "DataManager";

    private static DataManager instance;

    private PublishRelay<Media> mediaRelay = PublishRelay.create();
    private PublishRelay<Album> albumsRelay = PublishRelay.create();



   /* private BriteDatabase whitelistDatabase;
    private Subscription whitelistSubscription;
    private BehaviorRelay<List<WhitelistFolder>> whitelistRelay = BehaviorRelay.create();*/

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager() {

    }

    public Observable<Album> getAlbumsRelay(Context context, boolean hidden) {
        CPHelper.getAlbums(context, hidden)
                .subscribe(albumsRelay);
        return albumsRelay;
    }

    public Observable<Media> getMediaRelay(Context context, Album album) {
        CPHelper.getMedia(context, album)
                .subscribe(mediaRelay);

        return mediaRelay;
    }


/*

    public Observable<List<Media>> getSongsRelay() {

        if (songsSubscription == null || songsSubscription.isUnsubscribed()) {

            Observable<List<Media>> songsObservable = SqlBriteUtils.createContinuousQuery(ShuttleApplication.make(), Song::new, Song.getQuery());

            songsSubscription = (songs) ->
            {
                List<Media> result = songs;

                //Filter out blacklisted songs
                if (!blacklistedSongs.isEmpty()) {
                    result = Stream.of(songs)
                            .filter(song -> !Stream.of(blacklistedSongs)
                                    .anyMatch(blacklistedSong -> blacklistedSong.songId == song.id))
                            .collect(Collectors.toList());
                }

                //Filter out non-whitelisted folders
                if (!whitelistFolders.isEmpty()) {
                    result = Stream.of(result)
                            .filter(song -> Stream.of(whitelistFolders)
                                    .anyMatch(whitelistFolder -> StringUtils.containsIgnoreCase(song.path, whitelistFolder.folder)))
                            .collect(Collectors.toList());
                }

                return result;
            }).subscribe(mediaRelay, error -> Crashlytics.log("getSongsRelay error: " + error.getMessage()));
        }
        return mediaRelay.subscribeOn(Schedulers.io()).map(ArrayList::new);
    }






    public Observable<List<Media>> getSongsObservable(Func1<Media, Boolean> predicate) {
        return getSongsRelay()
                .first()
                .flatMap(Observable::from)
                .filter(predicate).toList();
    }
*/



   /* *//**
     * @return a {@link BriteDatabase} wrapping the whitelist SqliteOpenHelper.
     *//*
    public BriteDatabase getWhitelistDatabase() {
        if (whitelistDatabase == null) {
            whitelistDatabase = new SqlBrite.Builder().build()
                    .wrapDatabaseHelper(new WhitelistDbOpenHelper(ShuttleApplication.make()), Schedulers.io());
        }
        return whitelistDatabase;
    }

    *//**
     * @return a <b>continuous</b> stream of {@link List<WhitelistFolder>>}, backed by a behavior relay for caching getCursor results.
     *//*
    private Observable<List<WhitelistFolder>> getWhitelistRelay() {
        if (whitelistSubscription == null || whitelistSubscription.isUnsubscribed()) {
            whitelistSubscription = getWhitelistDatabase()
                    .createQuery(WhitelistDbOpenHelper.TABLE_FOLDERS, "SELECT * FROM " + WhitelistDbOpenHelper.TABLE_FOLDERS)
                    .mapToList(WhitelistFolder::new)
                    .subscribe(whitelistRelay, error -> Crashlytics.log("getWhitelistRelay error: " + error.getMessage()));
        }
        return whitelistRelay.subscribeOn(Schedulers.io()).map(ArrayList::new);
    }*/
}