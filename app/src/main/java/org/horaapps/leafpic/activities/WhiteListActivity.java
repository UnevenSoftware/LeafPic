package org.horaapps.leafpic.activities;

import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.model.base.ImageFileFilter;
import org.horaapps.leafpic.util.StringUtils;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dnld on 01/04/16.
 */
public class WhiteListActivity extends SharedMediaActivity {

    private RecyclerView mRecyclerView;
    private Toolbar toolbar;

    private ArrayList<Item> folders = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(org.horaapps.leafpic.R.id.excluded_albums);
        initUi();
        folders = getAlbums().getItems();
        lookForFoldersInMediaStore();
    }


    private void lookForFoldersInMediaStore() {
        String[] projection = new String[]{
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA
        };

        String selection, selectionArgs[];

        selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=? or " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " ) GROUP BY ( " + MediaStore.Files.FileColumns.PARENT + " ";

        selectionArgs = new String[]{
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
        };
        Cursor cur = getContentResolver().query(
                MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null);

        if (cur != null) {
            int idColumn = cur.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
            int nameColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int mediaColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            while (cur.moveToNext()) {
                if (shouldAdd(cur.getLong(idColumn))) {
                    folders.add(new Item(cur.getLong(idColumn),
                            StringUtils.getBucketPathByImagePath(cur.getString(mediaColumn)),
                            cur.getString(nameColumn)));
                }

            }
            cur.close();
        }


    }

    private boolean shouldAdd(long id) {
        for (Item item : folders)
            if (item.equals(id))
                return false;
        return true;
    }

    @TestOnly
    private void fetchFolders(File dir) {
//        if (!alreadyTracked.contains(dir)) {
//            if (isFolderWithMedia(dir))
//                folders.add(new Item(dir.getPath(), dir.getName()));
//            File[] foo = dir.listFiles(new NotHiddenFoldersFilter());
//            if (foo != null)
//                for (File f : foo)
//                    if (!alreadyTracked.contains(f)) fetchFolders(f);
//        }
    }

    @TestOnly
    private boolean isFolderWithMedia(File dir) {
        String[] list = dir.list(new ImageFileFilter(true));
        return list != null && list.length > 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_track_albums, menu);
        //menu.findItem(R.id.action_done).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_done));
        menu.findItem(R.id.action_add).setIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_add_circle));
        return true;
    }


    private void addFolder(File dir) {
        String[] list = dir.list(new ImageFileFilter(true));
        if (list != null && list.length > 0) {
            MediaScannerConnection.scanFile(getApplicationContext(), list, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.wtf("asd", s+" - "+uri.toString());
                }
            });
        } else {
            Toast.makeText(this, "No media in that folder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.chose_folders))
                        .exploreMode(true, true)
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                addFolder(new File(path));
                            }
                        }).show();
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
        mRecyclerView.setAdapter(new ItemsAdapter());
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void updateUiElements(){
        toolbar.setBackgroundColor(getPrimaryColor());
        mRecyclerView.setBackgroundColor(getBackgroundColor());
        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.chose_folders));
        findViewById(org.horaapps.leafpic.R.id.rl_ea).setBackgroundColor(getBackgroundColor());
    }

    public static class Item {
        String path;
        String name;
        long id;
        boolean included = false;

        public String getPath() {
            return path;
        }

        public long getId() {
            return id;
        }

        public boolean isIncluded() {
            return included;
        }

        Item(long id, String path, String name) {
            this.path = path;
            this.name = name;
            this.id = id;
        }

        public Item(long id, String path, boolean included) {
            this.path = path;
            this.name = StringUtils.getName(path);
            this.id = id;
            this.included = included;
        }

        boolean toggleInclude() {
            included = !included;
            return included;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Long)
                return this.id == (long) obj;
            return super.equals(obj);
        }
    }

    private class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = folders.get((int) v.findViewById(R.id.folder_path).getTag());
                SwitchCompat s = (SwitchCompat) v.findViewById(R.id.tracked_status);
                s.setChecked(item.toggleInclude());
                setSwitchColor(getAccentColor(), s);
                getAlbums().handleTrackItem(item);
            }
        };

        public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_track_folder, parent, false);
            v.setOnClickListener(listener);
            return new ItemsAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ItemsAdapter.ViewHolder holder, final int position) {

            Item itm = folders.get(position);
            holder.path.setText(itm.path);
            holder.name.setText(itm.name);
            holder.path.setTag(position);
            holder.tracked.setChecked(itm.included);

            /**SET LAYOUT THEME**/
            holder.name.setTextColor(getTextColor());
            holder.path.setTextColor(getSubTextColor());
            holder.imgFolder.setColor(getIconColor());
            setSwitchColor(getAccentColor(), holder.tracked);
            holder.layout.setBackgroundColor(getCardBackgroundColor());
        }

        public int getItemCount() {
            return folders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;
            SwitchCompat tracked;
            IconicsImageView imgFolder;
            TextView name;
            TextView path;

            ViewHolder(View itemView) {
                super(itemView);
                layout = (LinearLayout) itemView.findViewById(R.id.linear_card_excluded);
                tracked = (SwitchCompat) itemView.findViewById(R.id.tracked_status);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon);
                name = (TextView) itemView.findViewById(R.id.folder_name);
                path = (TextView) itemView.findViewById(R.id.folder_path);
            }
        }
    }
}
