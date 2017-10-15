package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.koushikdutta.ion.Ion;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.sort.MediaComparators;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.views.SquareRelativeLayout;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedAdapter;
import org.horaapps.liz.ThemedViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by dnld on 1/7/16.
 */
public class MediaAdapter extends ThemedAdapter<MediaAdapter.ViewHolder> {

    private ArrayList<Media> media;

    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();
    private final PublishSubject<Media> onChangeSelectedSubject = PublishSubject.create();

    private int selectedCount = 0;

    private SortingOrder sortingOrder;
    private SortingMode sortingMode;

    private Drawable placeholder;

    public MediaAdapter(Context context, SortingMode sortingMode, SortingOrder sortingOrder) {
        super(context);
        media = new ArrayList<>();
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        placeholder = getThemeHelper().getPlaceHolder();
        setHasStableIds(true);
    }

    public void sort() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            media.sort(AlbumsComparators.getComparator(sortingMode));
            //media = media.stream().sorted(AlbumsComparators.getComparator(sortingMode)).collect(Collectors.toList());
        else Collections.sort(media, AlbumsComparators.getComparator(sortingMode));*/
        Collections.sort(media, MediaComparators.getComparator(sortingMode, sortingOrder));
        /*if (sortingOrder.equals(SortingOrder.DESCENDING))
            reverseOrder();*/

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return media.get(position).getUri().hashCode() ^ 1312;
    }

    public SortingOrder sortingOrder() {
        return sortingOrder;
    }

    public void changeSortingOrder(SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder;
        Collections.reverse(media);
        notifyDataSetChanged();
    }

    public SortingMode sortingMode() {
        return sortingMode;
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
        onChangeSelectedSubject.onNext(new Media());
    }

    public void clearSelected() {
        for (int i = 0; i < media.size(); i++)
            if (media.get(i).setSelected(false))
                notifyItemChanged(i);
        selectedCount = 0;
        onChangeSelectedSubject.onNext(new Media());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false));
    }

    private void notifySelected(boolean increase) {
        selectedCount += increase ? 1 : -1;
    }

    public boolean selecting() {
        return selectedCount > 0;
    }

    public Observable<Integer> getClicks() {
        return onClickSubject;
    }

    public Observable<Media> getSelectedClicks() {
        return onChangeSelectedSubject;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Media f = media.get(position);

        //holder.path.setTag(f);
        holder.icon.setVisibility(View.GONE);

        if (f.isGif()) {
            Ion.with(holder.imageView.getContext())
                    .load(f.getPath())
                    .intoImageView(holder.imageView);
            holder.gifIcon.setVisibility(View.VISIBLE);
        } else {

            RequestOptions options = new RequestOptions()
                    .signature(f.getSignature())
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .centerCrop()
                    .placeholder(placeholder)
                    //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);


            Glide.with(holder.imageView.getContext())
                    .load(f.getUri())
                    .apply(options)
                    .thumbnail(0.5f)
                    .into(holder.imageView);
            holder.gifIcon.setVisibility(View.GONE);
        }

        if(f.isVideo()) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(f.getName());
            /*holder.path.setTextColor(ContextCompat.getColor(holder.path.getContext(), R.color.md_dark_primary_text));
            holder.path.setBackgroundColor(
                    ColorPalette.getTransparentColor(
                            ContextCompat.getColor(holder.path.getContext(), R.color.md_black_1000), 100));*/
            holder.icon.setIcon(CommunityMaterial.Icon.cmd_play_circle);
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
            holder.path.animate().alpha(1).setDuration(250);
            holder.icon.setColor(getThemeHelper().getPrimaryColor());
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
            holder.layout.setPadding(15,15,15,15);
            holder.icon.setColor(getThemeHelper().getPrimaryColor());
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
            //holder.layout.setBackgroundColor(ThemeHelper.getPrimaryColor(holder.path.getContext()));
        } else {
            holder.imageView.clearColorFilter();
            holder.layout.setPadding(0,0,0,0);
        }

        holder.layout.setOnClickListener(v -> {
            if (selecting()) {
                notifySelected(f.toggleSelected());
                notifyItemChanged(position);
                onChangeSelectedSubject.onNext(f);
            } else
                onClickSubject.onNext(position);
        });

        holder.layout.setOnLongClickListener(v -> {
            if (!selecting()) {
                // If it is the first long press
                notifySelected(f.toggleSelected());
                notifyItemChanged(position);
                onChangeSelectedSubject.onNext(f);
            } else {
                selectAllUpTo(f);
                onChangeSelectedSubject.onNext(new Media());
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
        IconicsImageView gifIcon;
        @BindView(R.id.icon)
        IconicsImageView icon;
        @BindView(R.id.media_card_layout)
        SquareRelativeLayout layout;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            icon.setColor(Color.WHITE);
            Log.wtf("asd", "asdasd");
        }
    }
}