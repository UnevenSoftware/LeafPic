package org.horaapps.leafpic.views.cardviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter {

    static int MAX_ELEVATION_FACTOR = 8;

    private List<CardView> mViews;
    private float mBaseElevation;
    private ThemeHelper theme;

    public CardPagerAdapter(Context context) {
        theme = new ThemeHelper(context);
        mViews = new ArrayList<>(CardViewStyle.getSize());
        for (int i = 0; i < CardViewStyle.getSize(); i++)
            mViews.add(null);

    }

    float getBaseElevation() {
        return mBaseElevation;
    }

    CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return CardViewStyle.getSize();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v;

        switch (CardViewStyle.fromValue(position)){
            default:
            case CARD_MATERIAL: v = LayoutInflater.from(container.getContext()).inflate(R.layout.card_album_material, container, false); break;
            case CARD_FLAT: v = LayoutInflater.from(container.getContext()).inflate(R.layout.card_album_flat, container, false); break;
            case CARD_COMPACT: v = LayoutInflater.from(container.getContext()).inflate(R.layout.card_album_compact, container, false); break;
        }

        ImageView img = (ImageView) v.findViewById(org.horaapps.leafpic.R.id.album_preview);
        img.setBackgroundColor(theme.getPrimaryColor());

        Glide.with(container.getContext())
                .load(R.drawable.gilbert_profile)
                .into(img);

        String hexPrimaryColor = ColorPalette.getHexColor(theme.getPrimaryColor());
        String hexAccentColor = ColorPalette.getHexColor(theme.getAccentColor());

        if (hexAccentColor.equals(hexPrimaryColor))
            hexAccentColor = ColorPalette.getHexColor(ColorPalette.getDarkerColor(theme.getAccentColor()));

        String textColor = theme.getBaseTheme() != ThemeHelper.LIGHT_THEME ? "#FAFAFA" : "#2b2b2b";

        switch (CardViewStyle.fromValue(position)){
            default:
            case CARD_MATERIAL:v.findViewById(R.id.linear_card_text).setBackgroundColor(theme.getCardBackgroundColor());break;
            case CARD_FLAT:
            case CARD_COMPACT:v.findViewById(R.id.linear_card_text).setBackgroundColor(ColorPalette.getTransparentColor(theme.getBackgroundColor(), 150)); break;
        }

        String albumNameHtml = "<i><font color='" + textColor + "'>#PraiseDuarte</font></i>";
        String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>420</font></b>" + "<font " +
                "color='" + textColor + "'> " + container.getContext().getString(R.string.media) + "</font>";

        ((TextView) v.findViewById(R.id.album_name)).setText(StringUtils.html(albumNameHtml));
        ((TextView) v.findViewById(R.id.album_photos_count)).setText(StringUtils.html(albumPhotoCountHtml));

        ((CardView) v).setUseCompatPadding(true);
        ((CardView) v).setRadius(2);

        container.addView(v);

        if (mBaseElevation == 0) {
            mBaseElevation = ((CardView) v).getCardElevation();
        }

        ((CardView) v).setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, ((CardView) v));

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

}
