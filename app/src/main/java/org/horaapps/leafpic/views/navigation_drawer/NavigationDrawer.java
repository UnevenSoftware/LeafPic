package org.horaapps.leafpic.views.navigation_drawer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;
import butterknife.BindView;
import butterknife.ButterKnife;
import static org.horaapps.leafpic.util.ApplicationUtils.isDebug;

/**
 * Custom view which handles the home's Navigation Drawer.
 */
public class NavigationDrawer extends ScrollView implements Themed {

    public static final int NAVIGATION_ITEM_ALL_ALBUMS = 1001;

    public static final int NAVIGATION_ITEM_ALL_MEDIA = 1002;

    public static final int NAVIGATION_ITEM_HIDDEN_FOLDERS = 1003;

    public static final int NAVIGATION_ITEM_WALLPAPERS = 1004;

    public static final int NAVIGATION_ITEM_DONATE = 1005;

    public static final int NAVIGATION_ITEM_SETTINGS = 1006;

    public static final int NAVIGATION_ITEM_AFFIX = 1007;

    public static final int NAVIGATION_ITEM_ABOUT = 1009;

    public static final int NAVIGATION_ITEM_TIMELINE = 1010;

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        selectedColor = themeHelper.getButtonBackgroundColor();
        selectItem(selectedEntry);
    }

    @IntDef({ NAVIGATION_ITEM_ALL_ALBUMS, NAVIGATION_ITEM_ALL_MEDIA, NAVIGATION_ITEM_HIDDEN_FOLDERS, NAVIGATION_ITEM_WALLPAPERS, NAVIGATION_ITEM_DONATE, NAVIGATION_ITEM_SETTINGS, NAVIGATION_ITEM_AFFIX, NAVIGATION_ITEM_ABOUT, NAVIGATION_ITEM_TIMELINE })
    public @interface NavigationItem {
    }

    @BindView(R.id.navigation_drawer_header)
    ViewGroup drawerHeader;

    @BindView(R.id.navigation_item_albums)
    NavigationEntry albumsEntry;

    @BindView(R.id.navigation_item_all_media)
    NavigationEntry mediaEntry;

    @BindView(R.id.navigation_item_timeline)
    NavigationEntry timelineEntry;

    @BindView(R.id.navigation_item_hidden_albums)
    NavigationEntry hiddenFoldersEntry;

    @BindView(R.id.navigation_item_wallpapers)
    NavigationEntry wallpapersEntry;

    @BindView(R.id.navigation_item_donate)
    NavigationEntry donateEntry;

    @BindView(R.id.navigation_item_settings)
    NavigationEntry settingsEntry;

    @BindView(R.id.navigation_item_affix)
    NavigationEntry affixEntry;

    @BindView(R.id.navigation_item_about)
    NavigationEntry aboutEntry;

    @BindView(R.id.navigation_drawer_header_version)
    TextView appVersion;

    private NavigationEntry[] navigationEntries;

    private ItemListener itemListener;

    private NavigationEntry selectedEntry;

    @ColorInt
    private int selectedColor;

    public NavigationDrawer(@NonNull Context context) {
        this(context, null);
    }

    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NavigationDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Set the listener to be invoked on Navigation Drawer activity.
     */
    public void setListener(@NonNull ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    /**
     * Set the theme for this Navigation Drawer.
     *
     * @param primaryColor    Color for header background
     * @param backgroundColor Color for drawer background
     * @param textColor       Color for item text
     * @param iconColor       Color for icons
     */
    public void setTheme(@ColorInt int primaryColor, @ColorInt int backgroundColor, @ColorInt int textColor, @ColorInt int iconColor) {
        setBackgroundColor(backgroundColor);
        drawerHeader.setBackgroundColor(primaryColor);
        for (NavigationEntry navigationEntry : navigationEntries) {
            navigationEntry.setTextColor(textColor);
            navigationEntry.setIconColor(iconColor);
        }
    }

    /**
     * Set the version to be displayed in Navigation Drawer.
     */
    public void setAppVersion(@NonNull String version) {
        appVersion.setText(version);
    }

    /**
     * Selects the Navigation Item in Nav Drawer.
     *
     * @param navItem The Nav Item to select.
     */
    public void selectNavItem(@NavigationItem int navItem) {
        selectItem(getViewForSelection(navItem));
    }

    /**
     * Called on parent onStart. Use for any kind of refresh activities.
     */
    public void refresh() {
        timelineEntry.setVisibility(isDebug() && Prefs.timelineEnabled() ? VISIBLE : GONE);
    }

    private void init(@NonNull Context context) {
        setupView();
        LayoutInflater.from(context).inflate(R.layout.view_navigation_drawer, this, true);
        ButterKnife.bind(this);
        navigationEntries = new NavigationEntry[] { albumsEntry, mediaEntry, hiddenFoldersEntry, wallpapersEntry, donateEntry, settingsEntry, affixEntry, aboutEntry, timelineEntry };
        setupListeners();
        selectedEntry = albumsEntry;
    }

    private void setupView() {
        int scrollBarSize = getResources().getDimensionPixelOffset(R.dimen.nav_drawer_scrollbar_size);
        setScrollBarSize(scrollBarSize);
    }

    private void setupListeners() {
        OnClickListener onClickListener = v -> {
            if (itemListener == null)
                return;
            itemListener.onItemSelected(getNavigationItemSelected(v.getId()));
        };
        for (NavigationEntry navigationEntry : navigationEntries) {
            navigationEntry.setOnClickListener(onClickListener);
        }
    }

    private void selectItem(@NonNull NavigationEntry v) {
        selectedEntry.setBackground(null);
        selectedEntry = v;
        selectedEntry.setBackgroundColor(selectedColor);
    }

    @NavigationItem
    private int getNavigationItemSelected(@IdRes int viewId) {
        switch(viewId) {
            case R.id.navigation_item_albums:
                return NAVIGATION_ITEM_ALL_ALBUMS;
            case R.id.navigation_item_all_media:
                return NAVIGATION_ITEM_ALL_MEDIA;
            case R.id.navigation_item_timeline:
                return NAVIGATION_ITEM_TIMELINE;
            case R.id.navigation_item_hidden_albums:
                return NAVIGATION_ITEM_HIDDEN_FOLDERS;
            case R.id.navigation_item_wallpapers:
                return NAVIGATION_ITEM_WALLPAPERS;
            case R.id.navigation_item_donate:
                return NAVIGATION_ITEM_DONATE;
            case R.id.navigation_item_settings:
                return NAVIGATION_ITEM_SETTINGS;
            case R.id.navigation_item_affix:
                return NAVIGATION_ITEM_AFFIX;
            case R.id.navigation_item_about:
                return NAVIGATION_ITEM_ABOUT;
        }
        return NAVIGATION_ITEM_ABOUT;
    }

    @NonNull
    private NavigationEntry getViewForSelection(@NavigationItem int navItem) {
        switch(navItem) {
            case NAVIGATION_ITEM_ABOUT:
                return aboutEntry;
            case NAVIGATION_ITEM_ALL_ALBUMS:
                return albumsEntry;
            case NAVIGATION_ITEM_ALL_MEDIA:
                return mediaEntry;
            case NAVIGATION_ITEM_DONATE:
                return donateEntry;
            case NAVIGATION_ITEM_HIDDEN_FOLDERS:
                return hiddenFoldersEntry;
            case NAVIGATION_ITEM_SETTINGS:
                return settingsEntry;
            case NAVIGATION_ITEM_WALLPAPERS:
                return wallpapersEntry;
            case NAVIGATION_ITEM_TIMELINE:
                return timelineEntry;
            default:
                return albumsEntry;
        }
    }

    /**
     * Interface for clients to listen to item selections.
     */
    public interface ItemListener {

        /**
         * Alert the listener that a Navigation Item is select
         *
         * @param navigationItemSelected The Navigation Item selected.
         */
        void onItemSelected(@NavigationItem int navigationItemSelected);
    }
}
