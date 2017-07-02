package org.horaapps.leafpic.delete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.adapters.BaseAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import horaapps.org.liz.ThemeHelper;
import horaapps.org.liz.ThemedViewHolder;
import horaapps.org.liz.ui.ThemedTextView;

/**
 * Created by dnld on 6/29/17.
 */

public class FolderAdapter extends BaseAdapter<Folder, FolderAdapter.ViewHoldera> {

    public FolderAdapter(Context context, ArrayList<Folder> list) {
        super(context, list);
    }

    @Override
    public ViewHoldera onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_folder_dialog_item, parent, false);
        return new ViewHoldera(v);
    }

    @Override
    public void onBindViewHolder(ViewHoldera holder, int position) {
        holder.refreshTheme(getThemeHelper());

        Folder f = getElement(position);
        holder.folderName.setText(f.getName());

        if (f.getProgress() == -1 && -1 == f.getCount())
            holder.count.setText(null);
        else
            holder.count.setText(String.format("%d/%d", f.getProgress(), f.getCount()));
    }

    static class ViewHoldera extends ThemedViewHolder {
        @BindView(R.id.folder_name)
        ThemedTextView folderName;
        @BindView(R.id.file_count)
        ThemedTextView count;
        @BindView(R.id.text_dialog_rl)
        RelativeLayout llItemBackground;

        ViewHoldera(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            folderName.refreshTheme(themeHelper);
            folderName.refreshTheme(themeHelper);
        }
    }
}
