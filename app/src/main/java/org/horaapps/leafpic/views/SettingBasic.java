package org.horaapps.leafpic.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;
import org.horaapps.liz.ui.ThemedIcon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by darken (darken@darken.eu) on 04.03.2017.
 */
public class SettingBasic extends FrameLayout implements Themed {
    private final String iconString;
    @StringRes private final int titleRes;
    @StringRes private final int captionRes;
    @BindView(R.id.icon)
    ThemedIcon icon;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.caption) TextView caption;

    public SettingBasic(Context context) {
        this(context, null);
    }

    public SettingBasic(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingBasic(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundResource(R.drawable.ripple);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_setting_basic, this);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SettingBasic);
        iconString = a.getString(R.styleable.SettingBasic_settingIcon);
        titleRes = a.getResourceId(R.styleable.SettingBasic_settingTitle, 0);
        captionRes = a.getResourceId(R.styleable.SettingBasic_settingCaption, 0);
        int minimumApi = a.getInteger(R.styleable.SettingBasic_settingMinApi, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT < minimumApi) setVisibility(GONE);
    }

    @Override
    protected void onFinishInflate() {
        ButterKnife.bind(this);

        icon.setIcon(icon.getIcon().icon(iconString));
        title.setText(titleRes);
        caption.setText(captionRes);

        /*
        setPadding((int) getResources().getDimension(R.dimen.medium_spacing), 0, (int) getResources().getDimension(R.dimen.medium_spacing), 0);
        setMinimumHeight((int) getResources().getDimension(R.dimen.listitem_height_twoline));
        */
        super.onFinishInflate();
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {

    }

}
