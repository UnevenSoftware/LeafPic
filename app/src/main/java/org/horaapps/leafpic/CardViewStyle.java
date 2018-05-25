package org.horaapps.leafpic;


/**
 * Created by Jibo on 20/11/2016.
 */
public enum CardViewStyle {

    MATERIAL(0, R.layout.card_album_material),
    FLAT(1, R.layout.card_album_flat),
    COMPACT(2, R.layout.card_album_compact);

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
            case 0: default: return MATERIAL;
            case 1: return FLAT;
            case 2: return COMPACT;
        }
    }
}
