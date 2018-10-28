package org.amupoti.supermanager.parser.rdm;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Created by amupoti on 27/10/2018.
 */
@Builder
@Getter
public class RdmTeamData {

    private RdmTeam team;
    private List<Match> matches;
}
