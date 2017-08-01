package org.amupoti.supermanager.parser.acb.beans;

public enum PlayerPosition {
    BASE, ALERO, PIVOT;

    public static PlayerPosition getFromRowId(int id) {
        if (id <= 3) return BASE;
        else if (id <= 7) return ALERO;
        else return PIVOT;
    }
}