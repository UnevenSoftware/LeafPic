package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.sort.MediaComparators;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.items.ActionsListener;
import org.horaapps.leafpic.views.SquareRelativeLayout;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedAdapter;
import org.horaapps.liz.ThemedViewHolder;
import org.horaapps.liz.ui.ThemedIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter used to display Media Items.
 *
 * TODO: This class needs a major cleanup. Remove code from onBindViewHolder!
 */
public class MediaAdapter extends ThemedAdapter<MediaAdapter.ViewHolder> {

    private final ArrayList<Media> media;
    private int selectedCount = 0;

    private SortingOrder sortingOrder;
    private SortingMode sortingMode;

    private Drawable placeholder;
    private final ActionsListener actionsListener;

    private boolean isSelecting = false;

    public MediaAdapter(Context context, SortingMode sortingMode, SortingOrder sortingOrder, ActionsListener actionsListener) {
        super(context);
        media = new ArrayList<>();
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        placeholder = getThemeHelper().getPlaceHolder();
        setHasStableIds(true);
        this.actionsListener = actionsListener;
    }

    private void sort() {
        Collections.sort(media, MediaComparators.getComparator(sortingMode, sortingOrder));
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return media.get(position).getUri().hashCode() ^ 1312;
    }

    public void changeSortingOrder(SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder;
        Collections.reverse(media);
        notifyDataSetChanged();
    }

    public void changeSortingMode(SortingMode sortingMode) {
        this.sortingMode = sortingMode;
        sort();
    }

    public ArrayList<Media> getSelected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new ArrayList<>(media.stream().filter(Media::isSelected).collect(Collectors.toList()));
        } else {
            ArrayList<Media> arrayList = new ArrayList<>(selectedCount);
            for (Media m : media)
                if (m.isSelected())
                    arrayList.add(m);
            return arrayList;
        }
    }

    public Media getFirstSelected() {
        if (selectedCount > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return media.stream().filter(Media::isSelected).findFirst().orElse(null);
            else
                for (Media m : media)
                    if (m.isSelected())
                        return m;
        }
        return null;
    }

    public ArrayList<Media> getMedia() {
        return media;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void selectAll() {
        for (int i = 0; i < media.size(); i++)
            if (media.get(i).setSelected(true))
                notifyItemChanged(i);
        selectedCount = media.size();
        startSelection();
    }

    public boolean clearSelected() {
        boolean changed = true;
        for (int i = 0; i < media.size(); i++) {
            boolean b = media.get(i).setSelected(false);
            if (b)
                notifyItemChanged(i);
            changed &= b;
        }

        selectedCount = 0;
        stopSelection();
        return changed;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false));
    }

    private void notifySelected(boolean increase) {
        selectedCount += increase ? 1 : -1;
        actionsListener.onSelectionCountChanged(selectedCount, getItemCount());

        if (selectedCount == 0 && isSelecting) stopSelection();
        else if (selectedCount > 0 && !isSelecting) startSelection();
    }

    private void startSelection() {
        isSelecting = true;
        actionsListener.onSelectMode(true);
    }

    private void stopSelection() {
        isSelecting = false;
        actionsListener.onSelectMode(false);
    }

    public boolean selecting() {
        return isSelecting;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Media f = media.get(position);

        //holder.path.setTag(f);
        holder.icon.setVisibility(View.GONE);
        holder.layout.setBackgroundColor(getThemeHelper().getPrimaryColor());

        holder.gifIcon.setVisibility(f.isGif() ? View.VISIBLE : View.GONE);

        RequestOptions options = new RequestOptions()
                .signature(f.getSignature())
                .format(DecodeFormat.PREFER_RGB_565)
                .centerCrop()
                .placeholder(placeholder)
                //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);


        Glide.with(holder.imageView.getContext())
                .load(f.getUri())
                .apply(options)
                .thumbnail(0.5f)
                .into(holder.imageView);

        if (f.isVideo()) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(f.getName());
            /*holder.path.setTextColor(ContextCompat.getColor(holder.path.getContext(), R.color.md_dark_primary_text));
            holder.path.setBackgroundColor(
                    ColorPalette.getTransparentColor(
                            ContextCompat.getColor(holder.path.getContext(), R.color.md_black_1000), 100));*/
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
            holder.path.animate().alpha(1).setDuration(250);
        } else {
            holder.icon.setVisibility(View.GONE);
            holder.path.setVisibility(View.GONE);

            holder.icon.animate().alpha(0).setDuration(250);
            holder.path.animate().alpha(0).setDuration(250);
        }

        if (f.isSelected()) {
            holder.icon.setIcon(CommunityMaterial.Icon.cmd_check);
            holder.icon.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.layout.setPadding(15, 15, 15, 15);
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
        } else {
            holder.imageView.clearColorFilter();
            holder.layout.setPadding(0, 0, 0, 0);
        }

        holder.layout.setOnClickListener(v -> {
            if (selecting()) {
                notifySelected(f.toggleSelected());
                notifyItemChanged(holder.getAdapterPosition());
            } else
                actionsListener.onItemSelected(holder.getAdapterPosition());
        });

        holder.layout.setOnLongClickListener(v -> {
            if (!selecting()) {
                // If it is the first long press
                notifySelected(f.toggleSelected());
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                selectAllUpTo(f);
            }

            return true;
        });
    }

    public void remove(Media media) {
        int i = this.media.indexOf(media);
        this.media.remove(i);
        notifyItemRemoved(i);
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        placeholder = theme.getPlaceHolder();
        //super.refreshTheme(theme);
    }


    /**
     * On longpress, it finds the last or the first selected image before or after the targetIndex
     * and selects them all.
     *
     * @param
     */
    public void selectAllUpTo(Media m) {
        int targetIndex = media.indexOf(m);

        int indexRightBeforeOrAfter = -1;
        int indexNow;

        // TODO: 4/5/17 rewrite?
        for (Media sm : getSelected()) {
            indexNow = media.indexOf(sm);
            if (indexRightBeforeOrAfter == -1) indexRightBeforeOrAfter = indexNow;

            if (indexNow > targetIndex) break;
            indexRightBeforeOrAfter = indexNow;
        }

        if (indexRightBeforeOrAfter != -1) {
            for (int index = Math.min(targetIndex, indexRightBeforeOrAfter); index <= Math.max(targetIndex, indexRightBeforeOrAfter); index++) {
                if (media.get(index) != null) {
                    if (media.get(index).setSelected(true)) {
                        notifySelected(true);
                        notifyItemChanged(index);
                    }
                }
            }

        }
    }

    public void setupFor(Album album) {
        media.clear();
        changeSortingMode(album.settings.getSortingMode());
        changeSortingOrder(album.settings.getSortingOrder());
        notifyDataSetChanged();
    }

    public void clear() {
        media.clear();
        notifyDataSetChanged();
    }

    public int add(Media album) {
        int i = Collections.binarySearch(
                media, album, MediaComparators.getComparator(sortingMode, sortingOrder));
        if (i < 0) i = ~i;
        media.add(i, album);

        //notifyItemRangeInserted(0, media.size()-1);
        notifyItemInserted(i);
        //notifyDataSetChanged();
        return i;
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    static class ViewHolder extends ThemedViewHolder {
        @BindView(R.id.photo_preview)
        ImageView imageView;
        @BindView(R.id.photo_path)
        TextView path;
        @BindView(R.id.gif_icon)
        ThemedIcon gifIcon;
        @BindView(R.id.icon)
        ThemedIcon icon;
        @BindView(R.id.media_card_layout)
        SquareRelativeLayout layout;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            icon.setColor(Color.WHITE);
        }
    }
}
