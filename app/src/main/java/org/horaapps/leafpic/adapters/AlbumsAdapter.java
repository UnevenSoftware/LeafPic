package org.horaapps.leafpic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
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
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.Theme;
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
    private PreferenceUtil SP;


    private BitmapDrawable placeholder;

    public AlbumsAdapter(ArrayList<Album> ph, Context context) {
        albums = ph;
        theme = new ThemeHelper(context);
        updateTheme(context);
    }

    public void updateTheme(Context context) {
        SP = PreferenceUtil.getInstance(context);
        theme.updateTheme();
        placeholder = ((BitmapDrawable) theme.getPlaceHolder());
        cvs = CardViewStyle.fromValue(PreferenceUtil.getInstance(context).getInt("card_view_style",CardViewStyle.MATERIAL.getValue()));
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
                 //.placeholder(placeholder)
                .animate(org.horaapps.leafpic.R.anim.fade_in)
                .into(holder.picture);

        holder.name.setTag(a);

        String hexPrimaryColor = ColorPalette.getHexColor(theme.getPrimaryColor());
        String hexAccentColor = ColorPalette.getHexColor(theme.getAccentColor());

        if (hexAccentColor.equals(hexPrimaryColor))
            hexAccentColor = ColorPalette.getHexColor(ColorPalette.getDarkerColor(theme.getAccentColor()));

        String textColor = theme.getBaseTheme().equals(Theme.LIGHT) ? "#2B2B2B" : "#FAFAFA";

        if (a.isSelected()) {
            holder.layout.setBackgroundColor(Color.parseColor(hexPrimaryColor));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectedIcon.setVisibility(View.VISIBLE);
            if (theme.getBaseTheme().equals(Theme.LIGHT)) textColor = "#FAFAFA";
        } else {
            holder.picture.clearColorFilter();
            holder.selectedIcon.setVisibility(View.GONE);
            switch (cvs){
                default:
                case MATERIAL:holder.layout.setBackgroundColor(theme.getCardBackgroundColor());break;
                case FLAT:
                case COMPACT:holder.layout.setBackgroundColor(ColorPalette.getTransparentColor(theme.getBackgroundColor(), 150)); break;
            }
        }

        if(cvs.equals(CardViewStyle.MATERIAL)){
            holder.itemView.findViewById(R.id.preview_card_text).setBackgroundColor(ColorPalette.getTransparentColor(a.isSelected() ? theme.getPrimaryColor() : theme.getBackgroundColor(), 150));
            ((TextView) holder.itemView.findViewById(R.id.preview_media_name)).setText(a.getCoverAlbum().getName().toString());
            ((TextView) holder.itemView.findViewById(R.id.preview_media_path)).setText(a.getPath().toString());
            ((TextView) holder.itemView.findViewById(R.id.preview_media_path)).setTextColor(theme.getSubTextColor());
            ((TextView) holder.itemView.findViewById(R.id.preview_media_path)).setSelected(true);
        }


        holder.llMdia.setVisibility(SP.getBoolean("show_n_photos", true) ? View.VISIBLE : View.GONE);
        String albumNameHtml = "<i><font color='" + textColor + "'>" + a.getName() + "</font></i>";
        String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>" + a.getCount() + "</font></b>";

        holder.mediaLabel.setTextColor(theme.getTextColor());
        holder.name.setText(StringUtils.html(albumNameHtml));
        holder.nPhotos.setText(StringUtils.html(albumPhotoCountHtml));

        //START Animation MAKES BUG ON FAST TAP ON CARD
        //Animation anim;
        //anim = AnimationUtils.loadAnimation(holder.albumCard.getContext(), R.anim.slide_fade_card);
        //holder.albumCard.startAnimation(anim);
        //ANIMS
        holder.albumCard.animate().alpha(1).setDuration(250);
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
        View selectedIcon, layout, llMdia;
        TextView name, nPhotos, mediaLabel;
        CardView albumCard;

        ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(org.horaapps.leafpic.R.id.album_preview);
            selectedIcon = itemView.findViewById(org.horaapps.leafpic.R.id.selected_icon);
            llMdia = itemView.findViewById(R.id.ll_n_media);
            layout = itemView.findViewById(org.horaapps.leafpic.R.id.linear_card_text);
            name = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(org.horaapps.leafpic.R.id.album_photos_count);
            mediaLabel = (TextView) itemView.findViewById(R.id.album_media_label);
            albumCard = (CardView) itemView.findViewById(R.id.album_card);
        }
    }
}



