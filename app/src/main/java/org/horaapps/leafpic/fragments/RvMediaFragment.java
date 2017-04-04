package org.horaapps.leafpic.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.MainActivity;
import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.model.HandlingAlbums;
import org.horaapps.leafpic.model.Media;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.new_way.AlbumsHelper;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.ThemeHelper;
import org.horaapps.leafpic.util.Themeable;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 3/13/17.
 */

public class RvMediaFragment extends Fragment implements IFragment, Themeable {

    private static final String TAG = "asd";

    @BindView(R.id.media) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private MediaAdapter albumsAdapter;
    private GridSpacingItemDecoration rvAlbumsDecoration;

    private MainActivity act;
    private boolean hidden;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = ((MainActivity) context);
    }

    @Override
    public void onResume() {
        super.onResume();
        clearSelected();
    }

    public void displayAlbums(boolean hidden) {
        this.hidden = hidden;
        displayAlbums();
    }

    public void displayAlbums() {
        albumsAdapter.clear();
        /*SQLiteDatabase db = HandlingAlbums.getInstance(getContext()).getReadableDatabase();
        DataManager.getInstance()
                .getAlbumsRelay(getContext(), hidden)
                .subscribeOn(Schedulers.io())
                .map(album -> album.withSettings(HandlingAlbums.getSettings(db, album.getPath())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        album -> {

                            Log.wtf("asd", album.toString());
                            if (album.hasCover())
                                albumsAdapter.add(album);
                            else
                                CPHelper.getLastMedia(App.getInstance(), album.getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                media -> album.setCover(media.getPath()),
                                                throwable -> {},
                                                () -> albumsAdapter.add(album));

                        }, throwable -> { },
                        () -> {
                            db.close();
                            act.nothingToShow(getCount() == 0);
                            refresh.setRefreshing(false);
                        });*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            rv.removeItemDecoration(rvAlbumsDecoration);
            rvAlbumsDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.addItemDecoration(rvAlbumsDecoration);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
        }
    }

    public int columnsCount() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? PreferenceUtil.getInt(getContext(), "n_columns_folders", 2)
                : PreferenceUtil.getInt(getContext(), "n_columns_folders_landscape", 3);
    }

    private void updateToolbar() {
        if (editMode())
            //todo improve
            act.updateToolbar(
                    String.format(Locale.ENGLISH, "%d/%d",
                            albumsAdapter.getSelectedCount(), albumsAdapter.getItemCount()),
                    GoogleMaterial.Icon.gmd_check,
                    v -> albumsAdapter.clearSelected());
        else act.resetToolbar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rv_media, null);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        rvAlbumsDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.addItemDecoration(rvAlbumsDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        rv.setHasFixedSize(true);
        rv.setItemAnimator(new DefaultItemAnimator());

        albumsAdapter = new MediaAdapter(
                getContext(), sortingMode(), sortingOrder());

        albumsAdapter.getClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(album -> Toast.makeText(getContext(), album.toString(), Toast.LENGTH_SHORT).show());

        albumsAdapter.getSelectedClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(album -> {
                    updateToolbar();
                    getActivity().invalidateOptionsMenu();
                });

        refresh.setOnRefreshListener(this::displayAlbums);
        rv.setAdapter(albumsAdapter);

        displayAlbums(false);
        return v;
    }

    public SortingMode sortingMode() {
        return albumsAdapter != null
                ? albumsAdapter.sortingMode()
                : AlbumsHelper.getSortingMode(getContext());
    }

    public SortingOrder sortingOrder() {
        return albumsAdapter != null
                ? albumsAdapter.sortingOrder()
                : AlbumsHelper.getSortingOrder(getContext());
    }

    private HandlingAlbums db() {
        return HandlingAlbums.getInstance(getContext());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_albums_fragment, menu);

        menu.findItem(R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(R.id.search_action).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_search)));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean editMode = editMode();
        boolean oneSelected = getSelectedCount() == 1;

        menu.setGroupVisible(R.id.general_album_items, !editMode);
        menu.setGroupVisible(R.id.edit_mode_items, editMode);
        menu.setGroupVisible(R.id.one_selected_items, oneSelected);

        menu.findItem(R.id.select_all).setTitle(
                getSelectedCount() == getCount()
                        ? R.string.clear_selected
                        : R.string.clear_selected);

        if (!editMode) {
            menu.findItem(R.id.ascending_sort_order).setChecked(sortingOrder() == SortingOrder.ASCENDING);
            switch (sortingMode()) {
                case NAME:  menu.findItem(R.id.name_sort_mode).setChecked(true); break;
                case SIZE:  menu.findItem(R.id.size_sort_mode).setChecked(true); break;
                case DATE: default:
                    menu.findItem(R.id.date_taken_sort_mode).setChecked(true); break;
                case NUMERIC:  menu.findItem(R.id.numeric_sort_mode).setChecked(true); break;
            }
        }

        if (oneSelected) {
            Media selectedAlbum = albumsAdapter.getFirstSelected();
            //menu.findItem(R.id.pin_album).setTitle(selectedAlbum.isPinned() ? getString(R.string.un_pin) : getString(R.string.pin));
            //menu.findItem(R.id.clear_album_cover).setVisible(selectedAlbum.hasCover());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.select_all:
                if (albumsAdapter.getSelectedCount() == albumsAdapter.getItemCount())
                    albumsAdapter.clearSelected();
                else albumsAdapter.selectAll();
                return true;


            case R.id.name_sort_mode:
                albumsAdapter.changeSortingMode(SortingMode.NAME);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.NAME);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                albumsAdapter.changeSortingMode(SortingMode.DATE);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.DATE);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                albumsAdapter.changeSortingMode(SortingMode.SIZE);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.SIZE);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                albumsAdapter.changeSortingMode(SortingMode.NUMERIC);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.NUMERIC);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                SortingOrder sortingOrder = SortingOrder.fromValue(item.isChecked());
                albumsAdapter.changeSortingOrder(sortingOrder);
                AlbumsHelper.setSortingOrder(getContext(), sortingOrder);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public int getCount() {
        return albumsAdapter.getItemCount();
    }

    public int getSelectedCount() {
        return albumsAdapter.getSelectedCount();
    }

    @Override
    public boolean editMode() {
        return albumsAdapter.selecting();
    }

    @Override
    public void clearSelected() {
        albumsAdapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        albumsAdapter.updateTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }
}
