package org.amupoti.supermanager.acb.application.service;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.application.port.in.LoadUserTeamsUseCase;
import org.amupoti.supermanager.acb.application.port.out.AuthenticationPort;
import org.amupoti.supermanager.acb.application.port.out.MarketDataPort;
import org.amupoti.supermanager.acb.application.port.out.PlayerStatsPort;
import org.amupoti.supermanager.acb.application.port.out.TeamDataPort;
import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Use case: load, populate, and enrich all user teams with market and stats data.
 * Orchestrates parallel fetching across the out-ports.
 */
@Slf4j
public class LoadUserTeamsService implements LoadUserTeamsUseCase {

    private static final int MAX_TEAM_SIZE = 10;
    private static final Map<String, Integer> POSITION_QUOTA = Map.of("B", 2, "A", 4, "P", 4);

    private final AuthenticationPort authPort;
    private final TeamDataPort teamDataPort;
    private final MarketDataPort marketDataPort;
    private final PlayerStatsPort playerStatsPort;
    private final Executor executor;

    public LoadUserTeamsService(AuthenticationPort authPort,
                                 TeamDataPort teamDataPort,
                                 MarketDataPort marketDataPort,
                                 PlayerStatsPort playerStatsPort,
                                 Executor executor) {
        this.authPort = authPort;
        this.teamDataPort = teamDataPort;
        this.marketDataPort = marketDataPort;
        this.playerStatsPort = playerStatsPort;
        this.executor = executor;
    }

    @Override
    public List<Team> loadTeams(String user, String password) throws IOException {
        String token = authPort.authenticate(user, password);

        CompletableFuture<List<Team>> teamsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return teamDataPort.getTeams(user, token);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, executor);

        CompletableFuture<MarketData> marketFuture = CompletableFuture.supplyAsync(
                () -> marketDataPort.getMarketData(token), executor);

        List<Team> teams;
        MarketData marketData;
        try {
            teams = teamsFuture.join();
            marketData = marketFuture.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof UncheckedIOException uio) throw uio.getCause();
            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS, e);
        }

        List<CompletableFuture<Void>> rosterFutures = teams.stream()
                .map(team -> CompletableFuture.runAsync(() -> {
                    try {
                        teamDataPort.populateTeam(team, marketData, token);
                        teamDataPort.mergePlayerChangeIds(team, token);
                        computeTeamStats(team);
                        findCandidateBuyPlayer(team, marketData);
                        enrichWithLastFourAverages(team, marketData, token);
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

        log.debug("Teams loaded: {}", teams);
        return teams;
    }

    private void enrichWithLastFourAverages(Team team, MarketData marketData, String token) {
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
                    String avg = playerStatsPort.getLastFourAverage(player.getIdPlayer(), token);
                    Map<String, String> data = marketData.getPlayerMap(player.getName());
                    if (data != null && avg != null) {
                        data.put(MarketCategory.LAST_FOUR_VAL.name(), avg);
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
                .filter(p -> p.getStatus().isSpanish()).count();
        long foreignCount = team.getPlayerList().stream()
                .filter(p -> p.getStatus().isForeign()).count();

        boolean requireSpanish = marketData.hasSpanishData() && spanishCount < 4;
        boolean excludeForeign = foreignCount >= 2;

        Map<String, Player> candidatesByPosition = new java.util.HashMap<>();
        POSITION_QUOTA.forEach((pos, quota) -> {
            if (positionCounts.getOrDefault(pos, 0L) < quota) {
                marketData.findMostExpensiveFitPlayerName(teamNames, team.getCash(), Set.of(pos),
                                requireSpanish, excludeForeign)
                        .ifPresent(name -> {
                            Map<String, String> data = marketData.getPlayerMap(name);
                            long idPlayer = parseLong(data.get(MarketCategory.ID_PLAYER.name()));
                            candidatesByPosition.put(pos, Player.builder()
                                    .name(name).position(pos)
                                    .marketData(data).idPlayer(idPlayer).build());
                        });
            }
        });

        if (!candidatesByPosition.isEmpty()) {
            team.setCandidatesByPosition(candidatesByPosition);
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

    private long parseLong(String value) {
        try { return value != null ? Long.parseLong(value) : 0L; }
        catch (NumberFormatException e) { return 0L; }
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }
}
