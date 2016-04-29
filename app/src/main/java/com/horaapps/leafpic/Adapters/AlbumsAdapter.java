package com.horaapps.leafpic.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.horaapps.leafpic.Base.newAlbum;
import com.horaapps.leafpic.Base.newMedia;
import com.horaapps.leafpic.R;

import java.util.ArrayList;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    ArrayList<newAlbum> albums;
    SharedPreferences SP;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public AlbumsAdapter(ArrayList<newAlbum> ph, Context ctx) {
        albums = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_card, parent, false);

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
        newAlbum a = albums.get(position);
        Context c = holder.picture.getContext();
        newMedia f = a.getCoverAlbum();
        /*
        YoYo.with(Techniques.SlideInUp)
                .duration(100)
                .playOn(holder.cv);
        */
        Glide.with(c)
                .load(f.getPath())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .priority(Priority.HIGH)
                /*.signature(a.hasCustomCover()
                        ? new StringSignature(f.Path)
                        : new MediaStoreSignature(f.MIME, f.DateModified, f.orientation))*/
                .centerCrop()
                .placeholder(SP.getInt("basic_theme", 1)==1
                        ? R.drawable.ic_empty_white
                        : (SP.getInt("basic_theme", 1)==2
                                ? R.drawable.ic_empty
                                : R.drawable.ic_empty_amoled))
                .animate(R.anim.fade_in)
                .into(holder.picture);

        holder.name.setTag(position);

        String hexPrimaryColor = String.format("#%06X", (0xFFFFFF & SP.getInt("primary_color", ContextCompat.getColor(holder.card_layout.getContext(), R.color.md_indigo_500))));
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & SP.getInt("accent_color", ContextCompat.getColor(holder.card_layout.getContext(), R.color.md_light_blue_500))));

        if (hexAccentColor.equals(hexPrimaryColor)) {
            float[] hsv = new float[3];
            int color = SP.getInt("accent_color", ContextCompat.getColor(c, R.color.md_light_blue_500));
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.72f; // value component
            color = Color.HSVToColor(hsv);
            hexAccentColor= String.format("#%06X", (0xFFFFFF & color));
        }

        String textColor = SP.getInt("basic_theme", 1)!=1 ? "#FAFAFA" : "#2b2b2b";

        if (a.isSelected()) {
            holder.card_layout.setBackgroundColor(Color.parseColor(hexPrimaryColor));
            holder.picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
            holder.selectHolder.setVisibility(View.VISIBLE);
            if (SP.getInt("basic_theme", 1)==1 ) textColor ="#FAFAFA";
        } else {
            holder.picture.clearColorFilter();
            holder.selectHolder.setVisibility(View.GONE);

            if (SP.getInt("basic_theme", 1)==1)
                holder.card_layout.setBackgroundColor(ContextCompat.getColor(c, R.color.md_light_cards));
            else if (SP.getInt("basic_theme", 1)==2)
                holder.card_layout.setBackgroundColor(ContextCompat.getColor(c, R.color.md_dark_cards));
            else holder.card_layout.setBackgroundColor(ContextCompat.getColor(c, R.color.md_black_1000));
        }
        holder.name.setText(Html.fromHtml("<i><font color='" + textColor + "'>" + a.getName() + "</font></i>"));
        holder.nPhotos.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>" + a.getCount() + "</font></b>" + "<font " +
                "color='" + textColor + "'> " +a.getContentDescdription(c) + "</font>"));
        // (a.getImagesCount() == 1 ? c.getString(R.string.singular_photo) : c.getString(R.string.plural_photos))
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public void setOnLongClickListener(View.OnLongClickListener lis) {
        mOnLongClickListener = lis;
    }

    public void updateDataset(ArrayList<newAlbum> asd) {
        albums = asd;
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card_layout;
        ImageView picture;
        ImageView selectHolder;
        TextView name;
        TextView nPhotos;
        CardView cv;

        public ViewHolder(View itemView) {
            super(itemView);
            picture = (ImageView) itemView.findViewById(R.id.album_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            card_layout = (LinearLayout) itemView.findViewById(R.id.linear_card_text);
            name = (TextView) itemView.findViewById(R.id.album_name);
            nPhotos = (TextView) itemView.findViewById(R.id.album_photos_count);
            cv = (CardView) itemView.findViewById(R.id.album_card);
        }
    }
}



