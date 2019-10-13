package org.amupoti.sm.main.service.privateleague;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by amupoti on 12/10/2019.
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerLeagueStateId implements Serializable {

    private int matchNumber;
    private String team;
    private String stat;

}
