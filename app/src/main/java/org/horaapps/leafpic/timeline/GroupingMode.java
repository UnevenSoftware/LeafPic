package org.horaapps.leafpic.timeline;

import android.support.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Grouping for Timeline items.
 */
public enum GroupingMode {

    /**
     * Group the Timeline items by DAY.
     * eg: All media taken on 23rd October, 1994
     */
    DAY {

        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return WEEK.isInGroup(left, right) && isDayOfMonthSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return getFormattedDate(HEADER_PATTERN_DAY, calendar);
        }
    }
    ,
    /**
     * Group the Timeline items by WEEK.
     * eg: All media taken on 4th week of October, 1994
     */
    WEEK {

        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return MONTH.isInGroup(left, right) && isWeekOfMonthSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return "Week " + getFormattedDate(HEADER_PATTERN_WEEK, calendar);
        }
    }
    ,
    /**
     * Group the Timeline items by MONTH.
     * eg: All media taken in October, 1994.
     */
    MONTH {

        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return YEAR.isInGroup(left, right) && isMonthOfYearSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return getFormattedDate(HEADER_PATTERN_MONTH, calendar);
        }
    }
    ,
    /**
     * Group the Timeline items by YEAR.
     * eg: All media taken in 1994.
     */
    YEAR {

        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return isYearSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return getFormattedDate(HEADER_PATTERN_YEAR, calendar);
        }
    }
    ;

    // Must be below Enum constants. Consistency, I'm sorry :)
    private static final String HEADER_PATTERN_DAY = "E, d MMM yyyy";

    private static final String HEADER_PATTERN_WEEK = "W, MMM yyyy";

    private static final String HEADER_PATTERN_MONTH = "MMM yyyy";

    private static final String HEADER_PATTERN_YEAR = "yyyy";

    /**
     * Check if the Calendar for media items belong in the same group.
     *
     * @param left  The calendar instance of media item 1.
     * @param right The calendar instance of media item 2.
     * @return A boolean stating if the media items belong to the same group.
     */
    public abstract boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right);

    /**
     * Get a user-readable header for Timeline header items.
     *
     * @param calendar The calendar instance to get the header text.
     * @return A string to show on the Timeline header items.
     */
    @NonNull
    public abstract String getGroupHeader(@NonNull Calendar calendar);

    public boolean isDayOfMonthSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.DAY_OF_MONTH) == right.get(Calendar.DAY_OF_MONTH);
    }

    public boolean isWeekOfMonthSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.WEEK_OF_MONTH) == right.get(Calendar.WEEK_OF_MONTH);
    }

    public boolean isMonthOfYearSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.MONTH) == right.get(Calendar.MONTH);
    }

    public boolean isYearSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.YEAR) == right.get(Calendar.YEAR);
    }

    @NonNull
    public String getFormattedDate(@NonNull String formatter, @NonNull Calendar calendar) {
        return new SimpleDateFormat(formatter, Locale.ENGLISH).format(calendar.getTime());
    }
}
