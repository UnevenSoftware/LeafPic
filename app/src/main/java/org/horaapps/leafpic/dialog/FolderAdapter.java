package org.horaapps.leafpic.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.adapters.BaseAdapter;
import org.horaapps.leafpic.data.Album;

import butterknife.BindView;
import butterknife.ButterKnife;
import horaapps.org.liz.ThemeHelper;
import horaapps.org.liz.ThemedViewHolder;

/**
 * Created by dnld on 6/29/17.
 */

public class FolderAdapter extends BaseAdapter<Album, FolderAdapter.ViewHoldera> {

    public FolderAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHoldera onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_folder_bottom_sheet_item, parent, false);
        //v.setOnClickListener(onClickListener);
        return new ViewHoldera(v);
    }

    @Override
    public void onBindViewHolder(ViewHoldera holder, int position) {

    }

    static class ViewHoldera extends ThemedViewHolder {
        @BindView(R.id.name_folder)
        TextView folderName;
        @BindView(R.id.folder_icon_bottom_sheet_item)
        IconicsImageView imgFolder;
        @BindView(R.id.ll_album_bottom_sheet_item)
        LinearLayout llItemBackground;

        ViewHoldera(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            folderName.setTextColor(themeHelper.getTextColor());
        }
    }
}
