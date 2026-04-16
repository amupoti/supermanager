package org.amupoti.sm.main.service;

import org.amupoti.sm.main.model.ViewerMatch;
import org.amupoti.sm.main.model.ViewerPlayer;
import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by amupoti on 06/11/2018.
 */
@Service
public class RdmSmTeamService {

    @Value("${scraping.next-matches:5}")
    private int nextMatches;

    private static final String TEAM_RDM = "TEAM_RDM";

    @Autowired
    private RdmMatchService matchService;

    public List<ViewerPlayer> buildPlayerList(List<Player> playerList) {

        final int nextMatch = matchService.getNextMatch();
        List<ViewerPlayer> collect = playerList.stream()

                //TODO: get Market data
                .filter(p -> p.getMarketData() != null)
                .map(this::toRdmTeam)
                .map(playerAndTeam -> toRdmTeamData(playerAndTeam, nextMatch))
                .map(this::toViewerPlayer)
                .collect(toList());

        return collect;
    }

    private ViewerPlayer toViewerPlayer(Pair<Player, TeamSchedule> pair) {
        List<ViewerMatch> matches = pair.getValue().getMatches().stream()
                .map(this::buildViewerMatch)
                .collect(toList());

        return ViewerPlayer.builder()
                .player(pair.getKey())
                .matches(matches)
                .build();
    }

    private ViewerMatch buildViewerMatch(Match m) {
        return ViewerMatch.builder()
                .againstTeam(m.getAgainstTeam()).local(m.isLocal()).build();
    }

    private Pair<Player, TeamSchedule> toRdmTeamData(Pair<Player, LeagueTeam> pair, int nextMatch) {
        TeamSchedule teamDataFromMatchNumber = matchService.getTeamDataFromMatchNumber(pair.getValue(), nextMatch, nextMatches);
        return Pair.of(pair.getKey(), teamDataFromMatchNumber);
    }

    private Pair<Player, LeagueTeam> toRdmTeam(Player p) {
        String smTeamName = p.getMarketData().get(MarketCategory.TEAM.name());
        LeagueTeam rdmTeam = LeagueTeam.fromTeamName(smTeamName);
        p.getMarketData().put(TEAM_RDM, rdmTeam.name());
        return Pair.of(p, rdmTeam);
    }
}
