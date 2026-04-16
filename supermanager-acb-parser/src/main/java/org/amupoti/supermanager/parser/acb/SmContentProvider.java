package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.dto.LoginRequest;
import org.amupoti.supermanager.parser.acb.dto.LoginResponse;
import org.amupoti.supermanager.parser.acb.dto.SigninResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.InfrastructureException;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Created by amupoti on 28/08/2017.
 */
@Slf4j
public class SmContentProvider {

    @Value("${acb.url.signin:https://id.acb.com/api/signIn}")
    private String preLoginUrl;

    @Value("${acb.url.token:https://supermanager.acb.com/oauth/V2/open/accounttoken/getTokens}")
    private String loginUrl;

    @Value("${acb.url.teams:https://supermanager.acb.com/api/basic/userteam/all}")
    private String teamListUrl;

    @Value("${acb.url.market:https://supermanager.acb.com/api/basic/player?_filters={fields}&_page=1&_perPage=300}")
    private String marketPageUrl;

    private static final String MARKET_PAGE_FIELDS =
            "[{\"field\":\"competition.idCompetition\",\"value\":1,\"operator\":\"=\",\"condition\":\"AND\"}," +
            "{\"field\":\"edition.isActive\",\"value\":true,\"operator\":\"=\",\"condition\":\"AND\"}]";

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }

    @Cacheable("teamsPage")
    public String getTeamsPage(String user, String token) {
        log.info("Requesting all teams page for user {}", user);
        MultiValueMap<String, String> httpHeaders = addToken(token);
        ResponseEntity<String> exchange = restTemplate.exchange(teamListUrl, HttpMethod.GET, new HttpEntity<String>(null, httpHeaders), String.class);
        log.info("userteam/all raw response: {}", exchange.getBody());
        return exchange.getBody();
    }

    private MultiValueMap<String, String> addToken(String token) {
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        httpHeaders.add("Authorization", "Bearer " + token);
        return httpHeaders;
    }

    public String getTeamPage(SmTeam team, String token) {
        try {
            String body = restTemplate.exchange(team.getApiUrl(), HttpMethod.GET, new HttpEntity<>(addToken(token)), String.class).getBody();
            log.info("journeys raw response for team {}: {}", team.getName(), body);
            return body;
        } catch (RestClientException e) {
            throw new SmException(ErrorCode.TEAM_PAGE_ERROR, e);
        }
    }

    public LoginResponse authenticateUser(String user, String password) {
        LoginResponse response = checkUserLogin(user, password);
        if (response == null || response.getJwt() == null) {
            log.warn("ACB auth response missing JWT — API may have changed. Response: {}", response);
            throw new SmException(ErrorCode.INVALID_CREDENTIALS);
        }
        return response;
    }

    @Retryable(maxAttempts = 5, value = InfrastructureException.class,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    public LoginResponse checkUserLogin(String user, String password) {
        log.info("Trying to log user in...");
        try {
            ResponseEntity<SigninResponse> signinResponse = restTemplate.postForEntity(preLoginUrl, buildSigninRequest(user, password), SigninResponse.class);
            ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(loginUrl, addAuthenticationDetails(signinResponse.getBody()), LoginResponse.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            throw new SmException(ErrorCode.INVALID_CREDENTIALS, e);
        } catch (Exception e) {
            throw new InfrastructureException("Ha ocurrido un problema al intentar recuperar la información", e);
        }
    }

    private HttpEntity buildSigninRequest(String user, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", user);
        map.add("password", password);

        return new HttpEntity<>(map, headers);
    }

    private LoginRequest addAuthenticationDetails(SigninResponse signinResponse) {
        return new LoginRequest(signinResponse.getCode());
    }

    @Cacheable("marketPage")
    public String getMarketPage(String token) {
        log.info("Requesting market page");
        ResponseEntity<String> exchange = restTemplate.exchange(marketPageUrl, HttpMethod.GET, new HttpEntity<>(addToken(token)), String.class, MARKET_PAGE_FIELDS);
        return exchange.getBody();
    }

    public String getPendingChanges(String teamId, String token) {
        log.info("Requesting pending changes for team {}", teamId);
        String filters = "[{\"field\":\"idUserTeam\",\"value\":" + teamId + ",\"operator\":\"=\",\"condition\":\"AND\"}]";
        String body = restTemplate.exchange(
                "https://supermanager.acb.com/api/basic/userteamplayerchange?_filters={filters}",
                HttpMethod.GET,
                new HttpEntity<>(addToken(token)),
                String.class,
                filters).getBody();
        log.info("pendingChanges raw response for team {}: {}", teamId, body);
        return body;
    }

    public String getTeamPlayerDetails(String teamId, String token) {
        log.info("Requesting player details for team {}", teamId);
        return restTemplate.exchange(
            "https://supermanager.acb.com/api/basic/userteamplayer/" + teamId,
            HttpMethod.GET, new HttpEntity<>(addToken(token)), String.class).getBody();
    }

    public void cancelAllChanges(String teamId, String token) {
        log.info("Cancelling all pending changes for team {}", teamId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        restTemplate.exchange(
            "https://supermanager.acb.com/api/basic/userteamplayerchange/cancelAll/" + teamId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);
    }

    public void cancelPlayerChange(long idUserTeamPlayerChange, String token) {
        log.info("Cancelling pending player change {}", idUserTeamPlayerChange);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        String body = "{\"idUserTeamPlayerChange\":" + idUserTeamPlayerChange + "}";
        restTemplate.exchange(
            "https://supermanager.acb.com/api/basic/userteamplayerchange/cancel",
            HttpMethod.PUT,
            new HttpEntity<>(body, headers),
            String.class);
    }

    public void buyPlayer(String idUserTeam, long idPlayer, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        String body = "{\"idUserTeam\":" + idUserTeam + ",\"idPlayer\":" + idPlayer + ",\"action\":2}";
        log.info("Buying player — POST body: {}", body);
        ResponseEntity<String> response = restTemplate.exchange(
            "https://supermanager.acb.com/api/basic/userteamplayerchange/do",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            String.class);
        log.info("Buy player response: status={} body={}", response.getStatusCode(), response.getBody());
    }

    public void removePlayer(String idUserTeam, long idPlayer, String token) {
        log.info("Removing player {} from team {}", idPlayer, idUserTeam);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        String body = "{\"idUserTeam\":" + idUserTeam + ",\"idPlayer\":" + idPlayer + ",\"action\":1}";
        restTemplate.exchange(
            "https://supermanager.acb.com/api/basic/userteamplayerchange/do",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            String.class);
    }
}
