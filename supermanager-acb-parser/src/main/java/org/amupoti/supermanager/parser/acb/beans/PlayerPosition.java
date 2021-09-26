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

    public static int getFromName(String position) {
        switch (PlayerPosition.valueOf(position)) {
            case BASE:
                return 1;
            case ALERO:
                return 3;
            case PIVOT:
                return 5;
        }

        return 0;
    }
}