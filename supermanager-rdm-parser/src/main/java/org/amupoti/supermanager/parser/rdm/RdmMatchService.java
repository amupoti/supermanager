package org.amupoti.supermanager.parser.rdm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by amupoti on 28/10/2018.
 */
@Service
public class RdmMatchService {

    @Autowired
    private RdmContentParser parser;

    @Autowired
    private RdmContentProvider provider;

    @Cacheable("TeamData")
    public List<RdmTeamData> getTeamsData() {
        return Arrays.stream(RdmTeam.values()).map(this::getTeamData).collect(Collectors.toList());
    }

    @Cacheable("NextMatch")
    public int getNextMatch() {
        return getCurrentMatchFromIdealTeamWidget() + 1;
    }

    private int getCurrentMatchFromIdealTeamWidget() {
        String page = provider.getMainPage();
        String matchNumber = parser.getMatchNumber(page);
        return Integer.parseInt(matchNumber);
    }

    private RdmTeamData getTeamData(RdmTeam team) {
        String teamPage = provider.getTeamPage(team);
        List<Match> teamMatches = parser.getTeamMatches(teamPage, team);
        return RdmTeamData.builder()
                .matches(teamMatches)
                .team(team)
                .build();

    }
}
