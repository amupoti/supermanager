package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.dto.LoginRequest;
import org.amupoti.supermanager.parser.acb.dto.LoginResponse;
import org.amupoti.supermanager.parser.acb.privateleague.PrivateLeagueCategory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * Created by amupoti on 28/08/2017.
 */

@Slf4j
public class SmContentProvider {

    private static final String SUPERMANAGER_HOME_URL = "http://supermanager.acb.com/index/identificar";
    public static final String BASE_URL = "http://supermanager.acb.com";
    private static final String URL_TEAM_LIST = "https://supermanager.acb.com/api/basic/userteam/all";
    private static final String MARKET_PAGE = "http://supermanager.acb.com/mercado";
    public static final String EUROPEO_HOME_URL = "http://supermanager.acb.com/europeo/";
    public static final String COPA_HOME_URL = "http://supermanager.acb.com/copadelrey";
    public static final String ACTIVE_COMPETITION = SUPERMANAGER_HOME_URL;
    public static final String LOGIN_URL = "https://supermanager.acb.com/oauth/V2/token/open";
    private String competition;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${privateLeague.id}")
    private String urlPrivateLeague;



    public SmContentProvider(String competition) {
        this.competition = competition;
    }

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }

    @Cacheable("teamsPage")
    public String getTeamsPage(String user, String token) {
        log.info("Requesting all teams page for user {}", user);
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        httpHeaders.add("Authorization", "Bearer " + token);
        ResponseEntity<String> exchange = restTemplate.exchange(URL_TEAM_LIST, HttpMethod.GET, new HttpEntity<String>(null, httpHeaders), String.class);
        return exchange.getBody();
    }

    public String getTeamPage(SmTeam team) {
        return restTemplate.exchange(BASE_URL + team.getUrl(), HttpMethod.GET, new HttpEntity<>(null), String.class).getBody();
    }

    public LoginResponse authenticateUser(String user, String password) {

        return checkUserLogin(user, password);
    }

    public String getPrivateLeaguePage(PrivateLeagueCategory category) {
        return restTemplate.exchange(urlPrivateLeague + category.getPagePath(), HttpMethod.GET, new HttpEntity<>(null), String.class).getBody();
    }

    private LoginResponse checkUserLogin(String user, String password) {
        LoginRequest params = addAuthenticationDetails(user, password);
        ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(LOGIN_URL, params, LoginResponse.class, params);
        return responseEntity.getBody();
    }

    private void addCookieFromEntryPageToHeaders(MultiValueMap<String, String> httpHeaders) {
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(competition, HttpMethod.GET, httpEntity, String.class);
        String cookie = exchange.getHeaders().get("Set-Cookie").toString().replace("[", "").split(";")[0];
        httpHeaders.add("Cookie", cookie);
    }

    private LoginRequest addAuthenticationDetails(String user, String password) {
        return new LoginRequest(user, password);
    }

    private MultiValueMap<String, String> prepareHeaders() {
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        httpHeaders.add("Host", "supermanager.acb.com");
        httpHeaders.add(HttpHeaders.ACCEPT, "*/*");
        httpHeaders.add("Content-type", MediaType.APPLICATION_FORM_URLENCODED.toString());
        httpHeaders.add(HttpHeaders.REFERER, "http://supermanager.acb.com/index/identificar");
        return httpHeaders;
    }

    @Cacheable("marketPage")
    public String getMarketPage() {
        log.info("Requesting market page");
        ResponseEntity<String> exchange = restTemplate.exchange(MARKET_PAGE, HttpMethod.GET, new HttpEntity<>(null), String.class);
        return exchange.getBody();
    }
}
