package org.amupoti.supermanager.acb.application.port.out;

import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Team;

import java.io.IOException;
import java.util.List;

/**
 * Driven port: retrieve and populate team data from the ACB API.
 */
public interface TeamDataPort {
    List<Team> getTeams(String user, String token) throws IOException;
    void populateTeam(Team team, MarketData marketData, String token) throws IOException;
    void mergePlayerChangeIds(Team team, String token);
}
