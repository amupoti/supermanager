package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.dto.LoginRequest;
import org.amupoti.supermanager.parser.acb.dto.LoginResponse;
import org.amupoti.supermanager.parser.acb.dto.SigninRequest;
import org.amupoti.supermanager.parser.acb.dto.SigninResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amupoti on 28/08/2017.
 */

@Slf4j
public class SmContentProvider {

    private static final String SUPERMANAGER_HOME_URL = "http://supermanager.acb.com/index/identificar";
    private static final String URL_TEAM_LIST = "https://supermanager.acb.com/api/basic/userteam/all";
    private static final String MARKET_PAGE = "https://supermanager.acb.com/api/basic/player?_filters={fields}&_page=1&_perPage=30";
    private static final String MARKET_PAGE_FIELDS = "[{\"field\":\"competition.idCompetition\",\"value\":1,\"operator\":\"=\",\"condition\":\"AND\"},{\"field\":\"edition.isActive\",\"value\":true,\"operator\":\"=\",\"condition\":\"AND\"}]";
    public static final String ACTIVE_COMPETITION = SUPERMANAGER_HOME_URL;
    public static final String LOGIN_URL = "https://admin-supermanager.acb.com/oauth/V2/open/accounttoken/getTokens";
    public static final String PRE_LOGIN_URL = "https://id.acb.com/api/signIn";
    private String competition;

    @Autowired
    private RestTemplate restTemplate;


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
        MultiValueMap<String, String> httpHeaders = addToken(token);
        ResponseEntity<String> exchange = restTemplate.exchange(URL_TEAM_LIST, HttpMethod.GET, new HttpEntity<String>(null, httpHeaders), String.class);
        return exchange.getBody();
    }

    private MultiValueMap<String, String> addToken(String token) {
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        httpHeaders.add("Authorization", "Bearer " + token);
        return httpHeaders;
    }

    public String getTeamPage(SmTeam team, String token) {

        try {
            return restTemplate.exchange(team.getApiUrl(), HttpMethod.GET, new HttpEntity<>(addToken(token)), String.class).getBody();
        } catch (RestClientException e) {
            throw new SmException(ErrorCode.TEAM_PAGE_ERROR, e);
        }
    }

    public LoginResponse authenticateUser(String user, String password) {

        return checkUserLogin(user, password);
    }

    private LoginResponse checkUserLogin(String user, String password) {

        try {
            ResponseEntity<SigninResponse> signinResponse = restTemplate.postForEntity(PRE_LOGIN_URL, buildSigninRequest(user, password), SigninResponse.class);
            ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(LOGIN_URL, addAuthenticationDetails(signinResponse.getBody()), LoginResponse.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new SmException(ErrorCode.INVALID_CREDENTIALS, e);

        }
    }

    private HttpEntity buildSigninRequest(String user, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("username",user);
        map.add("password",password);

        return new HttpEntity<>(map, headers);

    }

    private LoginRequest addAuthenticationDetails(SigninResponse signinResponse) {
        return new LoginRequest(signinResponse.getCode());
    }

    @Cacheable("marketPage")
    public String getMarketPage(String token) {
        log.info("Requesting market page");
        ResponseEntity<String> exchange = restTemplate.exchange(MARKET_PAGE, HttpMethod.GET, new HttpEntity<>(addToken(token)), String.class, MARKET_PAGE_FIELDS);
        return exchange.getBody();
    }
}
