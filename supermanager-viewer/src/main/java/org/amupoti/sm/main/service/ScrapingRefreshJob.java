package org.amupoti.sm.main.service;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
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
 * ACB caches (teamsPage, marketPage) expire via Caffeine TTL and are
 * re-populated automatically on the next user login.
 */
@Service
@Slf4j
public class ScrapingRefreshJob {

    @Autowired
    private RdmMatchService rdmMatchService;

    @Autowired
    @Qualifier("rdmFetchExecutor")
    private ExecutorService rdmFetchExecutor;

    @Value("${scraping.next-matches:5}")
    private int nextMatches;

    @Scheduled(fixedDelayString = "${scraping.rdm.refresh-interval-ms:3600000}")
    public void refreshRdmData() {
        log.info("Starting scheduled RDM cache refresh");
        rdmMatchService.evictRdmCaches();
        try {
            // Pre-warm TeamData (all 34 rounds for all teams — used by the calendar page)
            rdmMatchService.getTeamsData();

            // Pre-warm NextMatch (used to compute match window shown to users)
            int currentMatch = rdmMatchService.getNextMatch();

            // Pre-warm per-team match-window entries in parallel (used by user team views)
            List<CompletableFuture<Void>> futures = Arrays.stream(LeagueTeam.values())
                    .map(team -> CompletableFuture.runAsync(
                            () -> rdmMatchService.getTeamDataFromMatchNumber(team, currentMatch, nextMatches),
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
