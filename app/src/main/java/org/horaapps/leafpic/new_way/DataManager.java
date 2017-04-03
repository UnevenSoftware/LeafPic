package org.horaapps.leafpic.new_way;


import android.content.Context;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.ReplayRelay;

import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.Media;
import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;


public class DataManager {

    private static final String TAG = "DataManager";

    private static DataManager instance;

    private Subscription songsSubscription;
    private ReplayRelay<List<Media>> songsRelay = ReplayRelay.create();

    private Subscription albumsSubscription;
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
        ReplaySubject<Album> as = ReplaySubject.create();
        CPHelper.getAlbums(context, hidden)
                .subscribe(as);
        return as;
    }


/*

    public Observable<List<Media>> getSongsRelay() {

        if (songsSubscription == null || songsSubscription.isUnsubscribed()) {

            Observable<List<Media>> songsObservable = SqlBriteUtils.createContinuousQuery(ShuttleApplication.getInstance(), Song::new, Song.getQuery());

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
            }).subscribe(songsRelay, error -> Crashlytics.log("getSongsRelay error: " + error.getMessage()));
        }
        return songsRelay.subscribeOn(Schedulers.io()).map(ArrayList::new);
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
                    .wrapDatabaseHelper(new WhitelistDbOpenHelper(ShuttleApplication.getInstance()), Schedulers.io());
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