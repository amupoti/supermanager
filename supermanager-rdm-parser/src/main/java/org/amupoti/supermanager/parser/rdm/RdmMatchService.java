package org.amupoti.supermanager.parser.rdm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by amupoti on 28/10/2018.
 */
@Service
@Slf4j
public class RdmMatchService {

    @Autowired
    private RdmContentParser parser;

    @Autowired
    private RdmContentProvider provider;

    /** Last successfully fetched full-season match list per team — used as stale-data fallback. */
    private final ConcurrentHashMap<RdmTeam, List<Match>> fallbackStore = new ConcurrentHashMap<>();

    /** Last known current match number — used as fallback if the main page is unreachable. */
    private final AtomicInteger lastKnownMatch = new AtomicInteger(1);

    @Cacheable("TeamData")
    public List<RdmTeamData> getTeamsData() {
        log.info("Fetching match data for all {} teams", RdmTeam.values().length);
        return Arrays.stream(RdmTeam.values())
                .map(this::getTeamData)
                .collect(Collectors.toList());
    }

    @Cacheable("NextMatch")
    public int getNextMatch() {
        try {
            int match = getCurrentMatchFromIdealTeamWidget() + 1;
            lastKnownMatch.set(match);
            return match;
        } catch (Exception e) {
            int stale = lastKnownMatch.get();
            log.warn("Failed to fetch current match from RDM, serving last known value {}", stale, e);
            return stale;
        }
    }

    @Cacheable("RdmTeamData")
    public RdmTeamData getTeamDataFromMatchNumber(RdmTeam team, int matchNumber, int nextMatches) {
        int lastMatch = Math.min(matchNumber + nextMatches - 1, 34);
        try {
            String teamPage = provider.getTeamPage(team);
            List<Match> allMatches = parser.getTeamMatches(teamPage, team);
            fallbackStore.put(team, allMatches);
            return RdmTeamData.builder()
                    .matches(allMatches.subList(matchNumber - 1, lastMatch))
                    .team(team)
                    .build();
        } catch (Exception e) {
            log.warn("RDM fetch failed for {}, trying stale fallback", team.getTeamName(), e);
            List<Match> stale = fallbackStore.get(team);
            if (stale == null) {
                throw new RdmException("No RDM data available for team " + team.getTeamName() + " and live fetch failed");
            }
            int safeLastMatch = Math.min(lastMatch, stale.size());
            int safeFirstMatch = Math.min(matchNumber - 1, safeLastMatch);
            return RdmTeamData.builder()
                    .matches(stale.subList(safeFirstMatch, safeLastMatch))
                    .team(team)
                    .build();
        }
    }

    /** Called by the background refresh job to force re-fetch on the next access. */
    @CacheEvict(value = {"TeamData", "NextMatch", "RdmTeamData"}, allEntries = true)
    public void evictRdmCaches() {
        log.info("All RDM caches evicted");
    }

    public int getCurrentMatchFromIdealTeamWidget() {
        String page = provider.getMainPage();
        String matchNumber = parser.getMatchNumber(page);
        return Integer.parseInt(matchNumber) - 1;
    }

    private RdmTeamData getTeamData(RdmTeam team) {
        try {
            String teamPage = provider.getTeamPage(team);
            List<Match> allMatches = parser.getTeamMatches(teamPage, team);
            fallbackStore.put(team, allMatches);
            return RdmTeamData.builder().matches(allMatches).team(team).build();
        } catch (Exception e) {
            log.warn("Failed to fetch match data for {} — trying stale fallback", team.getTeamName(), e);
            List<Match> stale = fallbackStore.get(team);
            if (stale == null) throw new RdmException("No data for team " + team.getTeamName());
            return RdmTeamData.builder().matches(stale).team(team).build();
        }
    }
}
