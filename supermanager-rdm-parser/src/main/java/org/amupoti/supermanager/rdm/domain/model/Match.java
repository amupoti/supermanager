package org.amupoti.supermanager.rdm.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Match {
    private LeagueTeam againstTeam;
    private boolean local;
}
