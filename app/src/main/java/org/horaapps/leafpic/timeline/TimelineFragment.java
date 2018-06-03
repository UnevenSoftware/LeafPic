package org.horaapps.leafpic.timeline;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.filter.FilterMode;
import org.horaapps.leafpic.data.filter.MediaFilter;
import org.horaapps.leafpic.data.provider.CPHelper;
import org.horaapps.leafpic.data.sort.MediaComparators;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.fragments.BaseFragment;
import org.horaapps.leafpic.interfaces.MediaClickListener;
import org.horaapps.leafpic.util.DeviceUtils;
import org.horaapps.leafpic.util.preferences.Defaults;
import org.horaapps.liz.ThemeHelper;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment which shows the Timeline.
 */
public class TimelineFragment extends BaseFragment {

    public static final String TAG = "TimelineFragment";

    private static final String ARGS_ALBUM = "args_album";

    private static final String KEY_ALBUM = "key_album";
    private static final String KEY_GROUPING_MODE = "key_grouping_mode";
    private static final String KEY_FILTER_MODE = "key_filter_mode";

    @BindView(R.id.timeline_items) RecyclerView timelineItems;
    @BindView(R.id.timeline_swipe_refresh_layout) SwipeRefreshLayout refreshLayout;

    private TimelineAdapter timelineAdapter;
    private MediaClickListener timelineListener;
    private GridLayoutManager gridLayoutManager;

    private Album contentAlbum;

    private GroupingMode groupingMode;
    private FilterMode filterMode;

