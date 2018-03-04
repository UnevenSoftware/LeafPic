package org.horaapps.leafpic.about;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;
import org.horaapps.liz.ui.ThemedIcon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Links to show in the About screen.
 */
public class AboutLink extends RelativeLayout implements Themed {

    @BindView(R.id.about_link_icon) ThemedIcon linkIcon;
    @BindView(R.id.about_link_title) TextView linkTitle;
    @BindView(R.id.about_link_description) TextView linkDescription;

    public AboutLink(@NonNull Context context) {
        this(context, null);
    }

    public AboutLink(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AboutLink(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AboutLink(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        setupView(context);
        LayoutInflater.from(context).inflate(R.layout.view_about_link, this, true);
        ButterKnife.bind(this);

        if (attributeSet == null) return;
        setupData(context, attributeSet);
    }

    private void setupView(@NonNull Context context) {
        setBackground(ContextCompat.getDrawable(context, R.drawable.ripple));
        setGravity(Gravity.CENTER_VERTICAL);

        int padding = getResources().getDimensionPixelOffset(R.dimen.about_link_padding);
        setPadding(padding, padding, padding, padding);
    }

    private void setupData(@NonNull Context context, @NonNull AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.AboutLink);
        String titleText = typedArray.getString(R.styleable.AboutLink_linkTitle);
        String linkIcon = typedArray.getString(R.styleable.AboutLink_linkIcon);
        String linkDescription = typedArray.getString(R.styleable.AboutLink_linkDescription);
        typedArray.recycle();

        setText(titleText);
        setDescription(linkDescription);

        if (linkIcon == null) return;
        setIcon(linkIcon);
    }

    private void setText(@Nullable String text) {
        linkTitle.setText(text);
    }

    /**
     * Set the description subtext for this link.
     */
    public void setDescription(@Nullable String text) {
        linkDescription.setText(text);
    }

    private void setIcon(@NonNull String iconText) {
        linkIcon.setIcon(new IconicsDrawable(getContext(), iconText));
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        int textColor = themeHelper.getTextColor();
        int subTextColor = themeHelper.getSubTextColor();
        int iconColor = themeHelper.getIconColor();

        linkIcon.setColor(iconColor);
        linkTitle.setTextColor(textColor);
        linkDescription.setTextColor(subTextColor);
    }
}
