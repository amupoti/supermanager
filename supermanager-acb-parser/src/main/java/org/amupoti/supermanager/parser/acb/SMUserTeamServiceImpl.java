package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.htmlcleaner.XPatherException;
import org.springframework.http.HttpHeaders;

import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
@Slf4j
public class SMUserTeamServiceImpl implements SMUserTeamService {

    private SmContentProvider smContentProvider;
    private SmContentParser smContentParser;

    public SMUserTeamServiceImpl(SmContentProvider smContentProvider, SmContentParser smContentParser) {
        this.smContentProvider = smContentProvider;
        this.smContentParser = smContentParser;
    }

    @Override
    public List<SmTeam> getTeamsByCredentials(String user, String password) throws XPatherException {

        HttpHeaders httpHeaders = smContentProvider.prepareHeaders();

        smContentProvider.addCookieFromEntryPageToHeaders(httpHeaders);
        String loginPage = smContentProvider.authenticateUser(user, password, httpHeaders);
        smContentParser.checkGameStatus(loginPage);

        String pageBody = smContentProvider.getTeamsPage(httpHeaders);
        List<SmTeam> teams = smContentParser.getTeams(pageBody);
        //no teams returned if game is closed
        for (SmTeam team : teams) {
            String teamPage = smContentProvider.getTeamPage(httpHeaders, team);
            smContentParser.populateTeam(teamPage, team);
            computeTeamStats(team);
        }

        log.debug("Teams found: " + teams);
        return teams;
    }

    private void computeTeamStats(SmTeam team) {
        DoubleSummaryStatistics stats = team.getPlayerList().stream()
                .filter(p -> !p.getScore().equals("-"))
                .mapToDouble(p -> DataUtils.getScoreFromStringValue(p.getScore()))
                .summaryStatistics();
        team.setMeanScorePerPlayer((float) stats.getAverage());
        team.setUsedPlayers((int) stats.getCount());
        team.setComputedScore((float) stats.getSum());
        team.setScorePrediction(computeTeamScorePrediction(stats, team));
    }

    private float computeTeamScorePrediction(DoubleSummaryStatistics stats, SmTeam team) {
        return (float) stats.getAverage() * (team.getPlayerList().size()
                - team.getPlayerList().stream().filter(p -> !p.getStatus().isActive() || p.getStatus().isInjured()).count());
    }

}



