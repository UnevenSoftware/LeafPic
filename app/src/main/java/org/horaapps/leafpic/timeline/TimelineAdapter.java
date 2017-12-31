package org.horaapps.leafpic.timeline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.sort.MediaComparators;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.timeline.data.TimelineModel;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedAdapter;
import org.horaapps.liz.ThemedViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Adapter for showing Timeline.
 * Internally uses {@link MediaAdapter} for rendering items per date
 */
public class TimelineAdapter extends ThemedAdapter<TimelineAdapter.TimelineViewHolder> {

    private ArrayList<TimelineModel> timelineList;

    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();
    private SortingOrder sortingOrder;

    public TimelineAdapter(Context context) {
        super(context);
        timelineList = new ArrayList<>();
        this.sortingOrder = SortingOrder.DESCENDING;
    }

    public void sort() {
        Collections.sort(timelineList, MediaComparators.getTimelineComparator(sortingOrder));
        notifyDataSetChanged();
    }

    public ArrayList<Media> getMedia() {
        return null;
    }

    public boolean clearSelected() {
        return true;
    }

    @Override
    public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View timelineView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_timeline_container, parent, false);

        return new TimelineViewHolder(timelineView);
    }

    public void clearAll() {
        timelineList.clear();
        notifyDataSetChanged();
    }

    public boolean selecting() {
        return false;
    }

    public Observable<Integer> getClicks() {
        return onClickSubject;
    }

    @Override
    public void onBindViewHolder(final TimelineViewHolder holder, int position) {
        holder.setupForModel(timelineList.get(position));
        holder.refreshTheme(getThemeHelper());
    }

    public int add(@NonNull Media media) {
        TimelineModel timelineModel = getTimelineModelForDate(media.getDateModified());
        timelineModel.addMedia(media);

        // TODO: Find the timeline ViewHolder for this and refresh instead of sorting & notifying
        sort();
        notifyDataSetChanged();
        /*
        int i = Collections.binarySearch(
                timelineList, media, MediaComparators.getComparator(sortingMode, sortingOrder));
        if (i < 0) i = ~i;
        media.add(i, media);

        //notifyItemRangeInserted(0, media.size()-1);
        notifyItemInserted(i);
        //notifyDataSetChanged();
        return i;
        */

        return -1;
    }

    private TimelineModel getTimelineModelForDate(long timeInMillis) {
        Date mediaDate = new Date(timeInMillis);
        for (TimelineModel timelineModel : timelineList) {
            Date timelineDate = timelineModel.getDate();
            if (timelineDate.getDate() == mediaDate.getDate()
                    && timelineDate.getMonth() == mediaDate.getMonth()
                    && timelineDate.getYear() == mediaDate.getYear()) return timelineModel;
        }
        TimelineModel timelineModel = new TimelineModel(timeInMillis);
        timelineList.add(timelineModel);
        return timelineModel;
    }

    @Override
    public int getItemCount() {
        return timelineList.size();
    }

    protected static class TimelineViewHolder extends ThemedViewHolder {

        @BindView(R.id.timeline_container_header) TextView headerText;
        @BindView(R.id.timeline_content) RecyclerView timelineContent;

        private MediaAdapter mediaAdapter;

        private TimelineViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            mediaAdapter = new MediaAdapter(view.getContext());
            timelineContent.setLayoutManager(new GridLayoutManager(view.getContext(), 4));
            timelineContent.setAdapter(mediaAdapter);
        }

        private void setupForModel(@NonNull TimelineModel timelineModel) {
            headerText.setText(StringUtils.getUserReadableDate(timelineModel.getDate()));
            mediaAdapter.setMedia(timelineModel.getMedia());
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            headerText.setTextColor(themeHelper.getTextColor());
        }
    }
}
