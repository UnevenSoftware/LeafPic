package org.horaapps.leafpic.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import org.horaapps.leafpic.Data.Album;
import org.horaapps.leafpic.Data.Media;
import org.horaapps.leafpic.utils.ThemeHelper;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private ArrayList<Album> albums;

    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private ThemeHelper theme;

    private BitmapDrawable placeholder;

    public AlbumsAdapter(ArrayList<Album> ph, Context context) {
        albums = ph;
        theme = new ThemeHelper(context);
        updateTheme();
    }

    public void updateTheme() {
        theme.updateTheme();
        placeholder = ((BitmapDrawable) theme.getPlaceHolder());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(org.horaapps.leafpic.R.layout.card_album, parent, false);

        v.setOnClickListener(mOnClickListener);
        v.setOnLongClickListener(mOnLongClickListener);
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
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {
        Album a = albums.get(position);
        Context c = holder.picture.getContext();
        Media f = a.getCoverAlbum();

        Glide.with(c)
                .load(f.getPath())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .priority(Priority.HIGH)
                .signature(new StringSignature(f.getPath() +"-"+ f.getDateModified()))
                .centerCrop()
                .error(org.horaapps.leafpic.R.drawable.ic_error)
                .placeholder(placeholder)
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into(holder.picture);

        holder.name.setTag(position);

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
            holder.layout.setBackgroundColor(theme.getCardBackgroundColor());
        }
        // TODO: 02/08/16 Html.fromHtml deprecated
        holder.name.setText(Html.fromHtml("<i><font color='" + textColor + "'>" + a.getName() + "</font></i>"));
        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + a.getCount() + "</font></b>" + "<font " +
                                                     "color='" + textColor + "'> " +a.getContentDescription(c) + "</font>"));
        // (a.getImagesCount() == 1 ? c.getString(R.string.singular_photo) : c.getString(R.string.plural_photos))
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void swapDataSet(ArrayList<Album> asd) {
        albums = asd;
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



