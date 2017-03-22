package org.horaapps.leafpic.new_way;

import android.content.ContentResolver;
import android.database.Cursor;

import io.reactivex.Observable;


/**
 * Created by dnld on 3/13/17.
 */

public class QueryUtils {

    public static <T> Observable<T> query(Query q, ContentResolver cr, CursorHandler<T> ch) {
        return Observable.create(subscriber -> {
            Cursor cursor = null;
            try {
                cursor = q.getCursor(cr);
                if (cursor != null && cursor.getCount() > 0)
                    while (cursor.moveToNext()) subscriber.onNext(ch.handle(cursor));
                subscriber.onComplete();
            } catch (Exception err) {
                subscriber.onError(err);
            } finally {
                if (cursor != null) cursor.close();
            }
        });
    }
}
