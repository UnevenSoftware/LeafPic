package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
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
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.ThemeHelper;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private ArrayList<Album> albums;
    private CardViewStyle cvs;

    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private ThemeHelper theme;

    private BitmapDrawable placeholder;

    public AlbumsAdapter(ArrayList<Album> ph, Context context) {
        albums = ph;
        theme = new ThemeHelper(context);
        updateTheme(context);
    }

    public void updateTheme(Context context) {
        theme.updateTheme();
        placeholder = ((BitmapDrawable) theme.getPlaceHolder());
        cvs = CardViewStyle.fromValue(PreferenceUtil.getInstance(context).getInt("card_view_style",CardViewStyle.CARD_MATERIAL.getValue()));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (cvs) {
            default:
            case CARD_MATERIAL: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_material, parent, false); break;
            case CARD_FLAT: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_flat, parent, false); break;
            case CARD_COMPACT: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_compact, parent, false); break;
        }
        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        Media f = a.getCoverAlbum();

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

        holder.name.setTag(a);

        String hexPrimaryColor = String.format("#%06X", (0xFFFFFF & theme.getPrimaryColor()));
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & theme.getAccentColor()));

        if (hexAccentColor.equals(hexPrimaryColor)) {
            float[] hsv = new float[3];
            int color = theme.getAccentColor();
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.72f; // value component
            color = Color.HSVToColor(hsv);
            hexAccentColor= String.format("#%06X", (0xFFFFFF & color));
        }

        String textColor = theme.getBaseTheme() != ThemeHelper.LIGHT_THEME ? "#FAFAFA" : "#2b2b2b";

        if (a.isSelected()) {
            holder.layout.setBackgroundColor(Color.parseColor(hexPrimaryColor));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectedIcon.setVisibility(View.VISIBLE);
            if (theme.getBaseTheme() == ThemeHelper.LIGHT_THEME ) textColor = "#FAFAFA";
        } else {
            holder.picture.clearColorFilter();
            holder.selectedIcon.setVisibility(View.GONE);
            switch (cvs){
                default:
                case CARD_MATERIAL:holder.layout.setBackgroundColor(theme.getCardBackgroundColor());break;
                case CARD_FLAT:
                case CARD_COMPACT:holder.layout.setBackgroundColor(ColorPalette.getTransparentColor(theme.getBackgroundColor(), 150)); break;
            }
        }

        String albumNameHtml = "<i><font color='" + textColor + "'>" + a.getName() + "</font></i>";
        String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>" + a.getCount() + "</font></b>" + "<font " +
                "color='" + textColor + "'> " + holder.nPhotos.getContext().getString(R.string.media) + "</font>";

        holder.name.setText(StringUtils.html(albumNameHtml));
        holder.nPhotos.setText(StringUtils.html(albumPhotoCountHtml));

        // (a.getImagesCount() == 1 ? c.getString(R.string.singular_photo) : c.getString(R.string.plural_photos))
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void swapDataSet(ArrayList<Album> asd) {

        // TODO improve this
        albums.clear();
        albums.addAll(asd);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView picture;
        View selectedIcon, layout;
        TextView name, nPhotos;

        ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(org.horaapps.leafpic.R.id.album_preview);
            selectedIcon = itemView.findViewById(org.horaapps.leafpic.R.id.selected_icon);
            layout = itemView.findViewById(org.horaapps.leafpic.R.id.linear_card_text);
            name = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.album_photos_count);
        }
    }
}



