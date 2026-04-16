package org.amupoti.supermanager.acb.application.port.in;

import org.amupoti.supermanager.acb.domain.model.Team;

import java.io.IOException;
import java.util.List;

/**
 * Driving port: load and enrich all teams for the given user credentials.
 */
public interface LoadUserTeamsUseCase {
    List<Team> loadTeams(String user, String password) throws IOException;
}
