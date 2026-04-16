package org.amupoti.supermanager.rdm.adapter.out.rdm;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.rdm.application.port.out.ScheduleScrapingPort;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.amupoti.supermanager.parser.rdm.RdmContentParser;
import org.amupoti.supermanager.parser.rdm.RdmContentProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Out-adapter: scrapes team match schedules from rincondelmanager.com.
 * Delegates HTTP and HTML parsing to the existing provider/parser — these will be
 * inlined here in the cleanup step.
 */
@Component
@Slf4j
public class RdmScrapingAdapter implements ScheduleScrapingPort {

    private final RdmContentProvider rdmContentProvider;
    private final RdmContentParser rdmContentParser;

    public RdmScrapingAdapter(RdmContentProvider rdmContentProvider, RdmContentParser rdmContentParser) {
        this.rdmContentProvider = rdmContentProvider;
        this.rdmContentParser = rdmContentParser;
    }

    @Override
    public List<Match> scrapeTeamMatches(LeagueTeam team) {
        String html = rdmContentProvider.getTeamPage(team);
        return rdmContentParser.getTeamMatches(html, team);
    }

    @Override
    public int scrapeCurrentMatchNumber() {
        String page = rdmContentProvider.getMainPage();
        return Integer.parseInt(rdmContentParser.getMatchNumber(page));
    }
}
