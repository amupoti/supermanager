package org.amupoti.supermanager.parser.rdm;

import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by amupoti on 28/08/2017.
 */
@Service
public class RdmContentParser {

    public List<Match> getTeamMatches(String html, LeagueTeam rdmTeam) {
        Document doc = Jsoup.parse(html);
        // Anchor on the wrapper whose header says "Calendario" — resilient to other tables on the page
        Element wrapper = doc.selectFirst("div.proxima-table-wrapper:has(h4:containsOwn(Calendario))");
        if (wrapper == null) {
            throw new RdmException("Could not find calendar section for team " + rdmTeam.name());
        }
        Elements rows = wrapper.select("tbody tr");
        if (rows.isEmpty()) {
            throw new RdmException("Calendar table has no rows for team " + rdmTeam.name());
        }
        return rows.stream()
                .map(row -> parseMatch(row, rdmTeam))
                .collect(Collectors.toList());
    }

    private Match parseMatch(Element row, LeagueTeam rdmTeam) {
        // Each row has two td.team-result cells: [0] = home, [1] = away
        Elements teamCells = row.select("td.team-result");
        if (teamCells.size() < 2) {
            throw new RdmException("Row is missing team cells for team " + rdmTeam.name());
        }
        String homeTeam = teamCells.get(0).selectFirst("span.team-name").text();
        String awayTeam = teamCells.get(1).selectFirst("span.team-name").text();
        String againstTeam = homeTeam.equals(rdmTeam.name()) ? awayTeam : homeTeam;
        return Match.builder()
                .againstTeam(LeagueTeam.valueOf(againstTeam))
                .local(homeTeam.equals(rdmTeam.name()))
                .build();
    }

    public String getMatchNumber(String page) {
        Document doc = Jsoup.parse(page);
        for (Element h4 : doc.select("div.proxima-table-header h4")) {
            String text = h4.text();
            if (text.startsWith("Jornada ")) {
                return text.split("Jornada ")[1];
            }
        }
        throw new RdmException("Could not find current match number on main page");
    }
}
