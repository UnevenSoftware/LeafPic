package org.horaapps.leafpic.timeline.data;

import android.support.annotation.NonNull;

import org.horaapps.leafpic.data.Media;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model for showing the Timeline.
 * Contains media for a given Timeline (as per date, month, year)
 */
public class TimelineModel {

    private Date date;
    private ArrayList<Media> mediaList;

    public TimelineModel(long timeInMillis) {
        this(timeInMillis, new ArrayList<>());
    }

    public TimelineModel(long timeInMillis, ArrayList<Media> mediaList) {
        this.date = new Date(timeInMillis);
        this.mediaList = mediaList;
    }

    public Date getDate() {
        return date;
    }

    public void addMedia(@NonNull Media media) {
        mediaList.add(media);
    }

    public List<Media> getMedia() {
        return mediaList;
    }
}