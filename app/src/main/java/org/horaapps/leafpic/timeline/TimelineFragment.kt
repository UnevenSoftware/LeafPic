package org.horaapps.leafpic.timeline

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.GridLayoutManager
import android.view.*
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_timeline.*
import org.horaapps.leafpic.R
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.Media
import org.horaapps.leafpic.data.filter.FilterMode
import org.horaapps.leafpic.data.filter.MediaFilter
import org.horaapps.leafpic.data.provider.CPHelper
import org.horaapps.leafpic.data.sort.MediaComparators
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import org.horaapps.leafpic.fragments.BaseMediaGridFragment
import org.horaapps.leafpic.interfaces.MediaClickListener
import org.horaapps.leafpic.items.ActionsListener
import org.horaapps.leafpic.progress.ProgressBottomSheet
import org.horaapps.leafpic.util.DeviceUtils
import org.horaapps.leafpic.util.Security
import org.horaapps.leafpic.util.deleteMedia
import org.horaapps.leafpic.util.preferences.Defaults
import org.horaapps.leafpic.util.shareMedia
import org.horaapps.liz.ThemeHelper
import org.horaapps.liz.ThemedActivity
import java.util.*

/**
 * Fragment which shows the Timeline.
 */
class TimelineFragment : BaseMediaGridFragment(), ActionsListener {

    companion object {

        const val TAG = "TimelineFragment"

        private const val ARGS_ALBUM = "args_album"

        private const val KEY_ALBUM = "key_album"
        private const val KEY_GROUPING_MODE = "key_grouping_mode"
        private const val KEY_FILTER_MODE = "key_filter_mode"

        fun newInstance(album: Album) = TimelineFragment().apply {
            arguments = Bundle().apply { putParcelable(ARGS_ALBUM, album) }
        }
    }

    private lateinit var timelineAdapter: TimelineAdapter
    private lateinit var timelineListener: MediaClickListener
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var contentAlbum: Album

    private lateinit var groupingMode: GroupingMode
    private lateinit var filterMode: FilterMode

