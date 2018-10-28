package org.amupoti.supermanager.parser.rdm;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by amupoti on 28/10/2018.
 */
@Builder
@Getter
@ToString
public class Match {

    private RdmTeam homeTeam;
    private RdmTeam awayTeam;
    private String homeScore;
    private String awayScore;


}
