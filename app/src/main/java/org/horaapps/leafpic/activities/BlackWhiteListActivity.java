package org.horaapps.leafpic.activities;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.data.HandlingAlbums;
import org.horaapps.leafpic.data.filter.ImageFileFilter;
import org.horaapps.leafpic.data.provider.ContentProviderHelper;
import org.horaapps.leafpic.util.AnimationUtils;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ui.ThemedIcon;

import java.io.File;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by dnld on 01/04/16.
 */
public class BlackWhiteListActivity extends SharedMediaActivity {

    public static final String EXTRA_TYPE = "typeExcluded";

    private RecyclerView mRecyclerView;
    private ItemsAdapter adapter;
    private Toolbar toolbar;
    private ArrayList<String> folders = new ArrayList<>();
    private boolean typeExcluded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_white_list);
        toolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.excluded_albums);
        initUi();
        loadFolders(getIntent().getIntExtra(EXTRA_TYPE, HandlingAlbums.EXCLUDED));
    }

    private void loadFolders(int type) {
        this.typeExcluded = type == HandlingAlbums.EXCLUDED;
        folders = HandlingAlbums.getInstance(getApplicationContext()).getFolders(type);
        checkNothing();
        if (isExcludedMode()) setTitle(getString(R.string.excluded_items));
        else setTitle(getString(R.string.white_list));
        adapter.notifyDataSetChanged();
        supportInvalidateOptionsMenu();
    }

    private boolean isExcludedMode() {
        return typeExcluded;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_black_white_list, menu);
        menu.findItem(R.id.action_add).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_add_circle));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isExcludedMode()) {
            menu.findItem(R.id.action_add).setVisible(false);
            menu.findItem(R.id.action_toggle).setTitle(R.string.white_list);
        } else {
            menu.findItem(R.id.action_add).setVisible(true);
            menu.findItem(R.id.action_toggle).setTitle(R.string.excluded_items);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void addFolder(final File dir) {
        String[] list = dir.list(new ImageFileFilter(true));
        final boolean[] found = { false };
        if (list != null && list.length > 0) {
            MediaScannerConnection.scanFile(getApplicationContext(), list, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    // TODO: 12/15/16 test this!
                    if(!found[0]) {
                        long albumId = ContentProviderHelper.getAlbumId(getApplicationContext(), s);
                        if (albumId != -1) {
                            found[0] = true;
                            Toast.makeText(BlackWhiteListActivity.this, "got the ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            HandlingAlbums.getInstance(getApplicationContext()).addFolderToWhiteList(dir.getPath());
            folders.add(0, dir.getPath());
            adapter.notifyItemInserted(0);
            checkNothing();
        } else {
            Toast.makeText(this, R.string.no_media_in_this_folder, Toast.LENGTH_SHORT).show();
            // TODO: 12/26/16 should i add or not?
        }
    }

    private void checkNothing() {
        findViewById(R.id.white_list_decription_card).setVisibility(
                showDescriptionCard() ? View.VISIBLE : View.GONE);

        findViewById(R.id.nothing_to_show_placeholder).setVisibility(
                showNothingToShowPlaceholder() ? View.VISIBLE : View.GONE);

        findViewById(R.id.ll_emoji_easter_egg).setVisibility(
                showEasterEgg() ? View.VISIBLE : View.GONE);
    }

    private boolean showDescriptionCard() {
        return !isExcludedMode() && Prefs.getToggleValue(getString(R.string.preference_show_tips), true);
    }

    private boolean showNothingToShowPlaceholder() {
        return folders.size() < 1 && isExcludedMode() && !Prefs.showEasterEgg();
    }

    private boolean showEasterEgg() {
        return folders.size() < 1 && isExcludedMode() && Prefs.showEasterEgg();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.chose_folders))
                        .exploreMode(true)
                        .force(true)
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                addFolder(new File(path));
                            }
                        }).show();
                return true;
            case R.id.action_toggle:
                loadFolders(isExcludedMode() ? HandlingAlbums.INCLUDED : HandlingAlbums.EXCLUDED);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initUi() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter((adapter = new ItemsAdapter()));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mRecyclerView.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));
    }

    @CallSuper
    @Override
    public void updateUiElements(){
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        mRecyclerView.setBackgroundColor(getBackgroundColor());
        setStatusBarColor();
        setNavBarColor();
        toolbar.setTitle(getTitle());
        setRecentApp(getTitle().toString());
        ((CardView) findViewById(R.id.white_list_decription_card)).setCardBackgroundColor(getCardBackgroundColor());
        ((TextView) findViewById(R.id.white_list_decription_txt)).setTextColor(getTextColor());
        //TODO: EMOJI EASTER EGG - THERE'S NOTHING TO SHOW
        ((TextView) findViewById(R.id.emoji_easter_egg)).setTextColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setTextColor(getSubTextColor());

        findViewById(org.horaapps.leafpic.R.id.rl_ea).setBackgroundColor(getBackgroundColor());
    }

    private class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

        private View.OnClickListener listener = v -> {
            String path = (String) v.getTag();
            int i = folders.indexOf(path);
            HandlingAlbums.getInstance(getApplicationContext()).clearStatusFolder(path);
            folders.remove(i);
            notifyItemRemoved(i);
            checkNothing();
        };

        public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_track_folder, parent, false);
            v.findViewById(R.id.remove_icon).setOnClickListener(listener);
            return new ItemsAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ItemsAdapter.ViewHolder holder, final int position) {
            String itm = folders.get(position);
            holder.path.setText(itm);
            holder.name.setText(StringUtils.getName(itm));
            holder.imgRemove.setTag(itm);

            holder.name.setTextColor(getTextColor());
            holder.path.setTextColor(getSubTextColor());
            holder.imgFolder.setColor(getIconColor());
            holder.imgRemove.setColor(getIconColor());
            holder.layout.setBackgroundColor(getCardBackgroundColor());
        }

        public int getItemCount() {
            return folders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;
            ThemedIcon imgFolder, imgRemove;
            TextView name, path;

            ViewHolder(View itemView) {
                super(itemView);
                layout = itemView.findViewById(R.id.linear_card_excluded);
                imgFolder = itemView.findViewById(R.id.folder_icon);
                imgRemove = itemView.findViewById(R.id.remove_icon);
                name = itemView.findViewById(R.id.folder_name);
                path = itemView.findViewById(R.id.folder_path);
            }
        }
    }
}
