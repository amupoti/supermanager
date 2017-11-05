package org.amupoti.supermanager.parser.acb.teams;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.htmlcleaner.XPatherException;

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
    synchronized public List<SmTeam> getTeamsByCredentials(String user, String password) throws XPatherException {

        String loginPage = smContentProvider.authenticateUser(user, password);
        smContentParser.checkGameStatus(loginPage);

        String pageBody = smContentProvider.getTeamsPage();
        List<SmTeam> teams = smContentParser.getTeams(pageBody);

        String marketPage = smContentProvider.getMarketPage();
        PlayerMarketData playerMarketData = smContentParser.providePlayerData(marketPage);

        for (SmTeam team : teams) {
            String teamPage = smContentProvider.getTeamPage(team);
            smContentParser.populateTeam(teamPage, team, playerMarketData);
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
        team.setMeanScorePerPlayer(round((float) stats.getAverage()));
        team.setUsedPlayers((int) stats.getCount());
        team.setComputedScore(round((float) stats.getSum()));
        team.setScorePrediction(round(computeTeamScorePrediction(stats, team)));
    }

    private float computeTeamScorePrediction(DoubleSummaryStatistics stats, SmTeam team) {
        return (float) stats.getAverage() * (team.getPlayerList().size()
                - team.getPlayerList().stream().filter(p -> !p.getStatus().isActive() || p.getStatus().isInjured()).count());
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }

}



