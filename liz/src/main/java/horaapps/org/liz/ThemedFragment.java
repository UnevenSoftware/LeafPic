package horaapps.org.liz;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by dnld on 3/31/17.
 */

public abstract class ThemedFragment extends Fragment implements Themed {

    ThemeHelper themeHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof ThemedActivity)
            themeHelper = ((ThemedActivity) context).getThemeHelper();*/
        themeHelper = ThemeHelper.getInstance(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        themeHelper.updateTheme();
        refreshTheme(themeHelper);
    }

    public ThemeHelper getThemeHelper() {
        return themeHelper;
    }

    public int getPrimaryColor() {
        return themeHelper.getPrimaryColor();
    }

    public int getDialogStyle() {
        return themeHelper.getDialogStyle();
    }

    public int getAccentColor() {
        return themeHelper.getAccentColor();
    }

    public Theme getBaseTheme(){ return  themeHelper.getBaseTheme(); }

    public int getBackgroundColor(){
        return themeHelper.getBackgroundColor();
    }

    public int getCardBackgroundColor(){
        return themeHelper.getCardBackgroundColor();
    }

    public int getIconColor(){
        return themeHelper.getIconColor();
    }

    public int getTextColor(){
        return themeHelper.getTextColor();
    }
}
