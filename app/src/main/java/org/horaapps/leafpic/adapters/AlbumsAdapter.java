package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.CardViewStyle;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.sort.AlbumsComparators;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import horaapps.org.liz.ColorPalette;
import horaapps.org.liz.Theme;
import horaapps.org.liz.ThemeHelper;
import horaapps.org.liz.ThemedAdapter;
import horaapps.org.liz.ThemedViewHolder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends ThemedAdapter<AlbumsAdapter.ViewHolder> {

    private List<Album> albums;

    private final PublishSubject<Album> onClickSubject = PublishSubject.create();
    private final PublishSubject<Album> onChangeSelectedSubject = PublishSubject.create();

    private int selectedCount = 0;

    private SortingOrder sortingOrder;
    private SortingMode sortingMode;

    private Drawable placeholder;
    private CardViewStyle cvs;

    public AlbumsAdapter(Context context, SortingMode sortingMode, SortingOrder sortingOrder, ArrayList<Album> albums) {
        super(context);
        this.albums = albums;
        placeholder = getThemeHelper().getPlaceHolder();
        cvs = CardViewStyle.fromValue(Hawk.get("card_view_style", 0));
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
    }

    public void sort() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            albums.sort(AlbumsComparators.getComparator(sortingMode));
            //albums = albums.stream().sorted(AlbumsComparators.getComparator(sortingMode)).collect(Collectors.toList());
        else Collections.sort(albums, AlbumsComparators.getComparator(sortingMode));*/
        Collections.sort(albums, AlbumsComparators.getComparator(sortingMode, sortingOrder));
        /*if (sortingOrder.equals(SortingOrder.DESCENDING))
            reverseOrder();*/

        notifyDataSetChanged();
    }

    public void notifyItemChanaged(Album album) {
        notifyItemChanged(albums.indexOf(album));
    }

    public SortingOrder sortingOrder() {
        return sortingOrder;
    }

    public void changeSortingOrder(SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder;
        reverseOrder();
        notifyDataSetChanged();
    }

    public SortingMode sortingMode() {
        return sortingMode;
    }

    public void changeSortingMode(SortingMode sortingMode) {
        this.sortingMode = sortingMode;
        sort();
    }

    public List<Album> getSelectedAlbums() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return albums.stream().filter(Album::isSelected).collect(Collectors.toList());
        } else {
            ArrayList<Album> arrayList = new ArrayList<>(selectedCount);
            for (Album album : albums)
                if (album.isSelected())
                    arrayList.add(album);
            return arrayList;
        }
    }

    public Album getFirstSelectedAlbum() {
        if (selectedCount > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return albums.stream().filter(Album::isSelected).findFirst().orElse(null);
            else
                for (Album album : albums)
                    if (album.isSelected())
                        return album;
        }
        return null;
    }


    public int getSelectedCount() {
        return selectedCount;
    }

    public void selectAll() {
        for (int i = 0; i < albums.size(); i++)
            if (albums.get(i).setSelected(true))
                notifyItemChanged(i);
        selectedCount = albums.size();
        onChangeSelectedSubject.onNext(Album.getEmptyAlbum());
    }

    public void removeSelectedAlbums(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            albums.removeIf(Album::isSelected);
        else {
            Iterator<Album> iter = albums.iterator();

            while (iter.hasNext()) {
                Album album = iter.next();

                if (album.isSelected())
                    iter.remove();
            }
        }
        selectedCount = 0;
        notifyDataSetChanged();
    }

    public void removeAlbumsThatStartsWith(String path){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            albums.removeIf(album -> album.getPath().startsWith(path));
        else {
            Iterator<Album> iter = albums.iterator();

            while (iter.hasNext()) {
                Album album = iter.next();

                if (album.getPath().startsWith(path))
                    iter.remove();
            }
        }

        notifyDataSetChanged();
    }

    public void clearSelected() {
        for (int i = 0; i < albums.size(); i++)
            if (albums.get(i).setSelected(false))
                notifyItemChanged(i);
        selectedCount = 0;
        onChangeSelectedSubject.onNext(Album.getEmptyAlbum());
    }

    public void forceSelectedCount(int count) {
        selectedCount = count;
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        placeholder = theme.getPlaceHolder();

        cvs = CardViewStyle.fromValue(Hawk.get("card_view_style", 0));
        super.refreshTheme(theme);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (cvs) {
            default:
            case MATERIAL: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_material, parent, false); break;
            case FLAT: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_flat, parent, false); break;
            case COMPACT: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_compact, parent, false); break;
        }
        return new ViewHolder(v);
    }

    private void notifySelected(boolean increase) {
        selectedCount += increase ? 1 : -1;
    }

    public boolean selecting() {
        return selectedCount > 0;
    }

    public Observable<Album> getClicks() {
        return onClickSubject;
    }

    public Observable<Album> getSelectedClicks() {
        return onChangeSelectedSubject;
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {

        Album a = albums.get(position);
        holder.refreshTheme(getThemeHelper(), cvs, a.isSelected());

        Media f = a.getCover();

        RequestOptions options = new RequestOptions()
                .signature(f.getSignature())
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .placeholder(placeholder)
                .error(org.horaapps.leafpic.R.drawable.ic_error)
                //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(holder.picture.getContext())
                .load(f.getPath())
                .apply(options)
                .into(holder.picture);


        int accentColor = getThemeHelper().getAccentColor();

        if (accentColor == getThemeHelper().getPrimaryColor())
            accentColor = ColorPalette.getDarkerColor(accentColor);

        int textColor = getThemeHelper().getColor(getThemeHelper().getBaseTheme().equals(Theme.LIGHT) ? R.color.md_album_color_2 : R.color.md_album_color);

        if (a.isSelected())
            textColor = getThemeHelper().getColor(R.color.md_album_color);

        holder.mediaLabel.setTextColor(textColor);

        holder.llCount.setVisibility(Hawk.get("show_n_photos", true) ? View.VISIBLE : View.GONE);
        holder.name.setText(StringUtils.htmlFormat(a.getName(), textColor, false, true));
        holder.nMedia.setText(StringUtils.htmlFormat(String.valueOf(a.getCount()), accentColor, true, false));
        holder.path.setVisibility(Hawk.get("show_album_path", false) ? View.VISIBLE : View.GONE);
        holder.path.setText(a.getPath());

        //START Animation MAKES BUG ON FAST TAP ON CARD
        //Animation anim;
        //anim = AnimationUtils.loadAnimation(holder.albumCard.getContext(), R.anim.slide_fade_card);
        //holder.albumCard.startAnimation(anim);
        //ANIMS
        //holder.card.animate().alpha(1).setDuration(250);

        holder.card.setOnClickListener(v -> {
            if (selecting()) {
                notifySelected(a.toggleSelected());
                notifyItemChanged(position);
                onChangeSelectedSubject.onNext(a);
            } else
                onClickSubject.onNext(a);
        });

        holder.card.setOnLongClickListener(v -> {
            notifySelected(a.toggleSelected());
            notifyItemChanged(position);
            onChangeSelectedSubject.onNext(a);
            return true;
        });
    }

    public void clear() {
        albums.clear();
        notifyDataSetChanged();
    }

    public int add(Album album) {
        int i = Collections.binarySearch(
                albums, album, AlbumsComparators.getComparator(sortingMode, sortingOrder));
        if (i < 0) i = ~i;
        albums.add(i, album);
        notifyItemInserted(i);
        //int finalI = i;
        //((ThemedActivity) context).runOnUiThread(() -> notifyItemInserted(finalI));
        return i;

    }

    private void reverseOrder() {
        int z = 0, size = getItemCount();
        while (z < size && albums.get(z).isPinned())
            z++;

        for (int i = Math.max(0, z), mid = (i+size)>>1, j = size-1; i < mid; i++, j--)
            Collections.swap(albums, i, j);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    static class ViewHolder extends ThemedViewHolder {

        @BindView(R.id.album_card) CardView card;
        @BindView(R.id.album_preview) ImageView picture;
        @BindView(R.id.selected_icon) View selectedIcon;
        @BindView(R.id.ll_album_info) View footer;
        @BindView(R.id.ll_media_count) View llCount;
        @BindView(R.id.album_name) TextView name;
        @BindView(R.id.album_media_count) TextView nMedia;
        @BindView(R.id.album_media_label) TextView mediaLabel;
        @BindView(R.id.album_path) TextView path;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void refreshTheme(ThemeHelper theme, CardViewStyle cvs, boolean selected) {

            if (selected) {
                footer.setBackgroundColor(theme.getPrimaryColor());
                picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                selectedIcon.setVisibility(View.VISIBLE);
            } else {
                picture.clearColorFilter();
                selectedIcon.setVisibility(View.GONE);
                switch (cvs) {
                    default: case MATERIAL:
                        footer.setBackgroundColor(theme.getCardBackgroundColor());
                        break;
                    case FLAT: case COMPACT:
                        footer.setBackgroundColor(ColorPalette.getTransparentColor(theme.getBackgroundColor(), 150));
                        break;
                }
            }

            path.setTextColor(theme.getSubTextColor());
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {

        }
    }
}