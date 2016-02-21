package com.leafpic.app;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

/**
 * Created by Jibo on 21/02/2016.
 */
public class ThemeActivity extends AppCompatActivity{
    RecyclerView mRecyclerView;
    Toolbar toolbar;
    SharedPreferences SP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_layout);

        initUiTweaks();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initUiTweaks(){
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        /**** Nav Bar ****/
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean NavBar = SP.getBoolean("nav_bar", false);
            if (NavBar)
                getWindow().setNavigationBarColor(getColor(R.color.primary));
            else getWindow().setNavigationBarColor(getColor(R.color.md_black_1000));

        }
        /**** ToolBar *****/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**** Status Bar */
        getWindow().setStatusBarColor(getColor(R.color.primary));
        //getWindow().setStatusBarColor(getColor(R.color.toolbar));

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl_theme_layout);
        if (SP.getBoolean("set_dark_theme", false)){
            //setTheme(R.style.AppTheme_Dark);
            rl.setBackgroundColor(getColor(R.color.background_material_dark));
        }else {
            //setTheme(R.style.AppTheme);
            rl.setBackgroundColor(getColor(R.color.background_material_light));
        }
    }

    private void LoadThems(){
        /*
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_theme);
        adapt = new AlbumsAdapter(albums.dispAlbums, R.layout.theme_card);
        adapt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                adapt.notifyItemChanged(albums.toggleSelectAlbum(a.getTag().toString()));
                editmode = true;
                invalidateOptionsMenu();
                return true;
            }
        });
        adapt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView a = (TextView) v.findViewById(R.id.album_name);
                if (editmode) {
                    adapt.notifyItemChanged(albums.toggleSelectAlbum(a.getTag().toString()));
                    invalidateOptionsMenu();
                } else {
                    Album album = albums.getAlbum(a.getTag().toString());
                    Intent intent = new Intent(AlbumsActivity.this, PhotosActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("album", album);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapt);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapt.notifyDataSetChanged();
        */
    }
}
