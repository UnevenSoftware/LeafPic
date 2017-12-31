package org.horaapps.leafpic.timeline;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.filter.MediaFilter;
import org.horaapps.leafpic.data.provider.CPHelper;
import org.horaapps.leafpic.fragments.BaseFragment;
import org.horaapps.liz.ThemeHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Fragment which shows the Timeline.
 */
public class TimelineFragment extends BaseFragment {

    public static final String TAG = "TimelineFragment";

    private static final String ARGS_ALBUM = "args_album";

    @BindView(R.id.timeline_items) RecyclerView timelineItems;
    @BindView(R.id.timeline_swipe_refresh_layout) SwipeRefreshLayout refreshLayout;

    private TimelineAdapter timelineAdapter;
    private TimelineListener timelineListener;

    private Album contentAlbum;

    public static TimelineFragment newInstance(@NonNull Album album) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            contentAlbum = savedInstanceState.getParcelable(ARGS_ALBUM);
            return;
        }

        // Get content from arguments
        Bundle arguments = getArguments();
        if (arguments == null) return;
        contentAlbum = arguments.getParcelable(ARGS_ALBUM);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ARGS_ALBUM, contentAlbum);
        super.onSaveInstanceState(outState);
    }

    public void setTimelineListener(@NonNull TimelineListener timelineListener) {
        this.timelineListener = timelineListener;
    }

    private void setupRecyclerView() {
        timelineItems.setItemAnimator(new LandingAnimator(new OvershootInterpolator(1f)));
        timelineItems.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));

        timelineAdapter = new TimelineAdapter(getContext());
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
        timelineAdapter.clearAll();
        CPHelper.getMedia(getContext(), contentAlbum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(media -> MediaFilter.getFilter(contentAlbum.filterMode()).accept(media))
                .subscribe(media -> timelineAdapter.add(media),
                        throwable -> refreshLayout.setRefreshing(false),
                        () -> {
                            contentAlbum.setCount(getCount());
                            if (getNothingToShowListener() != null)
                                getNothingToShowListener().changedNothingToShow(getCount() == 0);
                            refreshLayout.setRefreshing(false);
                        });
    }

    public int getCount() {
        return timelineAdapter.getItemCount();
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

    /**
     * Interface to report events to parent container
     */
    public interface TimelineListener {

        void onMediaClick(Album album, ArrayList<Media> media, int position);
    }
}
