package org.amupoti.supermanager.rdm.application.service;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.rdm.application.port.in.GetAllSchedulesUseCase;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
import org.amupoti.supermanager.rdm.application.port.out.ScheduleScrapingPort;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;
import org.amupoti.supermanager.rdm.exception.RdmException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Use case service: provides team match schedule data with caching and stale-fallback.
 */
@Slf4j
public class TeamScheduleService implements GetAllSchedulesUseCase, GetTeamScheduleUseCase {

    private final ScheduleScrapingPort scrapingPort;
    private final ConcurrentHashMap<LeagueTeam, List<Match>> fallbackStore = new ConcurrentHashMap<>();
    private final AtomicInteger lastKnownMatch = new AtomicInteger(1);

    public TeamScheduleService(ScheduleScrapingPort scrapingPort) {
        this.scrapingPort = scrapingPort;
    }

    @Override
    @Cacheable("TeamData")
    public List<TeamSchedule> getAllSchedules() {
        log.info("Fetching schedules for all {} teams", LeagueTeam.values().length);
        return Arrays.stream(LeagueTeam.values())
                .map(this::fetchAndCache)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("RdmTeamData")
    public TeamSchedule getTeamSchedule(LeagueTeam team, int fromMatch, int numMatches) {
        int lastMatch = Math.min(fromMatch + numMatches - 1, 34);
        try {
            List<Match> all = scrapingPort.scrapeTeamMatches(team);
            fallbackStore.put(team, all);
            return TeamSchedule.builder()
                    .team(team)
                    .matches(all.subList(fromMatch - 1, lastMatch))
                    .build();
        } catch (Exception e) {
            log.warn("Scraping failed for {}, trying stale fallback", team.getTeamName(), e);
            List<Match> stale = fallbackStore.get(team);
            if (stale == null) throw new RdmException("No data for " + team.getTeamName());
            int safeFirst = Math.min(fromMatch - 1, stale.size());
            int safeLast  = Math.min(lastMatch, stale.size());
            return TeamSchedule.builder().team(team).matches(stale.subList(safeFirst, safeLast)).build();
        }
    }

    @Override
    @Cacheable("NextMatch")
    public int getNextMatch() {
        try {
            int match = scrapingPort.scrapeCurrentMatchNumber();
            lastKnownMatch.set(match);
            return match;
        } catch (Exception e) {
            int stale = lastKnownMatch.get();
            log.warn("Failed to fetch current match, using last known {}", stale, e);
            return stale;
        }
    }

    @CacheEvict(value = {"TeamData", "NextMatch", "RdmTeamData"}, allEntries = true)
    public void evictCaches() {
        log.info("All schedule caches evicted");
    }

    private TeamSchedule fetchAndCache(LeagueTeam team) {
        try {
            List<Match> all = scrapingPort.scrapeTeamMatches(team);
            fallbackStore.put(team, all);
            return TeamSchedule.builder().team(team).matches(all).build();
        } catch (Exception e) {
            log.warn("Failed to fetch schedule for {} — trying stale", team.getTeamName(), e);
            List<Match> stale = fallbackStore.get(team);
            if (stale == null) throw new RdmException("No data for " + team.getTeamName());
            return TeamSchedule.builder().team(team).matches(stale).build();
        }
    }
}
