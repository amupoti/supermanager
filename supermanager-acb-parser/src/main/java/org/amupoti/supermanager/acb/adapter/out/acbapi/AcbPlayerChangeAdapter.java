package org.amupoti.supermanager.acb.adapter.out.acbapi;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Out-adapter: buy, sell, cancel, and query pending player changes via the ACB API.
 */
@Component
@Slf4j
public class AcbPlayerChangeAdapter implements PlayerChangePort {

    private final RestTemplate restTemplate;

    public AcbPlayerChangeAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void buyPlayer(String userTeamId, long playerId, String token) {
        HttpHeaders headers = jsonAuthHeaders(token);
        String body = "{\"idUserTeam\":" + userTeamId + ",\"idPlayer\":" + playerId + ",\"action\":2}";
        log.info("Buying player — POST body: {}", body);
        var response = restTemplate.exchange(
                "https://supermanager.acb.com/api/basic/userteamplayerchange/do",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        log.info("Buy player response: status={} body={}", response.getStatusCode(), response.getBody());
    }

    @Override
    public void sellPlayer(String userTeamId, long playerId, String token) {
        log.info("Removing player {} from team {}", playerId, userTeamId);
        String body = "{\"idUserTeam\":" + userTeamId + ",\"idPlayer\":" + playerId + ",\"action\":1}";
        restTemplate.exchange(
                "https://supermanager.acb.com/api/basic/userteamplayerchange/do",
                HttpMethod.POST, new HttpEntity<>(body, jsonAuthHeaders(token)), String.class);
    }

    @Override
    public void cancelChange(long changeId, String token) {
        log.info("Cancelling pending player change {}", changeId);
        String body = "{\"idUserTeamPlayerChange\":" + changeId + "}";
        restTemplate.exchange(
                "https://supermanager.acb.com/api/basic/userteamplayerchange/cancel",
                HttpMethod.PUT, new HttpEntity<>(body, jsonAuthHeaders(token)), String.class);
    }

    @Override
    public void cancelAllChanges(String userTeamId, String token) {
        log.info("Cancelling all pending changes for team {}", userTeamId);
        restTemplate.exchange(
                "https://supermanager.acb.com/api/basic/userteamplayerchange/cancelAll/" + userTeamId,
                HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), String.class);
    }

    @Override
    public String getPendingChanges(String userTeamId, String token) {
        log.debug("Requesting pending changes for team {}", userTeamId);
        String filters = "[{\"field\":\"idUserTeam\",\"value\":" + userTeamId
                + ",\"operator\":\"=\",\"condition\":\"AND\"}]";
        return restTemplate.exchange(
                "https://supermanager.acb.com/api/basic/userteamplayerchange?_filters={filters}",
                HttpMethod.GET, new HttpEntity<>(bearerHeaders(token)), String.class, filters).getBody();
    }

    private HttpHeaders jsonAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }
}
