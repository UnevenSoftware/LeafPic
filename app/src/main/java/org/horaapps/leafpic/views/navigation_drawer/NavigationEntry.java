package org.horaapps.leafpic.views.navigation_drawer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ui.ThemedIcon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Items to show in the Navigation Drawer
 */
public class NavigationEntry extends LinearLayout {

    @BindView(R.id.navigation_item_icon) ThemedIcon navIcon;
    @BindView(R.id.navigation_item_text) TextView navText;

    public NavigationEntry(@NonNull Context context) {
        this(context, null);
    }

    public NavigationEntry(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public NavigationEntry(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NavigationEntry(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * Sets the icon color for theming.
     *
     * @param colorRes The color integer resource.
     */
    public void setIconColor(@ColorInt int colorRes) {
        navIcon.setColor(colorRes);
    }

    /**
     * Sets the text color for theming.
     *
     * @param colorRes The color integer resource.
     */
    public void setTextColor(@ColorInt int colorRes) {
        navText.setTextColor(colorRes);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        setupView(context);
        LayoutInflater.from(context).inflate(R.layout.view_navigation_entry, this, true);
        ButterKnife.bind(this);

        if (attributeSet == null) return;
        setupData(context, attributeSet);
    }

    private void setupView(@NonNull Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setBackground(ContextCompat.getDrawable(context, R.drawable.ripple));
        setGravity(Gravity.CENTER_VERTICAL);

        int verticalPadding = getResources().getDimensionPixelOffset(R.dimen.nav_entry_vertical_spacing);
        int horizontalPadding = getResources().getDimensionPixelOffset(R.dimen.nav_entry_horizontal_spacing);
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
    }

    private void setupData(@NonNull Context context, @NonNull AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NavigationEntry);
        String displayText = typedArray.getString(R.styleable.NavigationEntry_itemText);
        String displayIcon = typedArray.getString(R.styleable.NavigationEntry_itemIcon);
        typedArray.recycle();

        setText(displayText);

        if (displayIcon == null) return;
        setIcon(displayIcon);
    }

    private void setText(@Nullable String text) {
        navText.setText(text);
    }

    private void setIcon(@NonNull String iconText) {
        navIcon.setIcon(new IconicsDrawable(getContext(), iconText));
    }
}
