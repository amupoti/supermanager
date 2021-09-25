package org.amupoti.supermanager.parser.acb.teams;

import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.beans.market.MarketCategory;
import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;
import org.amupoti.supermanager.parser.acb.dto.LoginResponse;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
public class SMUserTeamService {

    private final static Log log = LogFactory.getLog(SMUserTeamService.class);

    private SmContentProvider smContentProvider;
    private SmContentParser smContentParser;

    public SMUserTeamService(SmContentProvider smContentProvider, SmContentParser smContentParser) {
        this.smContentProvider = smContentProvider;
        this.smContentParser = smContentParser;
    }

    synchronized public List<SmTeam> getTeamsByCredentials(String user, String password) throws IOException {

        LoginResponse loginResponse = smContentProvider.authenticateUser(user, password);

        String token = loginResponse.getJwt();
        String response = smContentProvider.getTeamsPage(user, token);
        List<SmTeam> teams = smContentParser.getTeams(response);

        String marketPage = smContentProvider.getMarketPage(token);
        PlayerMarketData playerMarketData = smContentParser.providePlayerData(marketPage);

        for (SmTeam team : teams) {
            String teamPage = smContentProvider.getTeamPage(team, token);
            smContentParser.populateTeam(teamPage, team, playerMarketData);
            computeTeamStats(team);
        }

        log.debug("Teams found: " + teams);
        return teams;
    }

    private void computeTeamStats(SmTeam team) {
        DoubleSummaryStatistics stats = team.getPlayerList().stream()
                .filter(p -> p.getScore() != null && !p.getScore().equals("-"))
                .mapToDouble(p -> DataUtils.getScoreFromStringValue(p.getScore()))
                .summaryStatistics();
        team.setMeanScorePerPlayer(round((float) stats.getAverage()));
        team.setUsedPlayers((int) stats.getCount());
        team.setComputedScore(round((float) stats.getSum()));
        team.setScorePrediction(round(computeTeamScorePrediction(stats, team)));
        int brokerSum = getBrokerSum(team);
        team.setTeamBroker(brokerSum);
        team.setTotalBroker(team.getCash() + brokerSum);
    }

    private int getBrokerSum(SmTeam team) {
        return team.getPlayerList().stream()
                .filter(p -> p.getMarketData() != null)
                .map(p -> p.getMarketData().get(MarketCategory.PRICE.name()))
                .mapToInt(DataUtils::toPriceValue).sum();
    }

    private float computeTeamScorePrediction(DoubleSummaryStatistics stats, SmTeam team) {
        return (float) stats.getAverage() * (team.getPlayerList().size()
                - team.getPlayerList().stream().filter(p -> !p.getStatus().isActive() || p.getStatus().isInjured()).count());
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }

}



