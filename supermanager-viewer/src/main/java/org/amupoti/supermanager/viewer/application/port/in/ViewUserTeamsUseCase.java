package org.amupoti.supermanager.viewer.application.port.in;

import org.amupoti.supermanager.viewer.application.model.PrivateLeagueTeamData;

import java.io.IOException;
import java.util.Map;

public interface ViewUserTeamsUseCase {
    Map<String, PrivateLeagueTeamData> loadUserTeamView(String login, String password) throws IOException;
}
