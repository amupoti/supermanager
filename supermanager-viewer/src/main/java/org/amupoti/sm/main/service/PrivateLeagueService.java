package org.amupoti.sm.main.service;

import org.amupoti.sm.main.model.PrivateLeagueTeamData;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Service
public class PrivateLeagueService {

    private Map<String, PrivateLeagueTeamData> teamsByTeamName = new Hashtable<>();

    private List<String> teamList = List.of("CSKA Larropa",
            "FAmme Rouge",
            "Tararí De Telaví",
            "La casa del Gañán",
            "Margall",
            "Dinoseto",
            "Team Hardaway",
            "Capitán Cachamuiña",
            "Bird Team",
            "Averiaos",
            "Xeiks",
            "Drazem Team",
            "Gañanes del poder",
            "Escamot 1",
            "Team Duncan",
            "Cb Pujalt",
            "Acercandro Sanz",
            "FAverna",
            "Villacampa",
            "CB Rajadell",
            "Summa Teológica",
            "Delta 2");

    public void storePrivateLeagueTeams(Map<String, PrivateLeagueTeamData> teamMap) {

        teamMap.entrySet().stream()
                .filter(entry -> teamList.contains(entry.getKey()))
                .forEach(entry ->
                        teamsByTeamName.put(entry.getKey(), entry.getValue()));
        //TODO: add updatedAt

    }

    public Map<String, PrivateLeagueTeamData> getPrivateLeagueTeams() {
        return new HashMap<>(teamsByTeamName);
    }
}
