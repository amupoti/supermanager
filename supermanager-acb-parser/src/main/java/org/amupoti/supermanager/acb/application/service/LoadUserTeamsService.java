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
import org.amupoti.supermanager.acb.exception.ErrorCode;
import org.amupoti.supermanager.acb.exception.SmException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * Use case: load, populate, and enrich all user teams with market and stats data.
 * Orchestrates parallel fetching across the out-ports.
 */
@Slf4j
public class LoadUserTeamsService implements LoadUserTeamsUseCase {

    private final AuthenticationPort authPort;
    private final TeamDataPort teamDataPort;
    private final MarketDataPort marketDataPort;
    private final PlayerStatsPort playerStatsPort;
    private final ComputeTeamStatsService computeTeamStatsService;
    private final FindCandidateService findCandidateService;
    private final Executor executor;

    public LoadUserTeamsService(AuthenticationPort authPort,
                                 TeamDataPort teamDataPort,
                                 MarketDataPort marketDataPort,
                                 PlayerStatsPort playerStatsPort,
                                 ComputeTeamStatsService computeTeamStatsService,
                                 FindCandidateService findCandidateService,
                                 Executor executor) {
        this.authPort = authPort;
        this.teamDataPort = teamDataPort;
        this.marketDataPort = marketDataPort;
        this.playerStatsPort = playerStatsPort;
        this.computeTeamStatsService = computeTeamStatsService;
        this.findCandidateService = findCandidateService;
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
                        computeTeamStatsService.computeTeamStats(team);
                        findCandidateService.findCandidateBuyPlayer(team, marketData);
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
}
