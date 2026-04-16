package org.amupoti.sm.main.service;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
import org.amupoti.supermanager.rdm.application.service.TeamScheduleService;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Periodically evicts and pre-warms all RDM caches so user requests always
 * hit warm cache entries rather than triggering live HTTP calls.
 *
 * ACB data is not refreshed here because it requires per-user credentials.
 */
@Service
@Slf4j
public class ScrapingRefreshJob {

    @Autowired
    private TeamScheduleService teamScheduleService;

    @Autowired
    private GetTeamScheduleUseCase scheduleUseCase;

    @Autowired
    @Qualifier("rdmFetchExecutor")
    private ExecutorService rdmFetchExecutor;

    @Value("${scraping.next-matches:5}")
    private int nextMatches;

    @Scheduled(fixedDelayString = "${scraping.rdm.refresh-interval-ms:3600000}")
    public void refreshRdmData() {
        log.info("Starting scheduled RDM cache refresh");
        teamScheduleService.evictCaches();
        try {
            teamScheduleService.getAllSchedules();

            int currentMatch = scheduleUseCase.getNextMatch();

            List<CompletableFuture<Void>> futures = Arrays.stream(LeagueTeam.values())
                    .map(team -> CompletableFuture.runAsync(
                            () -> scheduleUseCase.getTeamSchedule(team, currentMatch, nextMatches),
                            rdmFetchExecutor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("RDM cache refresh completed — {} teams pre-warmed for match window [{}, {}]",
                    LeagueTeam.values().length, currentMatch, currentMatch + nextMatches - 1);
        } catch (Exception e) {
            log.warn("RDM cache refresh failed — stale fallback data will be served until the next cycle", e);
        }
    }
}
