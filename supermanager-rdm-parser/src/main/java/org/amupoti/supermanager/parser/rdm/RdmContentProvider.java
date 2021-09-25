package org.amupoti.supermanager.parser.rdm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;


/**
 * Created by amupoti on 28/08/2017.
 */
@Service
@Slf4j
public class RdmContentProvider {

    private final static String TEAM_URL = "https://www.rincondelmanager.com/smgr/equipo.php?eq=%s";
    public static final String RDM_MAIN_PAGE = "https://www.rincondelmanager.com/smgr/";
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }


    protected String getTeamPage(RdmTeam rdmTeam) {
        log.debug("Getting page for team {}", rdmTeam);
        String url = String.format(TEAM_URL, rdmTeam.name());
        ResponseEntity<String> exchange = restTemplate.getForEntity(url, String.class);
        return exchange.toString();
    }

    protected String getMainPage() {
        String url = RDM_MAIN_PAGE;
        ResponseEntity<String> exchange = restTemplate.getForEntity(url, String.class);
        return exchange.toString();
    }
}
