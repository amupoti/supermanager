package org.amupoti.supermanager.viewer.application.model;

import lombok.Builder;
import lombok.Getter;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;

@Builder
@Getter
public class ViewerMatch {
    private LeagueTeam againstTeam;
    private boolean local;
}
