package org.horaapps.leafpic.timeline.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Model for showing the Timeline headers.
 */
public class TimelineHeaderModel implements TimelineItem {

    private Calendar calendar;
    private String headerText;

    public TimelineHeaderModel(@NonNull Date date) {
        this(date.getTime());
    }

    public TimelineHeaderModel(long timeInMillis) {
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timeInMillis);
    }

    public TimelineHeaderModel(@NonNull Calendar calendar) {
        this.calendar = calendar;
    }

    public void setHeaderText(@NonNull String headerText) {
        this.headerText = headerText;
    }

    @NonNull
    public Calendar getDate() {
        return calendar;
    }

    @Nullable
    public String getHeaderText() {
        return headerText;
    }

    @Override
    public int getTimelineType() {
        return TYPE_HEADER;
    }
}
