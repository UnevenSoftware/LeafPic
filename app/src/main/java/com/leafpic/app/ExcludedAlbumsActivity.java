package com.leafpic.app;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.leafpic.app.Adapters.AlbumsAdapter;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Views.GridSpacingItemDecoration;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.Measure;

/**
 * Created by Jibo on 04/04/2016.
 */
public class ExcludedAlbumsActivity extends ThemedActivity {


    HandlingAlbums albums = new HandlingAlbums(ExcludedAlbumsActivity.this);
    RecyclerView mRecyclerView;
    AlbumsAdapter adapt;

    //UI
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded);
 


    }

    public void initUI(){
        /** TOOLBAR **/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        /** RECYCLE VIEW**/
        mRecyclerView = (RecyclerView) findViewById(R.id.excluded_albums);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(1, Measure.pxToDp(3, getApplicationContext()), true));


        /**SET UP UI COLORS**/
        toolbar.setBackgroundColor(getPrimaryColor());
        setStatusBarColor();
        setNavBarColor();
        mRecyclerView.setBackgroundColor(getBackgroundColor());
    }
}
