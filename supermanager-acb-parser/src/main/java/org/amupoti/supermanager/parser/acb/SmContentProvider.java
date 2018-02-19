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

    public static final String SUPERMANAGER_HOME_URL = "http://supermanager.acb.com/index/identificar";
    private static final String BASE_URL = "http://supermanager.acb.com";
    private static final String URL_TEAM_LIST = "http://supermanager.acb.com/equipos/listado";
    private static final String MARKET_PAGE = "http://supermanager.acb.com/mercado";
    public static final String EUROPEO_HOME_URL = "http://supermanager.acb.com/europeo/";
    public static final String COPA_HOME_URL = "http://supermanager.acb.com/copadelrey";
    public static final String ACTIVE_COMPETITION = SUPERMANAGER_HOME_URL;
    private String competition;

    @Autowired
    private RestTemplate restTemplate;
    private String user;
    private String password;
    private HttpHeaders httpHeaders;

    public SmContentProvider(String competition) {
        this.competition = competition;
    }

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }

    public String getTeamsPage() {
        ResponseEntity<String> exchange = restTemplate.exchange(URL_TEAM_LIST, HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
        return exchange.getBody();
    }

    public String getTeamPage(SmTeam team) {
        return restTemplate.exchange(BASE_URL + team.getUrl(), HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class).getBody();
    }

    public String authenticateUser(String user, String password) {

        this.user = user;
        this.password = password;
        httpHeaders = prepareHeaders();
        addCookieFromEntryPageToHeaders(httpHeaders);
        return checkUserLogin(user, password, httpHeaders);
    }

    private String checkUserLogin(String user, String password, HttpHeaders httpHeaders) {
        HttpEntity<MultiValueMap<String, String>> httpEntity;
        MultiValueMap<String, String> params = addFormParams(user, password);
        httpEntity = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> exchange = restTemplate.postForEntity(SUPERMANAGER_HOME_URL, httpEntity, String.class, params);
        return exchange.getBody();
    }

    private void addCookieFromEntryPageToHeaders(HttpHeaders httpHeaders) {
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(competition, HttpMethod.GET, httpEntity, String.class);
        String cookie = exchange.getHeaders().get("Set-Cookie").toString().replace("[", "").split(";")[0];
        httpHeaders.add("Cookie", cookie);
    }

    private MultiValueMap<String, String> addFormParams(String user, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", user);
        params.add("clave", password);
        params.add("entrar", "Entrar");
        return params;
    }

    private HttpHeaders prepareHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Host", "supermanager.acb.com");
        httpHeaders.add(HttpHeaders.ACCEPT, "*/*");
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.REFERER, "http://supermanager.acb.com/index/identificar");
        return httpHeaders;
    }

    public String getMarketPage() {
        ResponseEntity<String> exchange = restTemplate.exchange(MARKET_PAGE, HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);
        return exchange.getBody();
    }
}
