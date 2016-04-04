package com.leafpic.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.leafpic.app.Adapters.AlbumsAdapter;
import com.leafpic.app.Base.Album;
import com.leafpic.app.Base.CustomAlbumsHandler;
import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.ColorPalette;
import com.leafpic.app.utils.StringUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

/**
 * Created by Jibo on 04/04/2016.
 */
public class ExcludedAlbumsActivity extends ThemedActivity {

    HandlingAlbums albums = new HandlingAlbums(ExcludedAlbumsActivity.this);
    CustomAlbumsHandler h = new CustomAlbumsHandler(ExcludedAlbumsActivity.this);
    RecyclerView mRecyclerView;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded);

        albums.loadExcludedAlbums();
        checkNothing(albums.dispAlbums);
        initUI();
    }

    public void checkNothing(ArrayList<Album> asd){
        TextView a = (TextView) findViewById(R.id.nothing_to_show);
        a.setVisibility(asd.size() == 0 ? View.VISIBLE : View.GONE);
    }

    public void initUI(){
        /** TOOLBAR **/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        /** RECYCLE VIEW**/
        mRecyclerView = (RecyclerView) findViewById(R.id.excluded_albums);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(new ExcludedAlbumsAdapter(albums.dispAlbums, ExcludedAlbumsActivity.this));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
<<<<<<< HEAD

=======
>>>>>>> refs/remotes/DNLDsht/master

        /**SET UP UI COLORS**/
        toolbar.setBackgroundColor(getPrimaryColor());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setStatusBarColor();
        setNavBarColor();
        mRecyclerView.setBackgroundColor(getBackgroundColor());
        setRecentApp(getString(R.string.excluded_albums));
    }

    private class ExcludedAlbumsAdapter extends RecyclerView.Adapter<ExcludedAlbumsAdapter.ViewHolder> {

        ArrayList<Album> albums;
        SharedPreferences SP;

        public int getIndex(String id) {
            for (int i = 0; i < albums.size(); i++)
                    if (albums.get(i).ID.equals(id)) return i;
            return -1;
        }

        public ExcludedAlbumsAdapter(ArrayList<Album> ph, Context ctx){
            albums = ph;
            SP = PreferenceManager.getDefaultSharedPreferences(ctx);
        }

        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ID = v.getTag().toString();
                int pos;
                if((pos = getIndex(ID)) !=-1) {
                    h.clearAlbumExclude(ID);
                    albums.remove(pos);
                    notifyItemRemoved(pos);

                    MaterialRippleLayout.on(v)
                            .rippleOverlay(true)
                            .rippleAlpha(0.2f)
                            .rippleColor(0xFF585858)
                            .rippleHover(true)
                            .rippleDuration(1)
                            .create();
                    checkNothing(albums);
                }
            }
        };
        public ExcludedAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.excluded_card, parent, false);
            v.findViewById(R.id.UnExclude_icon).setOnClickListener(listener);
            return new ViewHolder(
                    MaterialRippleLayout.on(v)
                            .rippleOverlay(true)
                            .rippleAlpha(0.2f)
                            .rippleColor(0xFF585858)
                            .rippleHover(true)
                            .rippleDuration(1)
                            .create()
            );
        }

        @Override
        public void onBindViewHolder(final ExcludedAlbumsAdapter.ViewHolder holder, final int position) {
            final Album a = albums.get(position);
            Context c = holder.album_path.getContext();//picture
            holder.album_path.setText(a.Path);
            holder.album_name.setText(StringUtils.getBucketNamebyBucketPath(a.Path));

            /**SET LAYOUT THEME**/
            int color = SP.getBoolean("set_dark_theme", true)
                    ? ColorPalette.getLightBackgroundColor(c)
                    : ColorPalette.getDarkBackgroundColor(c);
            holder.imgFolder.setColor(color);
            holder.imgUnExclude.setColor(color);
            holder.album_name.setTextColor(color);

            color = ContextCompat.getColor(c, SP.getBoolean("set_dark_theme", true)
                    ? R.color.md_grey_400
                    : R.color.md_grey_600);
            holder.album_path.setTextColor(color);

            color = ContextCompat.getColor(c, SP.getBoolean("set_dark_theme", true)
                    ? R.color.md_dark_cards
                    : R.color.md_light_cards);
            holder.card_layout.setBackgroundColor(color);

            holder.imgUnExclude.setTag(a.ID);
        }

        public int getItemCount() {
            return albums.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout card_layout;
            IconicsImageView imgUnExclude;
            IconicsImageView imgFolder;
            TextView album_name;
            TextView album_path;

            public ViewHolder(View itemView) {
                super(itemView);
                card_layout = (LinearLayout) itemView.findViewById(R.id.linear_card_excluded);
                imgUnExclude = (IconicsImageView) itemView.findViewById(R.id.UnExclude_icon);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon);
                album_name = (TextView) itemView.findViewById(R.id.Excluded_Title_Item);
                album_path = (TextView) itemView.findViewById(R.id.Excluded_Path_Item);
            }
        }
    }
}
