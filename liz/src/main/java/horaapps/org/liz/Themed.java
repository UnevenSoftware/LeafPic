package horaapps.org.liz;

/**
 * Created by darken (darken@darken.eu) on 04.03.2017.
 * Views that can react to theme changes should implement this.
 */
public interface Themed {
    void refreshTheme(ThemeHelper themeHelper);
}
