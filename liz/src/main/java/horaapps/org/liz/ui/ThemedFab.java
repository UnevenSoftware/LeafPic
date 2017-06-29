package horaapps.org.liz.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import horaapps.org.liz.R;
import horaapps.org.liz.ThemeHelper;
import horaapps.org.liz.Themed;


/**
 * Created by dnld on 04.03.2017.
 */
public class ThemedFab extends FloatingActionButton implements Themed {

    private IconicsDrawable mIcon = null;

    public ThemedFab(Context context) {
        this(context, null);
    }

    public ThemedFab(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemedFab(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ThemedFab);
        setIcon(a.getString(R.styleable.ThemedFab_fab_icon));
        a.recycle();
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        setBackgroundTintList(ColorStateList.valueOf(theme.getAccentColor()));
        setImageDrawable(mIcon);
    }

    public void setIcon(IIcon icon) {
        mIcon = new IconicsDrawable(getContext()).icon(icon).color(Color.WHITE);
    }

    public void setIcon(String icon) {
        mIcon = new IconicsDrawable(getContext()).icon(icon).color(Color.WHITE);

    }
}
