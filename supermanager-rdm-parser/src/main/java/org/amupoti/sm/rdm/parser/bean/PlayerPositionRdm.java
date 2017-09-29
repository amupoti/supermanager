package org.amupoti.sm.rdm.parser.bean;

import lombok.Getter;

/**
 * Created by Marcel on 14/08/2015.
 */
@Getter
public enum PlayerPositionRdm {
    BASE("B", 0), ALERO("A", 1), PIVOT("P", 2), TOTAL("T", 3);

    private final String id;
    private int number;


    PlayerPositionRdm(String id,
                      int number)

    {
        this.id = id;
        this.number = number;
    }
}
