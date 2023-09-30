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

    private List<String> teamList = List.of(
            "Rianxeira",
            "EL PAS ZERO",
            "Xeiks",
            "Armónicos",
            "Armónicos",
            "EL POLLO HEMBRA",
            "Laso Vuelve",
            "Gañan secreto",
            "Drazem Team",
            "Bird Team",
            "Averiaos",
            "Drazem Team",
            "CB RAJADELL BANC",
            "Alta Segarra Negre",
            "Boí 10",
            "Circulo de quintas",
            "Mèdol 0",
            "Mateo Vete",
            "Muiñeira",
            "Roll for the gañan",
            "Xav Solo",
            "Xavi one Kenobi",
            "Faemino",
            "Cansado",
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
