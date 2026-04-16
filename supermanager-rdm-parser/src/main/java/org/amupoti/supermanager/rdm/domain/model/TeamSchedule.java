package org.amupoti.supermanager.rdm.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Aggregate representing all scheduled matches for a specific league team.
 */
@Builder
@Getter
public class TeamSchedule {
    private LeagueTeam team;
    private List<Match> matches;
}
