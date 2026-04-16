package org.amupoti.supermanager.parser.rdm;

import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;


/**
 * Created by amupoti on 28/08/2017.
 */
@Service
@Slf4j
public class RdmContentProvider {

    @Value("${rdm.url.team:https://www.rincondelmanager.com/smgr/equipo.php?eq=%s}")
    private String teamUrl;

    @Value("${rdm.url.main:https://www.rincondelmanager.com/smgr/}")
    private String mainPageUrl;

    @Value("${rdm.fetch-interval-ms:500}")
    private long fetchIntervalMs;

    @Autowired
    private RestTemplate restTemplate;

    private final Object rateLock = new Object();
    private long lastFetchMs = 0;

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }

    public String getTeamPage(LeagueTeam rdmTeam) {
        log.debug("Getting page for team {}", rdmTeam);
        throttle();
        String url = String.format(teamUrl, rdmTeam.name());
        ResponseEntity<String> exchange = restTemplate.getForEntity(url, String.class);
        return exchange.getBody();
    }

    public String getMainPage() {
        throttle();
        ResponseEntity<String> exchange = restTemplate.getForEntity(mainPageUrl, String.class);
        return exchange.getBody();
    }

    /**
     * Ensures at least {@code fetchIntervalMs} between consecutive HTTP requests to the RDM site,
     * protecting against accidental bursts (e.g., during cache pre-warming).
     */
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
}
