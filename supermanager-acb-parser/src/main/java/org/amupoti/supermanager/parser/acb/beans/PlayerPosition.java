package org.amupoti.supermanager.parser.acb.beans;

public enum PlayerPosition {
    BASE("B"), ALERO("A"), PIVOT("P");

    private String name;

    PlayerPosition(String name) {

        this.name = name;
    }

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

    public static int getFromName(String name) {
        for (PlayerPosition position : values()) {
            if (position.name.equals(name)) {
                switch (position) {
                    case BASE:
                        return 1;
                    case ALERO:
                        return 3;
                    case PIVOT:
                        return 5;
                }
            }
        }

        return 0;
    }

    public String getName() {
        return name;
    }
}