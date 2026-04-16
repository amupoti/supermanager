package org.amupoti.supermanager.rdm.application.port.in;

import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;

/**
 * Driving port: retrieve the match schedule for a single team within a match window.
 */
public interface GetTeamScheduleUseCase {
    TeamSchedule getTeamSchedule(LeagueTeam team, int fromMatch, int numMatches);
    int getNextMatch();
}
