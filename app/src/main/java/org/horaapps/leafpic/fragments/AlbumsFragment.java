package org.horaapps.leafpic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.MainActivity;
import org.horaapps.leafpic.adapters.AlbumsAdapter;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dnld on 3/13/17.
 */

public class AlbumsFragment extends Fragment {

    private AlbumsAdapter albumsAdapter;
    private GridSpacingItemDecoration rvAlbumsDecoration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        albumsAdapter = new AlbumsAdapter(new ArrayList<>(), getContext());
    }


    MainActivity act;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = ((MainActivity) context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        RecyclerView rvAlbums = new RecyclerView(getContext());
        rvAlbums.setHasFixedSize(true);
        rvAlbums.setItemAnimator(new DefaultItemAnimator());

        albumsAdapter.getAlbumsClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(album -> Toast.makeText(getContext(), album.toString(), Toast.LENGTH_SHORT).show());

        albumsAdapter.getAlbumsSelectedClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((album) -> {
                    act.updateSelectedStuff();
                    //invalidateOptionsMenu();
                    //Toast.makeText(this, album.toString(), Toast.LENGTH_SHORT).show();
                });

        //return super.onCreateView(inflater, container, savedInstanceState);

        return rvAlbums;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.select_all:
                if (albumsAdapter.getSelectedCount() == albumsAdapter.getItemCount()) {
                    albumsAdapter.clearSelectedAlbums();
                } else albumsAdapter.selectAllAlbums();
                return true;


        }

        return super.onOptionsItemSelected(item);
    }
}
