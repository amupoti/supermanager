package org.amupoti.supermanager.acb.application.service;

import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.acb.utils.DataUtils;

import java.util.DoubleSummaryStatistics;

/**
 * Computes aggregate statistics for a team (mean score, broker values, score prediction).
 */
public class ComputeTeamStatsService {

    public void computeTeamStats(Team team) {
        DoubleSummaryStatistics stats = team.getPlayerList().stream()
                .filter(p -> p.getScore() != null && !p.getScore().equals("-"))
                .mapToDouble(p -> DataUtils.getScoreFromStringValue(p.getScore()))
                .summaryStatistics();
        team.setMeanScorePerPlayer(round((float) stats.getAverage()));
        team.setUsedPlayers((int) stats.getCount());
        team.setComputedScore(round((float) stats.getSum()));
        team.setScorePrediction(round(computeScorePrediction(stats, team)));
        int brokerSum = team.getPlayerList().stream()
                .filter(p -> p.getMarketData() != null)
                .map(p -> p.getMarketData().get(MarketCategory.PRICE.name()))
                .map(Float::parseFloat)
                .mapToInt(Float::intValue)
                .sum();
        team.setTeamBroker(brokerSum);
        team.setTotalBroker(team.getCash() + brokerSum);
    }

    private float computeScorePrediction(DoubleSummaryStatistics stats, Team team) {
        return (float) stats.getAverage() * (team.getPlayerList().size()
                - team.getPlayerList().stream()
                .filter(p -> !p.getStatus().isActive() || p.getStatus().isInjured()).count());
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }
}
