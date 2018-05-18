package org.horaapps.leafpic.util.preferences;

import android.content.Context;
import android.support.annotation.NonNull;

import org.horaapps.leafpic.CardViewStyle;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;

/**
 * Class for storing & retrieving application data.
 * <p>
 * Abstracts clients from from DB / SharedPreferences / Hawk.
 */
public class Prefs {

    private static SharedPrefs sharedPrefs;

    /**
     * Initialise the Prefs object for future static usage.
     * Make sure to initialise this in Application class.
     *
     * @param context The context to initialise with.
     */
    public static void init(@NonNull Context context) {
        if (sharedPrefs != null) {
            throw new RuntimeException("Prefs has already been instantiated");
        }
        sharedPrefs = new SharedPrefs(context);
    }

    /********** GETTERS **********/

    /**
     * Get number of folder columns to display in Portrait orientation
     */
    public static int getFolderColumnsPortrait() {
        return getPrefs().get(Keys.FOLDER_COLUMNS_PORTRAIT, Defaults.FOLDER_COLUMNS_PORTRAIT);
    }

    /**
     * Get number of folder columns to display in Landscape orientation
     */
    public static int getFolderColumnsLandscape() {
        return getPrefs().get(Keys.FOLDER_COLUMNS_LANDSCAPE, Defaults.FOLDER_COLUMNS_LANDSCAPE);
    }

    /**
     * Get number of media columns to display in Portrait orientation
     */
    public static int getMediaColumnsPortrait() {
        return getPrefs().get(Keys.MEDIA_COLUMNS_PORTRAIT, Defaults.MEDIA_COLUMNS_PORTRAIT);
    }

    /**
     * Get number of media columns to display in Landscape orientation
     */
    public static int getMediaColumnsLandscape() {
        return getPrefs().get(Keys.MEDIA_COLUMNS_LANDSCAPE, Defaults.MEDIA_COLUMNS_LANDSCAPE);
    }

    /**
     * Get the sorting mode (Name / Date / Size / Type / Numeric) for albums.
     */
    @NonNull
    public static SortingMode getAlbumSortingMode() {
        return SortingMode.fromValue(
                getPrefs().get(Keys.ALBUM_SORTING_MODE, Defaults.ALBUM_SORTING_MODE));
    }

    /**
     * Get the sorting order (Ascending / Descending) for albums.
     */
    @NonNull
    public static SortingOrder getAlbumSortingOrder() {
        return SortingOrder.fromValue(
                getPrefs().get(Keys.ALBUM_SORTING_ORDER, Defaults.ALBUM_SORTING_ORDER));
    }

    /**
     * Should display video files in media.
     */
    public static boolean showVideos() {
        return getPrefs().get(Keys.SHOW_VIDEOS, Defaults.SHOW_VIDEOS);
    }

    /**
     * Should display count of media.
     */
    public static boolean showMediaCount() {
        return getPrefs().get(Keys.SHOW_MEDIA_COUNT, Defaults.SHOW_MEDIA_COUNT);
    }

    /**
     * Should display path of albums.
     */
    public static boolean showAlbumPath() {
        return getPrefs().get(Keys.SHOW_ALBUM_PATH, Defaults.SHOW_ALBUM_PATH);
    }

    /**
     * Should show the Emoji Easter Egg.
     */
    public static boolean showEasterEgg() {
        return getPrefs().get(Keys.SHOW_EASTER_EGG, Defaults.SHOW_EASTER_EGG);
    }

    /**
     * Should use animations
     */
    public static boolean animationsEnabled() {
        return !getPrefs().get(Keys.ANIMATIONS_DISABLED, Defaults.ANIMATIONS_DISABLED);
    }

    /**
     * Whether the Timeline view is enabled.
     */
    public static boolean timelineEnabled() {
        return getPrefs().get(Keys.TIMELINE_ENABLED, Defaults.TIMELINE_ENABLED);
    }

    /**
     * Get the Card Style (Material / Flat / Compact)
     */
    @NonNull
    public static CardViewStyle getCardStyle() {
        return CardViewStyle.fromValue(
                getPrefs().get(Keys.CARD_STYLE, Defaults.CARD_STYLE));
    }

    public static int getLastVersionCode() {
        return getPrefs().get(Keys.LAST_VERSION_CODE, Defaults.LAST_VERSION_CODE);
    }

    /********** SETTERS **********/

    /**
     * Set the number of folder columns in Portrait orientation.
     */
    public static void setFolderColumnsPortrait(int value) {
        getPrefs().put(Keys.FOLDER_COLUMNS_PORTRAIT, value);
    }

    /**
     * Set the number of folder columns in Landscape orientation.
     */
    public static void setFolderColumnsLandscape(int value) {
        getPrefs().put(Keys.FOLDER_COLUMNS_LANDSCAPE, value);
    }

    /**
     * Set the number of media columns in Portrait orientation.
     */
    public static void setMediaColumnsPortrait(int value) {
        getPrefs().put(Keys.MEDIA_COLUMNS_PORTRAIT, value);
    }

    /**
     * Set the number of media columns in Landscape orientation.
     */
    public static void setMediaColumnsLandscape(int value) {
        getPrefs().put(Keys.MEDIA_COLUMNS_LANDSCAPE, value);
    }

    /**
     * Set the Sorting Mode for albums (Name / Size / etc)
     */
    public static void setAlbumSortingMode(@NonNull SortingMode sortingMode) {
        getPrefs().put(Keys.ALBUM_SORTING_MODE, sortingMode.getValue());
    }

    /**
     * Set the Sorting Order for albums (Ascending / Descending)
     */
    public static void setAlbumSortingOrder(@NonNull SortingOrder sortingOrder) {
        getPrefs().put(Keys.ALBUM_SORTING_ORDER, sortingOrder.getValue());
    }

    /**
     * Set show video files in media collection.
     */
    public static void setShowVideos(boolean value) {
        getPrefs().put(Keys.SHOW_VIDEOS, value);
    }

    /**
     * Set show the media count.
     */
    public static void setShowMediaCount(boolean value) {
        getPrefs().put(Keys.SHOW_MEDIA_COUNT, value);
    }

    /**
     * Set show the full album path.
     */
    public static void setShowAlbumPath(boolean value) {
        getPrefs().put(Keys.SHOW_ALBUM_PATH, value);
    }

    /**
     * Set the Card Style (Material / Flat / Compact)
     */
    public static void setCardStyle(@NonNull CardViewStyle cardStyle) {
        getPrefs().put(Keys.CARD_STYLE, cardStyle.getValue());
    }

    /**
     * Set the last version code of Application.
     */
    public static void setLastVersionCode(int value) {
        getPrefs().put(Keys.LAST_VERSION_CODE, value);
    }

    /**
     * Set show the Emoji Easter Egg.
     */
    public static void setShowEasterEgg(boolean value) {
        getPrefs().put(Keys.SHOW_EASTER_EGG, value);
    }

    // ***** TODO Calvin: These methods does not belong here, DO NOT expose generic methods to clients

    @Deprecated
    public static void setToggleValue(@NonNull String key, boolean value) {
        getPrefs().put(key, value);
    }

    @Deprecated
    public static boolean getToggleValue(@NonNull String key, boolean defaultValue) {
        return getPrefs().get(key, defaultValue);
    }

    // ***** Remove these methods when SettingWithSwitchView is refactored.

    @NonNull
    private static SharedPrefs getPrefs() {
        if (sharedPrefs == null) {
            throw new RuntimeException("Prefs has not been instantiated. Call init() with context");
        }
        return sharedPrefs;
    }
}
