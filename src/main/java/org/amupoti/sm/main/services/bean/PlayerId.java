package org.amupoti.sm.main.services.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Marcel on 04/08/2015.
 */
@AllArgsConstructor
@Data
public class PlayerId {

    private String id;

    @Override
    public String toString() {
        return id;
    }
}
