package org.amupoti.sm.main.model;

import lombok.Builder;
import lombok.Getter;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;

/**
 * Created by amupoti on 05/11/2018.
 */
@Builder
@Getter
public class ViewerMatch {

    private LeagueTeam againstTeam;
    private boolean local;
}