    private val timelineGridSize: Int
        get() = if (DeviceUtils.isPortrait(resources)) Defaults.TIMELINE_ITEMS_PORTRAIT
        else Defaults.TIMELINE_ITEMS_LANDSCAPE

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is MediaClickListener) timelineListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        savedInstanceState?.let {
            contentAlbum = it.getParcelable(KEY_ALBUM)
            groupingMode = it.get(KEY_GROUPING_MODE) as GroupingMode
            filterMode = it.get(KEY_FILTER_MODE) as FilterMode
            return
        }

        /* Get content from arguments */
        val arguments = arguments ?: return
        contentAlbum = arguments.getParcelable(ARGS_ALBUM)

        /* Set defaults */
        groupingMode = GroupingMode.DAY
        filterMode = FilterMode.ALL
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_timeline, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeline_swipe_refresh_layout.setOnRefreshListener { this.loadAlbum() }
        setupRecyclerView()
        loadAlbum()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_timeline, menu)

        menu.findItem(getMenuForGroupingMode(groupingMode)).isChecked = true
        menu.findItem(getMenuForFilterMode(filterMode)).isChecked = true
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        if (menu == null) return

        val isEditing = editMode()
        with(menu) {
            setGroupVisible(R.id.timeline_view_items, !isEditing)
            setGroupVisible(R.id.timeline_edit_items, isEditing)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        getGroupingMode(item.itemId)?.let {
            // Handling Grouping Mode selections
            groupingMode = it
            item.isChecked = true
            timelineAdapter.setGroupingMode(it)
            return true
        }

        getFilterMode(item.itemId)?.let {
            // Handling Filter Mode selections
            filterMode = it
            item.isChecked = true
            loadAlbum()
            return true
        }

        return when (item.itemId) {

            R.id.timeline_menu_delete -> {
                if (Security.isPasswordOnDelete()) {
                    Security.authenticateUser(activity as ThemedActivity?, object : Security.AuthCallBack {
                        override fun onAuthenticated() {
                            deleteMedia()
                        }

                        override fun onError() {
                            Toast.makeText(context, R.string.wrong_password, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else deleteMedia()

                true
            }

            R.id.timeline_share -> {
                shareMedia(context!!, timelineAdapter.selectedMedia)
                true
            }

            R.id.timeline_menu_select_all -> {
                if (timelineAdapter.selectedCount == timelineAdapter.mediaCount) exitContextMenu()
                else timelineAdapter.selectAll()
                true
            }

            else -> false

        }
    }

    private fun deleteMedia() {
        deleteMedia(context!!, timelineAdapter.selectedMedia, childFragmentManager, object : ProgressBottomSheet.Listener<Media> {

            override fun onCompleted() {
                exitContextMenu()
                loadAlbum()
            }

            override fun onProgress(item: Media?) {
               timelineAdapter.removeItem(item)
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putParcelable(KEY_ALBUM, contentAlbum)
            putSerializable(KEY_GROUPING_MODE, groupingMode)
            putSerializable(KEY_FILTER_MODE, filterMode)
        }
        super.onSaveInstanceState(outState)
    }

    private fun getGroupingMode(@IdRes menuId: Int) = when (menuId) {
        R.id.timeline_grouping_day -> GroupingMode.DAY
        R.id.timeline_grouping_week -> GroupingMode.WEEK
        R.id.timeline_grouping_month -> GroupingMode.MONTH
        R.id.timeline_grouping_year -> GroupingMode.YEAR
        else -> null
    }

    @IdRes
    private fun getMenuForGroupingMode(groupingMode: GroupingMode) = when (groupingMode) {
        GroupingMode.DAY -> R.id.timeline_grouping_day
        GroupingMode.WEEK -> R.id.timeline_grouping_week
        GroupingMode.MONTH -> R.id.timeline_grouping_month
        GroupingMode.YEAR -> R.id.timeline_grouping_year
    }

    private fun getFilterMode(@IdRes menuId: Int) = when (menuId) {
        R.id.all_media_filter -> FilterMode.ALL
        R.id.video_media_filter -> FilterMode.VIDEO
        R.id.image_media_filter -> FilterMode.IMAGES
        R.id.gifs_media_filter -> FilterMode.GIF
        else -> null
    }

    @IdRes
    private fun getMenuForFilterMode(filterMode: FilterMode) = when (filterMode) {
        FilterMode.ALL -> R.id.all_media_filter
        FilterMode.IMAGES -> R.id.image_media_filter
        FilterMode.GIF -> R.id.gifs_media_filter
        FilterMode.VIDEO -> R.id.video_media_filter
        FilterMode.NO_VIDEO -> R.id.all_media_filter
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val gridSize = timelineGridSize
        with(gridSize) {
            timelineAdapter.setTimelineGridSize(this)
            gridLayoutManager.spanCount = this
        }
    }

    private fun setupRecyclerView() {
        val decorator = TimelineAdapter.TimelineItemDecorator(context!!, R.dimen.timeline_decorator_spacing)
        gridLayoutManager = GridLayoutManager(context, timelineGridSize)
        timeline_items.layoutManager = gridLayoutManager
        timeline_items.addItemDecoration(decorator)

        timelineAdapter = TimelineAdapter(context!!, this, timelineGridSize)
        timelineAdapter.setGridLayoutManager(gridLayoutManager)
        timelineAdapter.setGroupingMode(groupingMode)
        timeline_items.adapter = timelineAdapter
    }

    private fun loadAlbum() {
        val mediaList = ArrayList<Media>()
        CPHelper.getMedia(context, contentAlbum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { media -> MediaFilter.getFilter(filterMode).accept(media) }
                .subscribe(
                        { mediaList.add(it) },
                        { _ -> timeline_swipe_refresh_layout!!.isRefreshing = false },
                        {
                            contentAlbum.count = mediaList.size
                            timeline_swipe_refresh_layout!!.isRefreshing = false
                            setAdapterMedia(mediaList)
                        })
    }

    private fun setAdapterMedia(mediaList: ArrayList<Media>) {
        Collections.sort(mediaList, MediaComparators.getComparator(SortingMode.DATE, SortingOrder.DESCENDING))
        timelineAdapter.media = mediaList
    }

    override fun editMode() = timelineAdapter.isSelecting

    override fun clearSelected() = timelineAdapter.clearSelected()

    override fun refreshTheme(t: ThemeHelper) {
        with(t) {
            timeline_items.setBackgroundColor(this.backgroundColor)
            timeline_swipe_refresh_layout.setColorSchemeColors(this.accentColor)
            timeline_swipe_refresh_layout.setProgressBackgroundColorSchemeColor(this.backgroundColor)
            timelineAdapter.refreshTheme(this)
        }
    }

    override fun onItemSelected(position: Int) = timelineListener.onMediaClick(contentAlbum, timelineAdapter.media, position)

    override fun onSelectMode(selectMode: Boolean) = updateToolbar()

    override fun onSelectionCountChanged(selectionCount: Int, totalCount: Int) = editModeListener.onItemsSelected(selectionCount, totalCount)

    override fun getSelectedCount() = timelineAdapter.selectedCount

    override fun getTotalCount() = timelineAdapter.mediaCount

    override fun getToolbarButtonListener(editMode: Boolean) = when (editMode) {
        true -> View.OnClickListener { exitContextMenu() }
        false -> null
    }

    override fun getToolbarTitle() = when (editMode()) {
        true -> null
        false -> getString(R.string.timeline_toolbar_title)
    }
}