    public static TimelineFragment newInstance(@NonNull Album album) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MediaClickListener) {
            timelineListener = (MediaClickListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            contentAlbum = savedInstanceState.getParcelable(KEY_ALBUM);
            groupingMode = (GroupingMode) savedInstanceState.get(KEY_GROUPING_MODE);
            filterMode = (FilterMode) savedInstanceState.get(KEY_FILTER_MODE);
            return;
        }

        /** Get content from arguments */
        Bundle arguments = getArguments();
        if (arguments == null) return;
        contentAlbum = arguments.getParcelable(ARGS_ALBUM);

        /** Set defaults */
        groupingMode = GroupingMode.DAY;
        filterMode = FilterMode.ALL;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setOnRefreshListener(this::loadAlbum);
        setupRecyclerView();
        loadAlbum();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timeline, menu);

        menu.findItem(getMenuForGroupingMode(groupingMode)).setChecked(true);
        menu.findItem(getMenuForFilterMode(filterMode)).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        GroupingMode selectedGrouping = getGroupingMode(item.getItemId());
        if (selectedGrouping != null) {
            groupingMode = selectedGrouping;
            item.setChecked(true);
            timelineAdapter.setGroupingMode(groupingMode);
            return true;
        }

        FilterMode selectedFilter = getFilterMode(item.getItemId());
        if (selectedFilter != null) {
            filterMode = selectedFilter;
            item.setChecked(true);
            loadAlbum();
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(KEY_ALBUM, contentAlbum);
        outState.putSerializable(KEY_GROUPING_MODE, groupingMode);
        outState.putSerializable(KEY_FILTER_MODE, filterMode);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    private GroupingMode getGroupingMode(@IdRes int menuId) {
        switch (menuId) {
            case R.id.timeline_grouping_day: return GroupingMode.DAY;
            case R.id.timeline_grouping_week: return GroupingMode.WEEK;
            case R.id.timeline_grouping_month: return GroupingMode.MONTH;
            case R.id.timeline_grouping_year: return GroupingMode.YEAR;
            default: return null;
        }
    }

    @IdRes
    private int getMenuForGroupingMode(@NonNull GroupingMode groupingMode) {
        switch (groupingMode) {
            case DAY: return R.id.timeline_grouping_day;
            case WEEK: return R.id.timeline_grouping_week;
            case MONTH: return R.id.timeline_grouping_month;
            case YEAR: return R.id.timeline_grouping_year;
            default: return R.id.timeline_grouping_day;
        }
    }

    @Nullable
    private FilterMode getFilterMode(@IdRes int menuId) {
        switch (menuId) {
            case R.id.all_media_filter: return FilterMode.ALL;
            case R.id.video_media_filter: return FilterMode.VIDEO;
            case R.id.image_media_filter: return FilterMode.IMAGES;
            case R.id.gifs_media_filter: return FilterMode.GIF;
            default: return null;
        }
    }

    @IdRes
    private int getMenuForFilterMode(@NonNull FilterMode filterMode) {
        switch (filterMode) {
            case ALL: return R.id.all_media_filter;
            case IMAGES: return R.id.image_media_filter;
            case GIF: return R.id.gifs_media_filter;
            case VIDEO: return R.id.video_media_filter;

            case NO_VIDEO:
            default: return R.id.all_media_filter;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int gridSize = getTimelineGridSize();
        timelineAdapter.setTimelineGridSize(gridSize);
        gridLayoutManager.setSpanCount(gridSize);
    }

    private void setupRecyclerView() {
        TimelineAdapter.TimelineItemDecorator decorator = new TimelineAdapter.TimelineItemDecorator(getContext(), R.dimen.timeline_decorator_spacing);
        gridLayoutManager = new GridLayoutManager(getContext(), getTimelineGridSize());
        timelineItems.setLayoutManager(gridLayoutManager);
        timelineItems.addItemDecoration(decorator);

        timelineAdapter = new TimelineAdapter(getContext(), getTimelineGridSize());
        timelineAdapter.setGridLayoutManager(gridLayoutManager);
        timelineAdapter.setGroupingMode(groupingMode);
        timelineAdapter.getClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pos -> {
                    if (timelineListener != null) {
                        timelineListener.onMediaClick(contentAlbum, timelineAdapter.getMedia(), pos);
                    }
                });

        timelineItems.setAdapter(timelineAdapter);
    }

    private void loadAlbum() {
        ArrayList<Media> mediaList = new ArrayList<>();
        CPHelper.getMedia(getContext(), contentAlbum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(media -> MediaFilter.getFilter(filterMode).accept(media))
                .subscribe(mediaList::add,
                        throwable -> refreshLayout.setRefreshing(false),
                        () -> {
                            contentAlbum.setCount(mediaList.size());
                            refreshLayout.setRefreshing(false);
                            setAdapterMedia(mediaList);
                        });
    }

    private void setAdapterMedia(@NonNull ArrayList<Media> mediaList) {
        Collections.sort(mediaList, MediaComparators.getComparator(SortingMode.DATE, SortingOrder.DESCENDING));
        timelineAdapter.setMedia(mediaList);
    }

    private int getTimelineGridSize() {
        return DeviceUtils.isPortrait(getResources())
                ? Defaults.TIMELINE_ITEMS_PORTRAIT
                : Defaults.TIMELINE_ITEMS_LANDSCAPE;
    }

    @Override
    public boolean editMode() {
        return timelineAdapter.selecting();
    }

    @Override
    public boolean clearSelected() {
        return timelineAdapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        timelineItems.setBackgroundColor(t.getBackgroundColor());
        timelineAdapter.refreshTheme(t);
        refreshLayout.setColorSchemeColors(t.getAccentColor());
        refreshLayout.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }

    @Override
    public void onItemSelected(int position) {
        if (timelineListener != null) {
            timelineListener.onMediaClick(contentAlbum, timelineAdapter.getMedia(), position);
        }
    }

    @Override
    public void onSelectMode(boolean selectMode) {
        // TODO: Implement via this interface
    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {
        // TODO: Implement via this interface
    }

    @Override
    public void updateMedia(ArrayList<Media> media) {
        if (timelineAdapter!=null) {
            timelineAdapter.setMedia(media);
        }
    }
}
