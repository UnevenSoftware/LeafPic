package org.horaapps.leafpic.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
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

import org.horaapps.leafpic.App;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.MainActivity;
import org.horaapps.leafpic.adapters.AlbumsAdapter;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.new_way.CPHelper;
import org.horaapps.leafpic.new_way.DataManager;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.ThemeHelper;
import org.horaapps.leafpic.util.Themeable;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 3/13/17.
 */

public class AlbumsFragment extends Fragment implements IFragment, Themeable {

    private static final String TAG = "asd";

    RecyclerView rvAlbums;
    private AlbumsAdapter albumsAdapter;
    private GridSpacingItemDecoration rvAlbumsDecoration;
    MainActivity act;


    SortingOrder sortingOrder;
    SortingMode sortingMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sortingMode = SortingMode.NUMERIC;
        sortingOrder = SortingOrder.ASCENDING;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = ((MainActivity) context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void displayAlbums(boolean hidden) {
        albumsAdapter.clear();
        SQLiteDatabase db = HandlingAlbums.getInstance(getContext()).getReadableDatabase();
        DataManager.getInstance()
                .getAlbumsRelay(hidden)
                /*.map(album -> album.withMediaObservable(CPHelper.getLastMedia(App.getInstance(), album.getId())))*/
                .subscribeOn(Schedulers.io())
                .map(album -> album.withSettings(HandlingAlbums.getSettings(db, album.getPath())))

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        album -> {
                            if (album.hasCover())
                                albumsAdapter.addAlbum(album);
                            else
                                CPHelper.getLastMedia(App.getInstance(), album.getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                media -> album.setCover(media.getPath()),
                                                throwable -> {},
                                                () -> albumsAdapter.addAlbum(album));

                        }, throwable -> { },
                        () -> {
                            db.close();
                            act.swipeRefreshLayout.setRefreshing(false);
                        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rvAlbums.getLayoutManager()).getSpanCount()) {
            rvAlbums.removeItemDecoration(rvAlbumsDecoration);
            rvAlbumsDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rvAlbums.addItemDecoration(rvAlbumsDecoration);
            rvAlbums.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
        }
    }

    public int columnsCount() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? PreferenceUtil.getInt(getContext(), "n_columns_folders", 2)
                : PreferenceUtil.getInt(getContext(), "n_columns_folders_landscape", 3);
    }

    private void updateToolbar() {

        if (editMode()) act.updateToolbar(
                String.format("%d/%d", albumsAdapter.getSelectedCount(), albumsAdapter.getItemCount()),
                GoogleMaterial.Icon.gmd_check, v -> albumsAdapter.clearSelectedAlbums());
        else
            act.updateToolbar(
                    getString(R.string.app_name),
                    GoogleMaterial.Icon.gmd_menu, v -> act.mDrawerLayout.openDrawer(GravityCompat.START));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rvAlbums = new RecyclerView(getContext());

        int spanCount = columnsCount();
        rvAlbumsDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rvAlbums.addItemDecoration(rvAlbumsDecoration);
        rvAlbums.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        rvAlbums.setHasFixedSize(true);
        rvAlbums.setItemAnimator(new DefaultItemAnimator());

        albumsAdapter = new AlbumsAdapter(getContext());

        albumsAdapter.getAlbumsClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(album -> Toast.makeText(getContext(), album.toString(), Toast.LENGTH_SHORT).show());

        albumsAdapter.getAlbumsSelectedClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((album) -> updateToolbar());

        //displayAlbums(false);

        rvAlbums.setAdapter(albumsAdapter);
        return rvAlbums;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.select_all).setTitle(
                getString(getSelectedCount() == getCount()
                        ? R.string.clear_selected
                        : R.string.select_all));

        menu.findItem(R.id.ascending_sort_action).setChecked(sortingOrder == SortingOrder.ASCENDING);

        switch (sortingMode) {
            case NAME:  menu.findItem(R.id.name_sort_action).setChecked(true); break;
            case SIZE:  menu.findItem(R.id.size_sort_action).setChecked(true); break;
            case DATE: default:
                menu.findItem(R.id.date_taken_sort_action).setChecked(true); break;
            case NUMERIC:  menu.findItem(R.id.numeric_sort_action).setChecked(true); break;
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean editMode = editMode();
        boolean oneSelected = getSelectedCount() == 1;

        menu.setGroupVisible(R.id.album_options_menu, true);
        menu.setGroupVisible(R.id.photos_option_men, false);

        menu.findItem(R.id.type_sort_action).setVisible(false);
        menu.findItem(R.id.filter_menu).setVisible(false);
        menu.findItem(R.id.search_action).setVisible(true);

        menu.findItem(R.id.renameAlbum).setVisible(oneSelected);
        menu.findItem(R.id.set_pin_album).setVisible(oneSelected);
        menu.findItem(R.id.clear_album_preview).setVisible(oneSelected);
        menu.findItem(R.id.clear_album_preview).setVisible(oneSelected);

        if (oneSelected)
            menu.findItem(R.id.set_pin_album).setTitle(albumsAdapter.getFirstSelectedAlbum().isPinned() ? getString(R.string.un_pin) : getString(R.string.pin));

        menu.findItem(R.id.delete_action).setVisible(editMode);
        menu.findItem(R.id.shortcut).setVisible(editMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.select_all:
                if (albumsAdapter.getSelectedCount() == albumsAdapter.getItemCount())
                    albumsAdapter.clearSelectedAlbums();
                else albumsAdapter.selectAllAlbums();
                return true;

            case R.id.set_pin_album:
                Album selectedAlbum = albumsAdapter.getFirstSelectedAlbum();
                if (selectedAlbum != null) {
                    selectedAlbum.togglePinAlbum(getContext());
                    albumsAdapter.clearSelectedAlbums();
                    albumsAdapter.sort();
                }
                // TODO: 3/24/17 notify
                return true;

            case R.id.shortcut:
                HandlingAlbums.installShortcutForSelectedAlbums(getContext(), albumsAdapter.getSelectedAlbums());
                albumsAdapter.clearSelectedAlbums();
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
        albumsAdapter.clearSelectedAlbums();
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        rvAlbums.setBackgroundColor(themeHelper.getBackgroundColor());
        albumsAdapter.updateTheme(themeHelper);
    }
}
