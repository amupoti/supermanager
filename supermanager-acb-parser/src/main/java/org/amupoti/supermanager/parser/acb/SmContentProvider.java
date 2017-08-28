package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * Created by amupoti on 28/08/2017.
 */

public class SmContentProvider {

    private static final String ENTRY_URL = "http://supermanager.acb.com/index/identificar";
    private static final String BASE_URL = "http://supermanager.acb.com";
    private static final String EUROPEO = "http://supermanager.acb.com/europeo/";
    private static final String URL_TEAM_LIST = "http://supermanager.acb.com/equipos/listado";
    private String competition;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        //TODO: add in properties file
        competition = EUROPEO;
    }


    public String getTeamsPage(HttpHeaders httpHeaders) {
        ResponseEntity<String> exchange = restTemplate.exchange(URL_TEAM_LIST, HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);

        return exchange.getBody();
    }

    public void authenticateUser(String user, String password, HttpHeaders httpHeaders) {
        HttpEntity<MultiValueMap<String, String>> httpEntity;
        MultiValueMap<String, String> params = addFormParams(user, password);
        httpEntity = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> exchange = restTemplate.postForEntity(ENTRY_URL, httpEntity, String.class, params);
    }

    public String getCookieFromEntryPage(HttpEntity<MultiValueMap<String, String>> httpEntity) {
        ResponseEntity<String> exchange = restTemplate.exchange(competition, HttpMethod.GET, httpEntity, String.class);
        return exchange.getHeaders().get("Set-Cookie").toString().replace("[", "").split(";")[0];
    }

    public MultiValueMap<String, String> addFormParams(String user, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", user);
        params.add("clave", password);
        params.add("entrar", "Entrar");
        return params;
    }

    public HttpHeaders prepareHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Host", "supermanager.acb.com");
        httpHeaders.add(HttpHeaders.ACCEPT, "*/*");
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.REFERER, "http://supermanager.acb.com/index/identificar");
        return httpHeaders;
    }

    public String getTeamPage(HttpHeaders httpHeaders, SmTeam team) {
        return restTemplate.exchange(BASE_URL + team.getUrl(), HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class).getBody();
    }

}
