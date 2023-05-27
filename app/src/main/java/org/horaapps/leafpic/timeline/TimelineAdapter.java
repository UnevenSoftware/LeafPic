package org.horaapps.leafpic.timeline;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.items.ActionsListener;
import org.horaapps.leafpic.timeline.data.TimelineHeaderModel;
import org.horaapps.leafpic.timeline.data.TimelineItem;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedAdapter;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.horaapps.leafpic.timeline.ViewHolder.TimelineHeaderViewHolder;
import static org.horaapps.leafpic.timeline.ViewHolder.TimelineMediaViewHolder;
import static org.horaapps.leafpic.timeline.ViewHolder.TimelineViewHolder;

/**
 * Adapter for showing Timeline.
 */
public class TimelineAdapter extends ThemedAdapter<TimelineViewHolder> {

    private List<TimelineItem> timelineItems;

    private ArrayList<Media> mediaItems;

    private SortingOrder sortingOrder;

    private GroupingMode groupingMode;

    private int timelineGridSize;

    private final ActionsListener actionsListener;

    /**
     * This set maintains the selected positions by user.
     */
    private Set<Integer> selectedPositions;

    public TimelineAdapter(@NonNull Context context, ActionsListener actionsListener, int timelineGridSize) {
        super(context);
        timelineItems = new ArrayList<>();
        this.timelineGridSize = timelineGridSize;
        this.sortingOrder = SortingOrder.DESCENDING;
        selectedPositions = new HashSet<>();
        this.actionsListener = actionsListener;
    }

    public ArrayList<Media> getMedia() {
        return mediaItems;
    }

