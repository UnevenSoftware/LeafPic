package com.leafpic.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leafpic.app.Adapters.PickAlbumAdapter;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Base.HandlingPhotos;
import com.leafpic.app.Views.GridSpacingItemDecoration;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.Measure;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by dnld on 2/8/16.
 */
public class PickAlbumActivity extends ThemedActivity{

    public static final int COPY_TO_ACTION = 23;
    public static final int MOVE_TO_ACTION = 911;

    HandlingAlbums albums = new HandlingAlbums(PickAlbumActivity.this);
    RecyclerView mRecyclerView;
    PickAlbumAdapter adapt;
    String photoPaths;
    String photosIndexes;
    boolean hidden=false;
    int code;
    HandlingPhotos p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_album_activity);

        photoPaths = getIntent().getStringExtra("selected_photos");
        code = getIntent().getIntExtra("request_code", -1);
        photosIndexes = getIntent().getStringExtra("photos_indexes");

        p = new HandlingPhotos(PickAlbumActivity.this);
        if (code == MOVE_TO_ACTION) setTitle(getString(R.string.move_to));
        else if (code == COPY_TO_ACTION) setTitle(getString(R.string.copy_to));

        setResult(Activity.RESULT_CANCELED);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getPrimaryColor());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_arrow_back)
                        .color(Color.WHITE)
                        .sizeDp(19));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final FloatingActionButton fabhidden = (FloatingActionButton) findViewById(R.id.fab_hidden);
        fabhidden.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        fabhidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAlbumPreview();
            }
        });
        loadAlbumPreview();

        LinearLayout ll = (LinearLayout) findViewById(R.id.select_album_layout);
        ll.setBackgroundColor(getBackgroundColor());

        setNavBarColor();
        setStatusBarColor();
        setRecentApp(getString(R.string.app_name));
    }

    private void loadAlbumPreview(){
        if (hidden) albums.loadPreviewHiddenAlbums();
        else albums.loadPreviewAlbums();
        hidden=!hidden;
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        adapt = new PickAlbumAdapter(albums.dispAlbums,getApplicationContext());
        adapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                String newAlbumPath = a.getTag().toString();
                Intent result;
                switch (code){
                    case MOVE_TO_ACTION:
                        result = new Intent();
                        result.putExtra("photos_indexes", photosIndexes);
                        p.moveSelectedPhotos(photoPaths, newAlbumPath);
                        setResult(Activity.RESULT_OK, result);
                        break;
                    case COPY_TO_ACTION:
                        result = new Intent();
                        p.copySelectedPhotos(photoPaths, newAlbumPath);
                        setResult(Activity.RESULT_OK, result);
                        break;
                    default: break;
                }
                finish();
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapt);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(1, Measure.pxToDp(5, getApplicationContext()), true));

        adapt.notifyDataSetChanged();
    }
}

