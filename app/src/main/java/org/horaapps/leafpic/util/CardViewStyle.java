package org.horaapps.leafpic.util;

/**
 * Created by Jibo on 20/11/2016.
 */
public enum CardViewStyle {
    CARD_MATERIAL(0), CARD_FLAT(1), CARD_COMPACT(2);
    int value;

    CardViewStyle(int value) { this.value = value; }
    public int getValue() { return value; }

    public static CardViewStyle fromValue(int value){
        switch (value){
            case 0: default: return CARD_MATERIAL;
            case 1: return CARD_FLAT;
            case 2: return CARD_COMPACT;
        }
    }
}
