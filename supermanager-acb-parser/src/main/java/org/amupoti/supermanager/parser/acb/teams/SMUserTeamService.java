package org.amupoti.supermanager.parser.acb.teams;

import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.beans.market.MarketCategory;
import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;
import org.amupoti.supermanager.parser.acb.dto.LoginResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Marcel on 02/01/2016.
 */
public class SMUserTeamService {

    private static final int MAX_TEAM_SIZE = 10;
    /** Rules: 2 bases, 4 aleros, 4 pivots. */
    private static final Map<String, Integer> POSITION_QUOTA = Map.of("B", 2, "A", 4, "P", 4);

    private final static Log log = LogFactory.getLog(SMUserTeamService.class);

    private final SmContentProvider smContentProvider;
    private final SmContentParser smContentParser;
    private final Executor executor;

    /** Full constructor used by ApplicationConfig (production). */
    public SMUserTeamService(SmContentProvider smContentProvider, SmContentParser smContentParser, Executor executor) {
        this.smContentProvider = smContentProvider;
        this.smContentParser = smContentParser;
        this.executor = executor;
    }

    /** Convenience constructor for tests — creates its own bounded pool. */
    public SMUserTeamService(SmContentProvider smContentProvider, SmContentParser smContentParser) {
        this(smContentProvider, smContentParser, Executors.newFixedThreadPool(5));
    }

    public List<SmTeam> getTeamsByCredentials(String user, String password) throws IOException {

        LoginResponse loginResponse = smContentProvider.authenticateUser(user, password);
        String token = loginResponse.getJwt();

        // Fetch teams list and market data in parallel — both only need the token
        CompletableFuture<List<SmTeam>> teamsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return smContentParser.getTeams(smContentProvider.getTeamsPage(user, token));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, executor);

        CompletableFuture<PlayerMarketData> marketFuture = CompletableFuture.supplyAsync(
                () -> smContentParser.providePlayerData(smContentProvider.getMarketPage(token)),
                executor);

        List<SmTeam> teams;
        PlayerMarketData playerMarketData;
        try {
            teams = teamsFuture.join();
            playerMarketData = marketFuture.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof UncheckedIOException uio) throw uio.getCause();
            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS, e);
        }

        // Fetch each team's roster page in parallel
        List<CompletableFuture<Void>> rosterFutures = teams.stream()
                .map(team -> CompletableFuture.runAsync(() -> {
                    try {
                        String teamId = team.getApiUrl().replaceAll(".*/", "");
                        String teamPage = smContentProvider.getTeamPage(team, token);
                        smContentParser.populateTeam(teamPage, team, playerMarketData);
                        String playerDetails = smContentProvider.getTeamPlayerDetails(teamId, token);
                        smContentParser.mergePlayerChangeIds(team, playerDetails);
                        computeTeamStats(team);
                        findCandidateBuyPlayer(team, playerMarketData);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, executor))
                .toList();

        try {
            CompletableFuture.allOf(rosterFutures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof UncheckedIOException uio) throw uio.getCause();
            throw new SmException(ErrorCode.TEAM_PAGE_ERROR, e);
        }

        log.debug("Teams found: " + teams);
        return teams;
    }

    private void findCandidateBuyPlayer(SmTeam team, PlayerMarketData marketData) {
        if (team.getPlayerList().size() >= MAX_TEAM_SIZE) return;

        Set<String> teamNames = team.getPlayerList().stream()
                .map(SmPlayer::getName)
                .collect(Collectors.toSet());

        // Which positions still have room according to the quota rules?
        Map<String, Long> positionCounts = team.getPlayerList().stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(SmPlayer::getPosition, Collectors.counting()));
        Set<String> neededPositions = POSITION_QUOTA.entrySet().stream()
                .filter(e -> positionCounts.getOrDefault(e.getKey(), 0L) < e.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (neededPositions.isEmpty()) return;

        marketData.findMostExpensiveFitPlayerName(teamNames, team.getCash(), neededPositions).ifPresent(name -> {
            Map<String, String> data = marketData.getPlayerMap(name);
            // POSITION is stored as "B"/"A"/"P" after parsing in SmContentParser
            String posName = data.get(MarketCategory.POSITION.name());
            long idPlayer = parseLong(data.get(MarketCategory.ID_PLAYER.name()));
            log.info("Candidate buy player for team " + team.getName() + ": " + name
                    + " (idPlayer=" + idPlayer + ", position=" + posName
                    + ", price=" + data.get(MarketCategory.PRICE.name()) + ")");
            SmPlayer candidate = SmPlayer.builder()
                    .name(name)
                    .position(posName != null ? posName : "-")
                    .marketData(data)
                    .idPlayer(idPlayer)
                    .build();
            team.setCandidateBuyPlayer(candidate);
            team.setCandidateAffordable(true);
        });
    }

    private long parseLong(String value) {
        try {
            return value != null ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
                .map(Float::parseFloat)
                .mapToInt(Float::intValue)
                .sum();
    }

    private float computeTeamScorePrediction(DoubleSummaryStatistics stats, SmTeam team) {
        return (float) stats.getAverage() * (team.getPlayerList().size()
                - team.getPlayerList().stream().filter(p -> !p.getStatus().isActive() || p.getStatus().isInjured()).count());
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }
}
