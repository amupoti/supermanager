package org.amupoti.sm.rdm.parser.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Marcel on 04/08/2015.
 */
@AllArgsConstructor
@Data
public class PlayerId implements Serializable {

    private String id;

    @Override
    public String toString() {
        return id;
    }
}
