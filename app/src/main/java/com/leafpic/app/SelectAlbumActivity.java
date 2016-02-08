package com.leafpic.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.leafpic.app.Adapters.SelectAlbumAdapter;
import com.leafpic.app.Base.Album;
import com.leafpic.app.Base.HandlingAlbums;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_album_activity);

        photoPaths = getIntent().getStringExtra("selected_photos");
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


        albums.loadPreviewAlbums();

        mRecyclerView = (RecyclerView) findViewById(R.id.gridAlbums);

        adapt = new SelectAlbumAdapter(albums.dispAlbums, R.layout.select_album_card);

        adapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                String s = a.getTag().toString();
                Album album = albums.getAlbum(s);
                Intent result = new Intent();
                result.putExtra("album_path", album.Path);
                result.putExtra("selected_photos", photoPaths);
                setResult(Activity.RESULT_OK, result);
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

