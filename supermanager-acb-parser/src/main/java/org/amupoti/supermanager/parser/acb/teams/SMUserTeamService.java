package org.amupoti.supermanager.parser.acb.teams;

import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.parser.acb.dto.LoginResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<Team> getTeamsByCredentials(String user, String password) throws IOException {

        LoginResponse loginResponse = smContentProvider.authenticateUser(user, password);
        String token = loginResponse.getJwt();

        // Fetch teams list and market data in parallel — both only need the token
        CompletableFuture<List<Team>> teamsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return smContentParser.getTeams(smContentProvider.getTeamsPage(user, token));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, executor);

        CompletableFuture<MarketData> marketFuture = CompletableFuture.supplyAsync(
                () -> smContentParser.providePlayerData(smContentProvider.getMarketPage(token)),
                executor);

        List<Team> teams;
        MarketData playerMarketData;
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
                        enrichWithLastFourAverages(team, playerMarketData, token);
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

    /**
     * Fetches per-player stats from /api/basic/playerstats/1/{idPlayer} for each
     * player on the current roster and each slot candidate in parallel, then stores
     * the last-4-match average (with win bonus) in the shared market data map.
     */
    private void enrichWithLastFourAverages(Team team, MarketData playerMarketData, String token) {
        Set<Long> processed = new HashSet<>();

        List<Player> players = Stream.concat(
                        team.getPlayerList().stream(),
                        team.getCandidatesByPosition() != null
                                ? team.getCandidatesByPosition().values().stream()
                                : Stream.empty())
                .filter(p -> p.getIdPlayer() > 0)
                .filter(p -> processed.add(p.getIdPlayer()))
                .toList();

        List<CompletableFuture<Void>> futures = players.stream()
                .map(player -> CompletableFuture.runAsync(() -> {
                    try {
                        String json = smContentProvider.getPlayerStats(player.getIdPlayer(), token);
                        String avg = smContentParser.computeLastFourAverage(json);
                        Map<String, String> data = playerMarketData.getPlayerMap(player.getName());
                        if (data != null && avg != null) {
                            data.put(MarketCategory.LAST_FOUR_VAL.name(), avg);
                        }
                    } catch (Exception e) {
                        log.warn("Could not fetch player stats for " + player.getName()
                                + " (idPlayer=" + player.getIdPlayer() + "): " + e.getMessage());
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void findCandidateBuyPlayer(Team team, MarketData marketData) {
        if (team.getPlayerList().size() >= MAX_TEAM_SIZE) return;

        Set<String> teamNames = team.getPlayerList().stream()
                .map(Player::getName)
                .collect(Collectors.toSet());

        Map<String, Long> positionCounts = team.getPlayerList().stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        long spanishCount = team.getPlayerList().stream()
                .filter(p -> p.getStatus().isSpanish())
                .count();
        long foreignCount = team.getPlayerList().stream()
                .filter(p -> p.getStatus().isForeign())
                .count();
        // Only enforce Spanish quota if the market data actually carries nationality info
        boolean requireSpanish = marketData.hasSpanishData() && spanishCount < 4;
        boolean excludeForeign = foreignCount >= 2;
        log.debug("Team " + team.getName() + " nationality: spanish=" + spanishCount
                + " foreign=" + foreignCount + " hasSpanishData=" + marketData.hasSpanishData()
                + " → requireSpanish=" + requireSpanish + " excludeForeign=" + excludeForeign);

        // Find one best candidate per position that has at least one open slot
        Map<String, Player> candidatesByPosition = new java.util.HashMap<>();
        POSITION_QUOTA.forEach((pos, quota) -> {
            if (positionCounts.getOrDefault(pos, 0L) < quota) {
                marketData.findMostExpensiveFitPlayerName(teamNames, team.getCash(), Set.of(pos),
                                requireSpanish, excludeForeign)
                        .ifPresent(name -> {
                            Map<String, String> data = marketData.getPlayerMap(name);
                            long idPlayer = parseLong(data.get(MarketCategory.ID_PLAYER.name()));
                            log.debug("Candidate for team " + team.getName() + " pos=" + pos + ": " + name
                                    + " (idPlayer=" + idPlayer + ", price=" + data.get(MarketCategory.PRICE.name()) + ")");
                            candidatesByPosition.put(pos, Player.builder()
                                    .name(name)
                                    .position(pos)
                                    .marketData(data)
                                    .idPlayer(idPlayer)
                                    .build());
                        });
            }
        });

        if (!candidatesByPosition.isEmpty()) {
            team.setCandidatesByPosition(candidatesByPosition);
        }
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

    private void computeTeamStats(Team team) {
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

    private int getBrokerSum(Team team) {
        return team.getPlayerList().stream()
                .filter(p -> p.getMarketData() != null)
                .map(p -> p.getMarketData().get(MarketCategory.PRICE.name()))
                .map(Float::parseFloat)
                .mapToInt(Float::intValue)
                .sum();
    }

    private float computeTeamScorePrediction(DoubleSummaryStatistics stats, Team team) {
        return (float) stats.getAverage() * (team.getPlayerList().size()
                - team.getPlayerList().stream().filter(p -> !p.getStatus().isActive() || p.getStatus().isInjured()).count());
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }
}
