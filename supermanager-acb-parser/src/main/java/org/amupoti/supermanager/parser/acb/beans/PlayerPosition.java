package org.amupoti.supermanager.parser.acb.beans;

public enum PlayerPosition {
    BASE, ALERO, PIVOT;

    public static PlayerPosition getFromNum(String position) {
        Integer num = Integer.valueOf(position);
        switch (num) {
            case 1:
                return BASE;
            case 3:
                return ALERO;
            case 5:
                return PIVOT;
        }
        return null;
    }
}