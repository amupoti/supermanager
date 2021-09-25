package org.amupoti.sm.main.service;

import org.amupoti.sm.main.model.ViewerMatch;
import org.amupoti.sm.main.model.ViewerPlayer;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;
import org.amupoti.supermanager.parser.acb.beans.market.MarketCategory;
import org.amupoti.supermanager.parser.rdm.Match;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
import org.amupoti.supermanager.parser.rdm.RdmTeam;
import org.amupoti.supermanager.parser.rdm.RdmTeamData;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by amupoti on 06/11/2018.
 */
@Service
public class RdmSmTeamService {

    public static final int NEXT_MATCHES = 5;
    private static final String TEAM_RDM = "TEAM_RDM";

    @Autowired
    private RdmMatchService matchService;

    public List<ViewerPlayer> buildPlayerList(List<SmPlayer> playerList) {

        final int nextMatch = matchService.getNextMatch();
        List<ViewerPlayer> collect = playerList.stream()
                .map(this::toSimpleViewerPlayer)
                //TODO: get Market data
//                .filter(p -> p.getMarketData() != null)
//                .map(this::toRdmTeam)
//                .map(playerAndTeam -> toRdmTeamData(playerAndTeam, nextMatch))
//                .map(this::toViewerPlayer)
                .collect(toList());

        return collect;
    }

    private ViewerPlayer toSimpleViewerPlayer(SmPlayer smPlayer) {
        return ViewerPlayer.builder()
                .player(smPlayer)
                .build();
    }


    private ViewerPlayer toViewerPlayer(Pair<SmPlayer, RdmTeamData> pair) {
        List<ViewerMatch> matches = pair.getValue().getMatches().stream()
                .map(m -> buildViewerMatch(m))
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

    private Pair<SmPlayer, RdmTeamData> toRdmTeamData(Pair<SmPlayer, RdmTeam> pair, int nextMatch) {
        RdmTeamData teamDataFromMatchNumber = matchService.getTeamDataFromMatchNumber(pair.getValue(), nextMatch, NEXT_MATCHES);
        Pair<SmPlayer, RdmTeamData> pairData = Pair.of(pair.getKey(), teamDataFromMatchNumber);
        return pairData;
    }

    private Pair<SmPlayer, RdmTeam> toRdmTeam(SmPlayer p) {
        String smTeamName = p.getMarketData().get(MarketCategory.TEAM.name());
        RdmTeam rdmTeam = RdmTeam.fromTeamName(smTeamName);
        p.getMarketData().put(TEAM_RDM, rdmTeam.name());
        return Pair.of(p, rdmTeam);

    }
}
