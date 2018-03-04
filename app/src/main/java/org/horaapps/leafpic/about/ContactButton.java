package org.horaapps.leafpic.about;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ui.ThemedTextView;

/**
 * Created by dnld on 04/03/18.
 */

public class ContactButton extends ThemedTextView {

    public ContactButton(Context context) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padd = (int) context.getResources().getDimension(R.dimen.developer_links_small_padding);
        setPadding(padd, padd, padd, padd);
        setTextSize(16);
    }

    public void bold() {
        setTypeface(null, Typeface.BOLD);
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        setTextColor(theme.getAccentColor());
    }

    public void setText(@NonNull String text) {
        super.setText(text.toUpperCase());
    }
}
