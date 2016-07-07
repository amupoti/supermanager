package org.amupoti.sm.main.bean;

import lombok.Getter;

/**
 * Created by Marcel on 14/08/2015.
 */
@Getter
public enum PlayerPosition {
    BASE("B",0),ALERO("A",1),PIVOT("P",2),TOTAL("T",3);

    private final String id;
    private int number;


    PlayerPosition(String id,
    int number)

    {
        this.id = id;
        this.number = number;
    }
}