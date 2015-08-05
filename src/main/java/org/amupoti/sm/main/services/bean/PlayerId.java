package org.amupoti.sm.main.services.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Marcel on 04/08/2015.
 */
@Getter
@Setter
@AllArgsConstructor

public class PlayerId {

    private String id;

    @Override
    public String toString() {
        return id;
    }
}
