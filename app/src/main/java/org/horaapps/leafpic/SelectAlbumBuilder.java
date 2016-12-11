package org.horaapps.leafpic;

/**
 * Created by Jibo on 18/04/2016.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.base.FoldersFileFilter;
import org.horaapps.leafpic.model.base.ImageFileFilter;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.ContentHelper;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.ThemeHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SelectAlbumBuilder extends BottomSheetDialogFragment {

    private String title;
    private ArrayList<File> folders;
    private BottomSheetAlbumsAdapter adapter;
    private ThemeHelper theme;
    private boolean exploreMode = false, canGoBack = false, forzed = false;
    private IconicsImageView imgExploreMode;
    private LinearLayout exploreModePanel;
    private TextView currentFolderPath;
    private OnFolderSelected onFolderSelected;
    FragmentManager fragmentManager;
    private FloatingActionButton fabDone;

    final int INTERNAL_STORAGE = 0;

    private String sdCardPath = null;

    public static SelectAlbumBuilder with(FragmentManager manager) {
        SelectAlbumBuilder fragment = new SelectAlbumBuilder();
        fragment.fragmentManager = manager;
        return fragment;
    }

    public SelectAlbumBuilder title(String title) {
        this.title = title;
        return this;
    }

    public SelectAlbumBuilder exploreMode(boolean enabled) {
        exploreMode = enabled;
        return this;
    }

    public SelectAlbumBuilder exploreMode(boolean enabled, boolean force) {
        exploreMode = enabled;
        forzed = force;
        return this;
    }

    public SelectAlbumBuilder onFolderSelected(OnFolderSelected callback) {
        onFolderSelected = callback;
        return this;
    }

    public void show() {
        show(fragmentManager, getTag());
    }


    private boolean canGoBack() {
        return canGoBack;
    }

    public interface OnFolderSelected {
        void folderSelected(String path);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String path = view.findViewById(R.id.name_folder).getTag().toString();
            if (exploreMode) displayContentFolder(new File(path));
            else {
                dismiss();
                onFolderSelected.folderSelected(path);
            }
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.select_folder_bottom_sheet, null);
        final RecyclerView mRecyclerView = (RecyclerView) contentView.findViewById(R.id.folders);
        final Spinner spinner = (Spinner) contentView.findViewById(R.id.storage_spinner);
        currentFolderPath = (TextView) contentView.findViewById(R.id.bottom_sheet_sub_title);
        exploreModePanel = (LinearLayout) contentView.findViewById(R.id.ll_explore_mode_panel);
        imgExploreMode = (IconicsImageView) contentView.findViewById(R.id.toggle_hidden_icon);

        theme = ThemeHelper.getThemeHelper(getContext());
        sdCardPath = ContentHelper.getSdcardPath(getContext());

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new BottomSheetAlbumsAdapter();
        mRecyclerView.setAdapter(adapter);

        spinner.setAdapter(new VolumeSpinnerAdapter(contentView.getContext()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                switch(pos){
                    case INTERNAL_STORAGE:
                        displayContentFolder(Environment.getExternalStorageDirectory());
                        break;
                    default:
                        // TODO: 12/11/16 do
//                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                            DocumentFile documentFile = ContentHelper.getDocumentFile(getContext(), new File(ContentHelper.getExtSdCardPaths(getContext())[pos - 1]), true, false);
//                            if(documentFile != null){
//                                displayContentFolder(new File(ContentHelper.getExtSdCardPaths(getContext())[pos - 1]));
//                            } else {
//                                Toast.makeText(getContext(), getString(R.string.no_permission), Toast.LENGTH_LONG).choseProvider();
//                                spinner.setSelection(0);
//                            }
//                        } else {
//                            displayContentFolder(new File(ContentHelper.getExtSdCardPaths(getContext())[pos - 1]));
//                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        /**SET UP THEME**/
        contentView.findViewById(R.id.rl_bottom_sheet_title).setBackgroundColor(theme.getPrimaryColor());
        exploreModePanel.setBackgroundColor(theme.getPrimaryColor());
        contentView.findViewById(R.id.ll_select_folder).setBackgroundColor(theme.getCardBackgroundColor());
        theme.setColorScrollBarDrawable(ContextCompat.getDrawable(dialog.getContext(), R.drawable.ic_scrollbar));

        fabDone = (FloatingActionButton) contentView.findViewById(R.id.fab_bottomsheet_done);
        fabDone.setBackgroundTintList(ColorStateList.valueOf(theme.getAccentColor()));
        fabDone.setImageDrawable(new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_done).color(Color.WHITE));
        fabDone.setVisibility(exploreMode ? View.VISIBLE : View.GONE);
        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onFolderSelected.folderSelected(currentFolderPath.getText().toString());
            }
        });

        ((TextView) contentView.findViewById(R.id.bottom_sheet_title)).setText(title);

        contentView.findViewById(R.id.rl_create_new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), theme.getDialogStyle());
                AlertDialogsHelper.getInsertTextDialog(((ThemedActivity) getActivity()), builder,
                        editText, R.string.new_folder);
                builder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File folderPath = new File(currentFolderPath.getText().toString() + File.separator + editText.getText().toString());
                        if (folderPath.mkdir()) displayContentFolder(folderPath);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
                builder.show();
            }
        });
        contentView.findViewById(R.id.rl_bottom_sheet_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!forzed) {
                    toggleExplorerMode(!exploreMode);
                    fabDone.setVisibility(exploreMode ? View.VISIBLE : View.GONE);
                }
            }
        });

        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        adapter.notifyDataSetChanged();
        toggleExplorerMode(exploreMode);
    }

    private void displayContentFolder(File dir) {
        canGoBack = false;
        if(dir.canRead()) {
            folders = new ArrayList<>();
            File parent = dir.getParentFile();
            if (parent.canRead()) {
                canGoBack = true;
                folders.add(0, parent);
            }
            File[] files = dir.listFiles(new FoldersFileFilter());
            if (files != null && files.length > 0) {
                folders.addAll(new ArrayList<>(Arrays.asList(files)));
                currentFolderPath.setText(dir.getAbsolutePath());
            }
            currentFolderPath.setText(dir.getAbsolutePath());
            adapter.notifyDataSetChanged();
        }
    }



    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) { dismiss(); }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
    };

    private void toggleExplorerMode(boolean enabled) {
        folders = new ArrayList<>();
        exploreMode = enabled;

        if(exploreMode) {
            displayContentFolder(Environment.getExternalStorageDirectory());
            imgExploreMode.setIcon(theme.getIcon(CommunityMaterial.Icon.cmd_folder));
            exploreModePanel.setVisibility(View.VISIBLE);
        } else {
            currentFolderPath.setText(R.string.local_folder);
            for (Album album : ((MyApplication) getActivity().getApplicationContext()).getAlbums().dispAlbums) {
                folders.add(new File(album.getPath()));
            }
            imgExploreMode.setIcon(theme.getIcon(CommunityMaterial.Icon.cmd_compass_outline));
            exploreModePanel.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private class VolumeSpinnerAdapter extends ArrayAdapter<String> {

        VolumeSpinnerAdapter(Context context) {
            super(context, R.layout.spinner_item_with_pic, R.id.volume_name);
            insert(getString(R.string.internal_storage), INTERNAL_STORAGE);
            if(sdCardPath != null)
                add(getString(R.string.extrnal_storage));
        }

        @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            GoogleMaterial.Icon icon;

            switch (position){
                case INTERNAL_STORAGE: icon = GoogleMaterial.Icon.gmd_storage; break;
                default: icon = GoogleMaterial.Icon.gmd_sd_card; break;
            }

            ((ImageView)view.findViewById(R.id.volume_image)).setImageDrawable(new IconicsDrawable(getContext()).icon(icon).sizeDp(24).color(Color.WHITE));
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            GoogleMaterial.Icon icon;

            switch (position){
                case INTERNAL_STORAGE: icon = GoogleMaterial.Icon.gmd_storage; break;
                default: icon = GoogleMaterial.Icon.gmd_sd_card; break;
            }
            ((IconicsImageView) view.findViewById(R.id.volume_image)).setIcon(icon);
            view.setBackgroundColor(theme.getPrimaryColor());
            return view;
        }
    }

    private class BottomSheetAlbumsAdapter extends RecyclerView.Adapter<BottomSheetAlbumsAdapter.ViewHolder> {

        BottomSheetAlbumsAdapter() { }

        public BottomSheetAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_folder_bottom_sheet_item, parent, false);
            v.setOnClickListener(onClickListener);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final BottomSheetAlbumsAdapter.ViewHolder holder, final int position) {

            File f = folders.get(position);
            String[] list = f.list(new ImageFileFilter(true));
            int countMedia = list != null ? list.length : 0;
            list = f.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return new File(file, s).isDirectory();
                }
            });
            int countFolder = list != null ? list.length : 0;

            holder.folderName.setText(f.getName());
            holder.folderName.setTag(f.getPath());
            holder.folderName.setTextColor(theme.getTextColor());
            String hexAccentColor =ColorPalette.getHexColor(theme.getAccentColor());

            String b = String.format(Locale.ENGLISH, "<b><font color='%s'>%d</font></b><font color='%d'> %s</font>", hexAccentColor, countMedia, theme.getSubTextColor(), getString(R.string.media)) +
                    " " +
                    String.format(Locale.ENGLISH, "<b><font color='%s'>%d</font></b><font color='%d'> %s</font>", hexAccentColor, countFolder, theme.getSubTextColor(), getString(countFolder != 1 ? R.string.folders : R.string.folder));
            holder.folderCount.setText(StringUtils.html(b));
            holder.imgFolder.setColor(theme.getIconColor());
            holder.imgFolder.setIcon(theme.getIcon(CommunityMaterial.Icon.cmd_folder));

            if(canGoBack() && position == 0) { // go to parent folder
                holder.folderName.setText("..");
                holder.folderCount.setText(StringUtils.html("<font color='" + theme.getSubTextColor() + "'>"+getString(R.string.go_to_parent)+"</font>"));
                holder.imgFolder.setIcon(theme.getIcon(CommunityMaterial.Icon.cmd_arrow_up));
            }
        }

        public int getItemCount() {
            return folders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView folderName;
            TextView folderCount;
            IconicsImageView imgFolder;
            ViewHolder(View itemView) {
                super(itemView);
                folderName = (TextView) itemView.findViewById(R.id.name_folder);
                folderCount = (TextView) itemView.findViewById(R.id.count_folder);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon_bottom_sheet_item);
            }
        }
    }
}

