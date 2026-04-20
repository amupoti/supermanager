package org.amupoti.supermanager.viewer.application.port.in;

import org.amupoti.supermanager.viewer.application.model.PrivateLeagueTeamData;

import java.util.Map;

public interface ViewPrivateLeagueUseCase {
    void storePrivateLeagueTeams(Map<String, PrivateLeagueTeamData> teamMap);
    Map<String, PrivateLeagueTeamData> getPrivateLeagueTeams();
}