    public boolean clearSelected() {
        Set<Integer> oldSelections = new HashSet<>(selectedPositions);
        selectedPositions.clear();
        for (int selectedPos : oldSelections) notifyItemChanged(selectedPos);
        return true;
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public int getMediaCount() {
        return mediaItems.size();
    }

    /**
     * Get the list of Selected Media.
     *
     * @return A list containing the selected Media items.
     */
    public List<Media> getSelectedMedia() {
        List<Media> selectedMedia = new ArrayList<>();
        for (int selectedPos : selectedPositions) {
            selectedMedia.add((Media) timelineItems.get(selectedPos));
        }
        return selectedMedia;
    }

    /**
     * Select all elements within the Timeline view.
     */
    public void selectAll() {
        int timelineItemSize = timelineItems.size();
        for (int pos = 0; pos < timelineItemSize; pos++) {
            TimelineItem timelineItem = getItem(pos);
            if (timelineItem.getTimelineType() == TimelineItem.TYPE_HEADER)
                continue;
            // Select the element
            selectedPositions.add(pos);
        }
        notifyDataSetChanged();
        actionsListener.onSelectionCountChanged(selectedPositions.size(), mediaItems.size());
    }

    /**
     * Set the grouping mode (DAY, WEEK, MONTH, YEAR) of the Timeline.
     */
    public void setGroupingMode(@NonNull GroupingMode groupingMode) {
        this.groupingMode = groupingMode;
        if (mediaItems == null)
            return;
        // Rebuild the Timeline Items
        buildTimelineItems();
    }

    /**
     * Set the sorting order (ASCENDING, DESCENDING) of the Timeline.
     */
    public void setSortingOrder(@NonNull SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder;
        notifyDataSetChanged();
    }

    public void setTimelineGridSize(int timelineGridSize) {
        this.timelineGridSize = timelineGridSize;
    }

    @NonNull
    @Override
    public ViewHolder.TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == TimelineItem.TYPE_HEADER) {
            return new TimelineHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_timeline_header, parent, false));
        } else
            return new TimelineMediaViewHolder(LayoutInflater.from(context).inflate(R.layout.card_photo, parent, false), ThemeHelper.getPlaceHolder(context));
    }

    public void setGridLayoutManager(GridLayoutManager gridLayoutManager) {
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                TimelineItem timelineItem = getItem(position);
                // If we have a header item, occupy the entire width
                if (timelineItem.getTimelineType() == TimelineItem.TYPE_HEADER)
                    return timelineGridSize;
                // Else, a media item takes up a single space
                return 1;
            }
        });
    }

    private void clearAll() {
        timelineItems.clear();
    }

    public boolean isSelecting() {
        return !selectedPositions.isEmpty();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getTimelineType();
    }

    @NonNull
    private TimelineItem getItem(int position) {
        return timelineItems.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        TimelineItem timelineItem = getItem(position);
        if (viewHolder instanceof TimelineHeaderViewHolder) {
            TimelineHeaderViewHolder headerViewHolder = (TimelineHeaderViewHolder) viewHolder;
            headerViewHolder.bind((TimelineHeaderModel) timelineItem);
        } else if (viewHolder instanceof TimelineMediaViewHolder) {
            TimelineMediaViewHolder mediaHolder = (TimelineMediaViewHolder) viewHolder;
            mediaHolder.bind((Media) timelineItem, selectedPositions.contains(position));
            mediaHolder.layout.setOnClickListener(v -> {
                if (isSelecting())
                    triggerSelection(mediaHolder.getAdapterPosition());
                else
                    displayMedia(timelineItem);
            });
            mediaHolder.layout.setOnLongClickListener(v -> {
                if (isSelecting())
                    triggerSelectionAllUpTo(mediaHolder.getAdapterPosition());
                else
                    triggerSelection(mediaHolder.getAdapterPosition());
                return true;
            });
        }
    }

    private void displayMedia(TimelineItem timelineItem) {
        for (int pos = 0; pos < mediaItems.size(); pos++) {
            Media mediaItem = mediaItems.get(pos);
            if (mediaItem.equals(timelineItem)) {
                actionsListener.onItemSelected(pos);
                return;
            }
        }
    }

    private void triggerSelection(int elementPos) {
        int oldCount = selectedPositions.size();
        if (selectedPositions.contains(elementPos))
            selectedPositions.remove(elementPos);
        else
            selectedPositions.add(elementPos);
        if (oldCount == 0 && isSelecting())
            actionsListener.onSelectMode(true);
        else if (oldCount == 1 && !isSelecting())
            actionsListener.onSelectMode(false);
        else
            actionsListener.onSelectionCountChanged(selectedPositions.size(), mediaItems.size());
        notifyItemChanged(elementPos);
    }

    private void triggerSelectionAllUpTo(int elemPos) {
        int indexRightBeforeOrAfter = -1, minOffset = Integer.MAX_VALUE;
        for (Integer selectedPosition : selectedPositions) {
            int offset = Math.abs(elemPos - selectedPosition);
            if (offset < minOffset) {
                minOffset = offset;
                indexRightBeforeOrAfter = selectedPosition;
            }
        }
        if (indexRightBeforeOrAfter != -1) {
            for (int index = Math.min(elemPos, indexRightBeforeOrAfter); index <= Math.max(elemPos, indexRightBeforeOrAfter); index++) {
                if (timelineItems.get(index) != null && timelineItems.get(index) instanceof Media) {
                    selectedPositions.add(index);
                    notifyItemChanged(index);
                }
            }
            actionsListener.onSelectionCountChanged(selectedPositions.size(), mediaItems.size());
        }
    }

    public void setMedia(@NonNull ArrayList<Media> mediaList) {
        mediaItems = mediaList;
        selectedPositions.clear();
        buildTimelineItems();
    }

    private void buildTimelineItems() {
        clearAll();
        timelineItems = getTimelineItems(mediaItems);
        notifyDataSetChanged();
    }

    /**
     * Get the list of Timeline Items to show.
     * Internally adds the headers to the list.
     *
     * @param mediaList The list of media items to show.
     * @return A list with headers to be inflated for Timeline.
     */
    private List<TimelineItem> getTimelineItems(@NonNull List<Media> mediaList) {
        // Preprocessing - Add headers in the list of media
        // TODO: Think of ways to optimise / improve this logic
        List<TimelineItem> timelineItemList = new ArrayList<>();
        int headersAdded = 0;
        Calendar currentDate = null;
        for (int position = 0; position < mediaList.size(); position++) {
            Calendar mediaDate = new GregorianCalendar();
            mediaDate.setTimeInMillis(mediaList.get(position).getDateModified());
            if (currentDate == null || !groupingMode.isInGroup(currentDate, mediaDate)) {
                currentDate = mediaDate;
                TimelineHeaderModel timelineHeaderModel = new TimelineHeaderModel(mediaDate);
                timelineHeaderModel.setHeaderText(groupingMode.getGroupHeader(mediaDate));
                timelineItemList.add(position + headersAdded, timelineHeaderModel);
                headersAdded++;
            }
            timelineItemList.add(mediaList.get(position));
        }
        return timelineItemList;
    }

    @Override
    public int getItemCount() {
        return timelineItems.size();
    }

    /**
     * Removes an item from this Timeline adapter.
     *
     * @param item The item to remove.
     */
    public void removeItem(@Nullable Media item) {
        for (int pos = 0; pos < timelineItems.size(); pos++) {
            TimelineItem timelineItem = timelineItems.get(pos);
            if (timelineItem.getTimelineType() == TimelineItem.TYPE_HEADER)
                continue;
            Media mediaItem = (Media) timelineItem;
            if (!mediaItem.equals(item))
                continue;
            timelineItems.remove(pos);
            notifyItemRemoved(pos);
            break;
        }
    }

    public static class TimelineItemDecorator extends RecyclerView.ItemDecoration {

        private int pixelOffset;

        public TimelineItemDecorator(@NonNull Context context, @DimenRes int dimenRes) {
            pixelOffset = context.getResources().getDimensionPixelOffset(dimenRes);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(pixelOffset, pixelOffset, pixelOffset, pixelOffset);
        }
    }
}
