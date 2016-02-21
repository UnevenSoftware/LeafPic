package com.leafpic.app;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Jibo on 21/02/2016.
 */
public class PrefUtils {
    public static final String OPEN_EXCLUDED_FOLDERS = "excluded_folders";

    public static final String DARK_THEME = "dark_theme";
    public static final String EXPLORER_MODE = "explorer_mode";
    public static final String COLORED_NAVBAR = "colored_navbar";
    public static final String GRID_MODE = "grid_mode";
    public static final String GRID_SIZE_PREFIX = "grid_size_";
    public static final String OVERVIEW_MODE = "overview_mode";
    public static final String FILTER_MODE = "filter_mode";
    public static final String INCLUDE_SUBFOLDERS = "include_subfolders";
    public static final String CURRENT_ACCOUNT_ID = "current_account";
    public static final String SORT_MODE = "sort_mode";
    public static final String PRIMARY_COLOR_PREFIX = "primary_color";
    public static final String ACCENT_COLOR_PREFIX = "accent_color";
    public static final String EXCLUDE_SUBFOLDERS = "exclude_subfolders";

    public static boolean isDarkTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DARK_THEME, false);
    }

    public static boolean isColoredNavBar(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(COLORED_NAVBAR, false);
    }

}
