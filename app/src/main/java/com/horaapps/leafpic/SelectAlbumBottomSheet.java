package com.horaapps.leafpic;

/**
 * Created by Jibo on 18/04/2016.
 */

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.HandlingAlbums;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectAlbumBottomSheet extends BottomSheetDialogFragment {

    RecyclerView mRecyclerView;
    TextView textViewTitle;
    TextView txtNewFolder;
    IconicsImageView imgHiddenDefault;
    ProgressBar progressBar;

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    boolean hidden = false;
    HandlingAlbums albums;

    IconicsImageView imgNewFolder;
    LinearLayout background;
    LinearLayout llNewFolder;
    ArrayList<Album> albumArrayList = null;
    SharedPreferences SP;
    View.OnClickListener onClickListener;
    View.OnClickListener onClickListenerNewFolder;

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public void setAlbumArrayList(ArrayList<Album> albumArrayList){ this.albumArrayList = albumArrayList; }

    String currentPath;
    BottomSheetAlbumsAdapter adapter;

    public void setTitle(String title) {
        this.title = title;
    }

    String title;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnClickListenerNewFolder(View.OnClickListener onClickListenerNF){
        onClickListenerNewFolder = onClickListenerNF;

    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        albums = new HandlingAlbums(getContext());
        View contentView = View.inflate(getContext(), R.layout.copy_move_bottom_sheet, null);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.rv_modal_dialog_albums);
        adapter = new BottomSheetAlbumsAdapter(onClickListener);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(dialog.getContext(), 1));

        /**SET UP DIALOG THEME**/
        SP = PreferenceManager.getDefaultSharedPreferences(dialog.getContext());

        /**** Scrollbar *****/
        Drawable drawableScrollBar = ContextCompat.getDrawable(dialog.getContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(SP.getInt("primary_color",
                ContextCompat.getColor(dialog.getContext(), R.color.md_indigo_500)), PorterDuff.Mode.SRC_ATOP));

        contentView.findViewById(R.id.ll_bottom_sheet_title).setBackgroundColor(SP.getInt("primary_color",
                ContextCompat.getColor(dialog.getContext(), R.color.md_indigo_500)));

        textViewTitle = (TextView) contentView.findViewById(R.id.bottom_sheet_title);
        progressBar = (ProgressBar) contentView.findViewById(R.id.spinner_loading);
        textViewTitle.setText(title);
        textViewTitle.setTextColor(ContextCompat.getColor(dialog.getContext(),R.color.md_white_1000));

        //TODO:WILL BE REPLACED WITH EXPANDABLE VIEW
        imgHiddenDefault = (IconicsImageView) contentView.findViewById(R.id.toggle_hidden_icon);
        imgHiddenDefault.setColor(ContextCompat.getColor(dialog.getContext(),R.color.md_white_1000));

        imgHiddenDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidden = !hidden;
                new ToggleAlbumsTask().execute(hidden);
            }
        });

        txtNewFolder = (TextView) contentView.findViewById(R.id.Create_New_Folder_Item);
        txtNewFolder.setTextColor(
                ContextCompat.getColor(getDialog().getContext(),  SP.getInt("basic_theme", 1)==1
                    ? R.color.md_grey_800
                    : R.color.md_grey_200));

        imgNewFolder = (IconicsImageView) contentView.findViewById(R.id.Create_New_Folder_Icon);
        imgNewFolder.setColor(
                ContextCompat.getColor(getDialog().getContext(), SP.getInt("basic_theme", 1)==1
                        ? R.color.md_light_primary_icon
                        : R.color.md_dark_primary_icon));

        llNewFolder = (LinearLayout) contentView.findViewById(R.id.ll_create_new_folder);
        llNewFolder.setOnClickListener(onClickListenerNewFolder);

        background = (LinearLayout) contentView.findViewById(R.id.ll_album_modal_dialog);
        background.setBackgroundColor(ContextCompat.getColor(dialog.getContext(),
                SP.getInt("basic_theme", 1)==1
                        ? R.color.md_light_cards
                        : (SP.getInt("basic_theme", 1)==2
                            ? R.color.md_dark_cards
                            : R.color.md_black_1000))
                );

        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        llNewFolder.setVisibility(View.GONE);
        if (albumArrayList == null) {
            albumArrayList = new ArrayList<Album>();
            new ToggleAlbumsTask().execute(hidden);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
            imgHiddenDefault.setIcon(hidden ? "gmd-folder" : "faw-low-vision");
            llNewFolder.setVisibility(View.VISIBLE);
        }
    }


    Button btnUP;
    TextView textFolder;
    ListView dialog_ListView;
    File root;
    File curFolder;
    private List<String> fileList = new ArrayList<String>();

    ArrayAdapter<String> directoryList;

    private void newFolderDialog() {
        //Toast.makeText(getContext(),"New Folder",Toast.LENGTH_SHORT).show();


        /*final File curFolder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());

        directoryList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout
                .simple_list_item_1, Arrays.asList(curFolder.list()));

        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this, getDialogStyle());

        IconicsImageView btnUP;
        final ListView dialog_ListView;

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_explorer, null);

        final TextView textViewCurrentPath = (TextView) dialogLayout.findViewById(R.id.current_path);
        btnUP = (IconicsImageView) dialogLayout.findViewById(R.id.directory_up);
        btnUP.setColor(getIconColor());
        ((IconicsImageView) dialogLayout.findViewById(R.id.folder_icon)).setColor(getIconColor());
        dialog_ListView = (ListView) dialogLayout.findViewById(R.id.folder_list);
        btnUP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                File current = new File(textViewCurrentPath.getText().toString());
                Log.wtf(TAG,current.getAbsolutePath());
                if(current.isDirectory()) {
                    directoryList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout
                            .simple_list_item_1, HandlingAlbums.getSubFolders(current.getParentFile()));
                    textViewCurrentPath.setText(current.getParentFile().getAbsolutePath());
                    dialog_ListView.setAdapter(directoryList);
                }
            }
        });
        deleteDialog.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String path = textViewCurrentPath.getText().toString();
                Toast.makeText(getContext(),"asd", Toast.LENGTH_SHORT).show();
            }
        });

        /*deleteDialog.setNeutralButton(R.string.new_folder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String path = textViewCurrentPath.getText().toString();
                Toast.makeText(MainActivity.this, "new folder", Toast.LENGTH_SHORT).show();
            }
        });*/


        /*dialog_ListView.setAdapter(directoryList);
        textViewCurrentPath.setText(curFolder.getAbsolutePath());
        deleteDialog.setView(dialogLayout);

        dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File selected = new File(textViewCurrentPath.getText()+"/"+ directoryList.getItem(position));

                Log.wtf("asd",selected.isDirectory()+ " - " + selected.getAbsolutePath());
                if(selected.isDirectory()){
                    directoryList = new ArrayAdapter<String>(getContext(), android.R.layout
                            .simple_list_item_1, HandlingAlbums.getSubFolders(selected));
                    textViewCurrentPath.setText(selected.getAbsolutePath());
                    dialog_ListView.setAdapter(directoryList);
                } else {
                    Toast.makeText(getContext(), selected.toString() + "selected ", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        deleteDialog.show();*/


    }



    class ToggleAlbumsTask extends AsyncTask<Boolean, Integer, Void> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Boolean... arg0) {
            albumArrayList = albums.getValidFolders(arg0[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
            imgHiddenDefault.setIcon(hidden ? "gmd-folder" : "faw-low-vision");
            llNewFolder.setVisibility(View.VISIBLE);
        }
    }

    class BottomSheetAlbumsAdapter extends RecyclerView.Adapter<BottomSheetAlbumsAdapter.ViewHolder> {

        private View.OnClickListener listener;
        public BottomSheetAlbumsAdapter( View.OnClickListener lis){
            listener=lis;
         }

        public BottomSheetAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.copy_move_bottom_sheet_item, parent, false);
            v.setOnClickListener(listener);
            v.findViewById(R.id.ll_album_bottom_sheet_item).setOnClickListener(listener);
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
        public void onBindViewHolder(final BottomSheetAlbumsAdapter.ViewHolder holder, final int position) {

            final Album a = albumArrayList.get(position);
            holder.album_name.setText(a.getName());
            holder.album_media_count.setText(String.format(Locale.getDefault(),"%d %s",a.getCount(),a.getContentDescription(getDialog().getContext())));
            holder.album_name.setTag(a.getPath());

            /**SET LAYOUT THEME**/
            SP = PreferenceManager.getDefaultSharedPreferences(getDialog().getContext());
            //getDialog().getContext()
            int textColor= ContextCompat.getColor(getDialog().getContext(),  SP.getInt("basic_theme", 1)==1
                    ? R.color.md_grey_800
                    : R.color.md_grey_200);

            int subtextColor= ContextCompat.getColor(getDialog().getContext(), SP.getInt("basic_theme", 1)==1
                    ? R.color.md_grey_600
                    : R.color.md_grey_400);

            holder.album_name.setTextColor(textColor);

            String hexAccentColor = String.format("#%06X", (0xFFFFFF & SP.getInt("accent_color",
                    ContextCompat.getColor(getDialog().getContext(), R.color.md_light_blue_500))));

            holder.album_media_count.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>"
                    + a.getCount() + "</font></b>" + "<font " + "color='" + subtextColor + "'> "
                    + a.getContentDescription(getDialog().getContext()) + "</font>"));

            holder.imgFolder.setColor(
                    ContextCompat.getColor(getDialog().getContext(), SP.getInt("basic_theme", 1)==1
                            ? R.color.md_light_primary_icon
                            : R.color.md_dark_primary_icon));
        }

        public int getItemCount() {
            return albumArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView album_name;
            TextView album_media_count;
            IconicsImageView imgFolder;
            public ViewHolder(View itemView) {
                super(itemView);
                album_name = (TextView) itemView.findViewById(R.id.title_bottom_sheet_item);
                album_media_count = (TextView) itemView.findViewById(R.id.count_bottom_sheet_item);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon_bottom_sheet_item);
            }
        }
    }
}

