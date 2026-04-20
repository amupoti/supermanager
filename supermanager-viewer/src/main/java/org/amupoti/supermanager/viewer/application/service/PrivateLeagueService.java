package org.amupoti.supermanager.viewer.application.service;

import org.amupoti.supermanager.viewer.application.model.PrivateLeagueTeamData;
import org.amupoti.supermanager.viewer.application.port.in.ViewPrivateLeagueUseCase;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateLeagueService implements ViewPrivateLeagueUseCase {

    private final Map<String, PrivateLeagueTeamData> teamsByTeamName = new Hashtable<>();
    private final List<String> teamList;

    public PrivateLeagueService(String teamsConfig) {
        if (teamsConfig == null || teamsConfig.isBlank()) {
            this.teamList = List.of();
        } else {
            this.teamList = Arrays.stream(teamsConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void storePrivateLeagueTeams(Map<String, PrivateLeagueTeamData> teamMap) {
        teamMap.entrySet().stream()
                .filter(entry -> teamList.contains(entry.getKey()))
                .forEach(entry -> teamsByTeamName.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public Map<String, PrivateLeagueTeamData> getPrivateLeagueTeams() {
        return new HashMap<>(teamsByTeamName);
    }
}
