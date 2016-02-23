package com.leafpic.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
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
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Created by dnld on 2/8/16.
 */
public class SelectAlbumActivity extends ThemedActivity{

    public static final int COPY_TO_ACTION = 23;
    public static final int MOVE_TO_ACTION = 911;

    HandlingAlbums albums = new HandlingAlbums(SelectAlbumActivity.this);
    RecyclerView mRecyclerView;
    SelectAlbumAdapter adapt;
    String photoPaths;
    String photosIndexes;
    boolean hidden=false;
    int code;
    HandlingPhotos p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_album_activity);

        photoPaths = getIntent().getStringExtra("selected_photos");
        code = getIntent().getIntExtra("request_code", -1);
        photosIndexes = getIntent().getStringExtra("photos_indexes");

        p = new HandlingPhotos(SelectAlbumActivity.this);
        if (code == MOVE_TO_ACTION) setTitle("Move to");
        else if (code == COPY_TO_ACTION) setTitle("Copy to");

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
        getWindow().setStatusBarColor(getPrimaryColor());

        /*FAB*/
        final FloatingActionButton fabhidden = (FloatingActionButton) findViewById(R.id.fab_hidden);


        fabhidden.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        fabhidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAlbumPreview();
            }
        });
        loadAlbumPreview();

        //Base Theme
        LinearLayout ll = (LinearLayout) findViewById(R.id.select_album_layout);
        if (isDarkTheme())
            ll.setBackgroundColor(getColor(R.color.act_bg_dark));
        else ll.setBackgroundColor(getColor(R.color.act_bg_light));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), drawable.getBitmap(), getPrimaryColor()));

            if (isNavigationBarColored())
                getWindow().setNavigationBarColor(getPrimaryColor());
            else getWindow().setNavigationBarColor(getColor(R.color.md_black_1000));
        }

    }

    private void loadAlbumPreview(){
        if (hidden) albums.loadPreviewHiddenAlbums();
        else albums.loadPreviewAlbums();

        hidden=!hidden;

        mRecyclerView = (RecyclerView) findViewById(R.id.grid_albums);
        adapt = new SelectAlbumAdapter(albums.dispAlbums, R.layout.select_album_card);
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
        adapt.notifyDataSetChanged();
    }
}

