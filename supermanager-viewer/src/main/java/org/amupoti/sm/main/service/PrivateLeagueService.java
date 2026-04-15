package org.amupoti.sm.main.service;

import org.amupoti.sm.main.model.PrivateLeagueTeamData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PrivateLeagueService {

    private final Map<String, PrivateLeagueTeamData> teamsByTeamName = new Hashtable<>();

    @Value("${private.league.teams}")
    private String teamsConfig;

    private List<String> teamList;

    @PostConstruct
    public void init() {
        teamList = Arrays.stream(teamsConfig.split(","))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    public void storePrivateLeagueTeams(Map<String, PrivateLeagueTeamData> teamMap) {
        teamMap.entrySet().stream()
                .filter(entry -> teamList.contains(entry.getKey()))
                .forEach(entry -> teamsByTeamName.put(entry.getKey(), entry.getValue()));
    }

    public Map<String, PrivateLeagueTeamData> getPrivateLeagueTeams() {
        return new HashMap<>(teamsByTeamName);
    }
}
