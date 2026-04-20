package org.amupoti.supermanager.rdm.adapter.out.rdm;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.rdm.application.port.out.ScheduleScrapingPort;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.amupoti.supermanager.rdm.exception.RdmException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Out-adapter: scrapes team match schedules from rincondelmanager.com.
 * Contains all HTTP fetching and HTML parsing logic.
 */
@Component
@Slf4j
public class RdmScrapingAdapter implements ScheduleScrapingPort {

    @Value("${rdm.url.team:https://www.rincondelmanager.com/smgr/equipo.php?eq=%s}")
    private String teamUrl;

    @Value("${rdm.url.main:https://www.rincondelmanager.com/smgr/}")
    private String mainPageUrl;

    @Value("${rdm.fetch-interval-ms:500}")
    private long fetchIntervalMs;

    private final RestTemplate restTemplate;
    private final Object rateLock = new Object();
    private long lastFetchMs = 0;

    public RdmScrapingAdapter(@Qualifier("rdmRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }

    @Override
    public List<Match> scrapeTeamMatches(LeagueTeam team) {
        log.debug("Getting page for team {}", team);
        String html = fetch(String.format(teamUrl, team.name()));
        return parseTeamMatches(html, team);
    }

    @Override
    public int scrapeCurrentMatchNumber() {
        String page = fetch(mainPageUrl);
        return Integer.parseInt(parseMatchNumber(page));
    }

    // --- HTTP ---

    private String fetch(String url) {
        throttle();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    private void throttle() {
        synchronized (rateLock) {
            long now = System.currentTimeMillis();
            long waitMs = fetchIntervalMs - (now - lastFetchMs);
            if (waitMs > 0) {
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastFetchMs = System.currentTimeMillis();
        }
    }

    // --- HTML parsing ---

    private List<Match> parseTeamMatches(String html, LeagueTeam rdmTeam) {
        Document doc = Jsoup.parse(html);
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

    private String parseMatchNumber(String page) {
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
