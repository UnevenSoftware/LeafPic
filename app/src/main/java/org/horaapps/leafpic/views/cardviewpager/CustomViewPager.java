package org.horaapps.leafpic.views.cardviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
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
import java.util.ListIterator;

import static org.horaapps.leafpic.util.CardViewStyle.CARD_COMPACT;
import static org.horaapps.leafpic.util.CardViewStyle.CARD_FLAT;
import static org.horaapps.leafpic.util.CardViewStyle.CARD_MATERIAL;

/**
 * Created by Jibo on 25/11/2016.
 */
public class CustomViewPager extends ViewPager {

    int MAX_ELEVATION_FACTOR = 8;
    int MAGIC_NUMBER = 21;
    private View mCurrentView;

    ArrayList<Integer> heightPorcoDio = new ArrayList<>();

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        CardPagerAdapter adapter = new CardPagerAdapter(context);
        setAdapter(adapter);
        setPageTransformer(false, new ShadowTransformer(this, adapter));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mCurrentView == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        mCurrentView.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        int h = mCurrentView.getMeasuredHeight()/* - (mCurrentView.getPaddingBottom() + MAGIC_NUMBER)*/;

        if (!heightPorcoDio.contains(h))
         heightPorcoDio.add(h);
        //h = getMin(heightPorcoDio);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    int getMin(ArrayList<Integer> list) {
        if (list.isEmpty()) {
           return 0;
        } else {
            ListIterator<Integer> itr = list.listIterator();
            int min = itr.next(); // first element as the current minimum
            while (itr.hasNext()) {
                Integer curr = itr.next();
                if (curr.compareTo(min) < 0) min = curr;
            }
            return min;
        }
    }


    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        measureCurrentView(((CardPagerAdapter) getAdapter()).getCardViewAt(position).findViewById(R.id.cont));
    }

    public void measureCurrentView(View currentView) {
        heightPorcoDio.clear();
        mCurrentView = currentView;
        requestLayout();
    }

    class CardPagerAdapter extends PagerAdapter {

        private List<CardView> mViews;
        private float mBaseElevation;
        private ThemeHelper theme;

        CardPagerAdapter(Context context) {
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
                case CARD_MATERIAL: v = LayoutInflater.from(container.getContext()).inflate(CARD_MATERIAL.getLayout(), container, false ); break;
                case CARD_FLAT: v = LayoutInflater.from(container.getContext()).inflate(CARD_FLAT.getLayout(), container, false); break;
                case CARD_COMPACT: v = LayoutInflater.from(container.getContext()).inflate(CARD_COMPACT.getLayout(), container, false); break;
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

            v.setPadding(20,20,20,20);

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

    public class ShadowTransformer implements ViewPager.OnPageChangeListener, ViewPager.PageTransformer {

        private CardPagerAdapter mAdapter;
        private float mLastOffset;

        ShadowTransformer(ViewPager viewPager, CardPagerAdapter adapter) {
            viewPager.addOnPageChangeListener(this);
            mAdapter = adapter;
        }


        @Override
        public void transformPage(View page, float position) { }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int realCurrentPosition;
            int nextPosition;
            float baseElevation = mAdapter.getBaseElevation();
            float realOffset;
            boolean goingLeft = mLastOffset > positionOffset;

            // If we're going backwards, onPageScrolled receives the last position
            // instead of the current one
            if (goingLeft) {
                realCurrentPosition = position + 1;
                nextPosition = position;
                realOffset = 1 - positionOffset;
            } else {
                nextPosition = position + 1;
                realCurrentPosition = position;
                realOffset = positionOffset;
            }

            // Avoid crash on overscroll
            if (nextPosition > mAdapter.getCount() - 1
                    || realCurrentPosition > mAdapter.getCount() - 1) {
                return;
            }

            CardView currentCard = mAdapter.getCardViewAt(realCurrentPosition);

            // This might be null if a fragment is being used
            // and the views weren't created yet
            if (currentCard != null) {
                currentCard.setScaleX((float) (1 + 0.1 * (1 - realOffset)));
                currentCard.setScaleY((float) (1 + 0.1 * (1 - realOffset)));
                currentCard.setCardElevation((baseElevation + baseElevation
                        * (MAX_ELEVATION_FACTOR - 1) * (1 - realOffset)));
            }

            CardView nextCard = mAdapter.getCardViewAt(nextPosition);

            // We might be scrolling fast enough so that the next (or previous) card
            // was already destroyed or a fragment might not have been created yet
            if (nextCard != null) {
                nextCard.setScaleX((float) (1 + 0.1 * (realOffset)));
                nextCard.setScaleY((float) (1 + 0.1 * (realOffset)));
                nextCard.setCardElevation((baseElevation + baseElevation
                        * (MAX_ELEVATION_FACTOR - 1) * (realOffset)));
            }

            mLastOffset = positionOffset;
        }

        @Override
        public void onPageSelected(int position) {  }

        @Override
        public void onPageScrollStateChanged(int state) {   }
    }
}
