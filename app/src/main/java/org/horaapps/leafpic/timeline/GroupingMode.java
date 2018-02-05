package org.horaapps.leafpic.timeline;

import android.support.annotation.NonNull;

import java.util.Calendar;

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
    },

    /**
     * Group the Timeline items by WEEK.
     * eg: All media taken on 4th week of October, 1994
     */
    WEEK {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return MONTH.isInGroup(left, right) && isWeekOfMonthSame(left, right);
        }
    },

    /**
     * Group the Timeline items by MONTH.
     * eg: All media taken in October, 1994.
     */
    MONTH {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return YEAR.isInGroup(left, right) && isMonthOfYearSame(left, right);
        }
    },

    /**
     * Group the Timeline items by YEAR.
     * eg: All media taken in 1994.
     */
    YEAR {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return isYearSame(left, right);
        }
    };

    public abstract boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right);

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
}
