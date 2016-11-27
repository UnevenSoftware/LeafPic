package org.horaapps.leafpic.util;


import org.horaapps.leafpic.R;

/**
 * Created by Jibo on 20/11/2016.
 */
public enum CardViewStyle {
    CARD_MATERIAL(0, R.layout.card_album_material),
    CARD_FLAT(1, R.layout.card_album_flat),
    CARD_COMPACT(2, R.layout.card_album_compact);

    private static final int size = CardViewStyle.values().length;
    int value;
    int layout;

    CardViewStyle(int value, int layout) {
        this.value = value;
        this.layout = layout;
    }

    public int getLayout() {
        return layout;
    }


    public int getValue() { return value; }

    public static int getSize() {
        return size;
    }

    public static CardViewStyle fromValue(int value){
        switch (value){
            case 0: default: return CARD_MATERIAL;
            case 1: return CARD_FLAT;
            case 2: return CARD_COMPACT;
        }
    }
}
