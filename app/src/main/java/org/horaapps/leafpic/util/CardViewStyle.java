package org.horaapps.leafpic.util;

/**
 * Created by Jibo on 20/11/2016.
 */
public enum CardViewStyle {
    CARD_MATERIAL(0), CARD_FLAT(1), CARD_COMPACT(2);

    private static final int size = CardViewStyle.values().length;
    int value;

    CardViewStyle(int value) { this.value = value; }

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
