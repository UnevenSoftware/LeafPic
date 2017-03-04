package org.horaapps.leafpic.util;

import android.app.Activity;
import android.content.res.Resources;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by darken (darken@darken.eu) on 04.03.2017.
 */
public class ViewUtil {
    // http://stackoverflow.com/questions/18668897/android-get-all-children-elements-of-a-viewgroup
    public static List<View> getAllChildren(View target) {
        if (!(target instanceof ViewGroup)) return Collections.singletonList(target);

        ArrayList<View> allChildren = new ArrayList<>();
        ViewGroup viewGroup = (ViewGroup) target;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            ArrayList<View> targetsChildren = new ArrayList<>();
            targetsChildren.add(target);
            targetsChildren.addAll(getAllChildren(child));
            allChildren.addAll(targetsChildren);
        }
        return allChildren;
    }

    public static boolean hasNavBar(Activity activity) {
        Resources resources = activity.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return resources.getBoolean(id);
        } else {    // Check for keys
            boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !hasMenuKey && !hasBackKey;
        }
    }
}
