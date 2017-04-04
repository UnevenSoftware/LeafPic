package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.Media;
import org.horaapps.leafpic.model.base.AlbumsComparators;
import org.horaapps.leafpic.model.base.SortingMode;
import org.horaapps.leafpic.model.base.SortingOrder;
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.Theme;
import org.horaapps.leafpic.util.ThemeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private List<Album> albums;

    private final PublishSubject<Album> onClickSubject = PublishSubject.create();
    private final PublishSubject<Album> onChangeSelectedSubject = PublishSubject.create();

    private int selectedCount = 0;

    private SortingOrder sortingOrder;
    private SortingMode sortingMode;

    private ThemeHelper theme;
    private BitmapDrawable placeholder;
    private CardViewStyle cvs;

    public AlbumsAdapter(Context context, SortingMode sortingMode, SortingOrder sortingOrder) {
        albums = new ArrayList<>();
        updateTheme(ThemeHelper.getThemeHelper(context));
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

    public void selectAllAlbums() {
        for (int i = 0; i < albums.size(); i++)
            if (albums.get(i).setSelected(true))
                notifyItemChanged(i);
        selectedCount = albums.size();
        onChangeSelectedSubject.onNext(Album.getEmptyAlbum());
    }

    public void clearSelectedAlbums() {
        for (int i = 0; i < albums.size(); i++)
            if (albums.get(i).setSelected(false))
                notifyItemChanged(i);
        selectedCount = 0;
        onChangeSelectedSubject.onNext(Album.getEmptyAlbum());
    }

    public void updateTheme(ThemeHelper theme) {
        this.theme = theme;
        placeholder = ((BitmapDrawable) theme.getPlaceHolder());
        cvs = theme.getCardViewStyle();
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

    public Observable<Album> getAlbumsClicks() {
        return onClickSubject;
    }

    public Observable<Album> getAlbumsSelectedClicks() {
        return onChangeSelectedSubject;
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {

        Album a = albums.get(position);

        Media f = a.getCover();

        Glide.with(holder.picture.getContext())
                .load(f.getPath())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .priority(Priority.HIGH)
                .signature(f.getSignature())
                .centerCrop()
                .error(org.horaapps.leafpic.R.drawable.ic_error)
                .placeholder(placeholder)
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into(holder.picture);

        //holder.name.setTag(a);

        String hexPrimaryColor = ColorPalette.getHexColor(theme.getPrimaryColor());
        String hexAccentColor = ColorPalette.getHexColor(theme.getAccentColor());

        if (hexAccentColor.equals(hexPrimaryColor))
            hexAccentColor = ColorPalette.getHexColor(ColorPalette.getDarkerColor(theme.getAccentColor()));

        // TODO: 4/4/17 use ints
        String textColor = theme.getBaseTheme().equals(Theme.LIGHT) ? "#2B2B2B" : "#FAFAFA";

        if (a.isSelected()) {
            holder.footer.setBackgroundColor(Color.parseColor(hexPrimaryColor));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectedIcon.setVisibility(View.VISIBLE);
            if (theme.getBaseTheme().equals(Theme.LIGHT)) textColor = "#FAFAFA";
        } else {
            holder.picture.clearColorFilter();
            holder.selectedIcon.setVisibility(View.GONE);
            switch (cvs){
                default:
                case MATERIAL:holder.footer.setBackgroundColor(theme.getCardBackgroundColor());break;
                case FLAT:
                case COMPACT:holder.footer.setBackgroundColor(ColorPalette.getTransparentColor(theme.getBackgroundColor(), 150)); break;
            }
        }

        holder.llCount.setVisibility(PreferenceUtil.getBool(holder.llCount.getContext(), "show_n_photos", true) ? View.VISIBLE : View.GONE);

        holder.mediaLabel.setTextColor(theme.getTextColor());

        holder.name.setText(StringUtils.htmlFormat(a.getName(), textColor, false, true));
        holder.nMedia.setText(StringUtils.htmlFormat(String.valueOf(a.getCount()), hexAccentColor, true, false));

        holder.path.setVisibility(PreferenceUtil.getBool(holder.path.getContext(),"show_album_path", false) ? View.VISIBLE : View.GONE);

        holder.path.setTextColor(theme.getSubTextColor());
        holder.path.setText(a.getPath());
        //holder.path.setSelected(true);

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

    public void addAlbum(Album album) {
        int i = Collections.binarySearch(
                albums, album, AlbumsComparators.getComparator(sortingMode, sortingOrder));
        if (i < 0) i = ~i;
        albums.add(i, album);
        notifyItemInserted(i);
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


    static class ViewHolder extends RecyclerView.ViewHolder {

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
    }
}