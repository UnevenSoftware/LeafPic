package com.leafpic.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leafpic.app.Adapters.SelectAlbumAdapter;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Base.HandlingPhotos;

/**
 * Created by dnld on 2/8/16.
 */
public class SelectAlbumActivity extends AppCompatActivity {

    public static final int COPY_TO_ACTION = 23;
    public static final int MOVE_TO_ACTION = 69;

    HandlingAlbums albums = new HandlingAlbums(SelectAlbumActivity.this);
    RecyclerView mRecyclerView;
    SelectAlbumAdapter adapt;
    String photoPaths;
    String photosIndexes;
    boolean hidden=true;
    int code;
    HandlingPhotos p;//= new HandlingPhotos(SelectAlbumActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_album_activity);

        photoPaths = getIntent().getStringExtra("selected_photos");
        code = getIntent().getIntExtra("request_code", -1);
        photosIndexes = getIntent().getStringExtra("photos_indexes");

        p = new HandlingPhotos(SelectAlbumActivity.this);
        if (code == 69) setTitle("Move to");
        else if (code == 23) setTitle("Copy to");

        setResult(Activity.RESULT_CANCELED);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
        getWindow().setStatusBarColor(getColor(R.color.toolbar));

        /*FAB*/
        SharedPreferences SP;
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final FloatingActionButton fabhidden = (FloatingActionButton) findViewById(R.id.fab_hidden);
        fabhidden.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(SP.getString("PrefColor", "#03A9F4"))));
        fabhidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAlbumPreview();
                //fabhidden.setImageIcon();
            }
        });
        loadAlbumPreview();

        //Base Theme
        LinearLayout ll = (LinearLayout) findViewById(R.id.select_album_layout);
        if (SP.getBoolean("set_dark_theme", false)){
            //setTheme(R.style.AppTheme_Dark);
            ll.setBackgroundColor(getColor(R.color.background_material_dark));
        }else {
            //setTheme(R.style.AppTheme);
            ll.setBackgroundColor(getColor(R.color.background_material_light));
        }
    }

    private void loadAlbumPreview(){
        if (hidden == false)
            albums.loadPreviewHiddenAlbums();
        else albums.loadPreviewAlbums();
        hidden=!hidden;

        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        adapt = new SelectAlbumAdapter(albums.dispAlbums, R.layout.select_album_card);
        adapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                String newAlbumPath = a.getTag().toString();
                if (code == MOVE_TO_ACTION) {
                    Intent result = new Intent();
                    result.putExtra("photos_indexes", photosIndexes);
                    p.moveSelectedPhotos(photoPaths, newAlbumPath);
                    setResult(Activity.RESULT_OK, result);
                }
                if (code == COPY_TO_ACTION) {
                    Intent result = new Intent();
                    p.copySelectedPhotos(photoPaths, newAlbumPath);
                    setResult(Activity.RESULT_OK, result);
                }
                finish();
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapt);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapt.notifyDataSetChanged();
    }
}

