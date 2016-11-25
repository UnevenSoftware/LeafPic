package org.horaapps.leafpic.views.cardviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.CardViewStyle;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.ThemeHelper;
import org.horaapps.leafpic.views.SquareImageView;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter {

    public static int MAX_ELEVATION_FACTOR = 8;

    private List<CardView> mViews;
    private float mBaseElevation;
    private ThemeHelper theme;

    public CardPagerAdapter(Context context) {
        theme = new ThemeHelper(context);
        mViews = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            mViews.add(null);
        }
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.card_view_sample, container, false);

        /**OBJECTS**/
        final SquareImageView imgPreviewAlbumCardStyle = (SquareImageView) view.findViewById(R.id.preview_album_card_style_image);
        imgPreviewAlbumCardStyle.setBackgroundColor(theme.getPrimaryColor());

        LinearLayout llPreview = (LinearLayout) view.findViewById(R.id.ll_preview_album_card);
        //llPreview.setBackgroundColor(theme.getInvertedBackgroundColor());

        final LinearLayout llPreviewFlatCompactText = (LinearLayout) view.findViewById(R.id.preview_album_card_style_linear_card_text2);
        final TextView txtPreviewFlatCompactAlbum = (TextView) view.findViewById(R.id.preview_album_card_style_name2);
        final TextView txtPreviewFlatCompactMedia = (TextView) view.findViewById(R.id.preview_album_card_style_photos_count2);

        final LinearLayout llPreviewMaterialText = (LinearLayout) view.findViewById(R.id.preview_album_card_style_linear_card_text);
        final TextView txtPreviewMaterialAlbum = (TextView) view.findViewById(R.id.preview_album_card_style_name);
        final TextView txtPreviewMaterialMedia = (TextView) view.findViewById(R.id.preview_album_card_style_photos_count);

        llPreviewMaterialText.setBackgroundColor(theme.getCardBackgroundColor());
        llPreviewFlatCompactText.setBackgroundColor(ColorPalette.getTransparentColor(theme.getCardBackgroundColor(), 150));

        int color=theme.getTextColor();
        String albumNameHtml = "<i><font color='" + color+ "'>" + container.getContext().getString(R.string.album) + "</font></i>";
        txtPreviewFlatCompactAlbum.setTextColor(color);
        txtPreviewMaterialAlbum.setTextColor(color);
        txtPreviewFlatCompactAlbum.setText(Html.fromHtml(albumNameHtml));
        txtPreviewMaterialAlbum.setText(Html.fromHtml(albumNameHtml));

        color=theme.getSubTextColor();
        String hexAccentColor = String.format("#%06X", (0xFFFFFF & theme.getAccentColor()));
        String albumPhotoCountHtml = "<b><font color='" + hexAccentColor + "'>" + "n" + "</font></b>" + "<font " +
                "color='" + color + "'> " + container.getContext().getString(R.string.media) + "</font>";
        txtPreviewFlatCompactMedia.setTextColor(color);
        txtPreviewMaterialMedia.setTextColor(color);
        txtPreviewFlatCompactMedia.setText(Html.fromHtml(albumPhotoCountHtml));
        txtPreviewMaterialMedia.setText(Html.fromHtml(albumPhotoCountHtml));


        switch (CardViewStyle.fromValue(position)){
            default:
            case CARD_MATERIAL:
                llPreviewFlatCompactText.setVisibility(View.GONE);
                llPreviewMaterialText.setVisibility(View.VISIBLE);
                break;
            case CARD_FLAT:
                llPreviewFlatCompactText.setVisibility(View.VISIBLE);
                txtPreviewFlatCompactMedia.setVisibility(View.VISIBLE);
                llPreviewMaterialText.setVisibility(View.GONE);
                break;
            case CARD_COMPACT:
                llPreviewFlatCompactText.setVisibility(View.VISIBLE);
                txtPreviewFlatCompactMedia.setVisibility(View.GONE);
                llPreviewMaterialText.setVisibility(View.GONE);
                break;
        }

        container.addView(view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

}
