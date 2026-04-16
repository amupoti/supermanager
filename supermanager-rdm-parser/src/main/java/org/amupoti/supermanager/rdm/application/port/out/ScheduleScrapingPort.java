package org.amupoti.supermanager.rdm.application.port.out;

import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;

import java.util.List;

/**
 * Driven port: scrape schedule data for a team from the external RDM site.
 */
public interface ScheduleScrapingPort {
    List<Match> scrapeTeamMatches(LeagueTeam team);
    int scrapeCurrentMatchNumber();
}
