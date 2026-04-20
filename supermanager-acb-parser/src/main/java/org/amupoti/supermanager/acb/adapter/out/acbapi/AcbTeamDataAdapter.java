package org.amupoti.supermanager.acb.adapter.out.acbapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.TeamPlayerDetailResponse;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.TeamsDescriptionResponse;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.TeamsDetailsResponse;
import org.amupoti.supermanager.acb.application.port.out.MarketDataPort;
import org.amupoti.supermanager.acb.application.port.out.TeamDataPort;
import org.amupoti.supermanager.acb.domain.model.*;
import org.amupoti.supermanager.acb.exception.ErrorCode;
import org.amupoti.supermanager.acb.exception.SmException;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.amupoti.supermanager.acb.domain.model.MarketCategory.*;
import static org.amupoti.supermanager.acb.domain.model.PlayerPosition.getFromName;
import static org.amupoti.supermanager.acb.utils.DataUtils.toFloat;

/**
 * Out-adapter: fetches and parses team roster data from the ACB API.
 */
@Component
@Slf4j
public class AcbTeamDataAdapter implements TeamDataPort {

    @Value("${acb.url.teams:https://supermanager.acb.com/api/basic/userteam/all}")
    private String teamListUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AcbTeamDataAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable("teamsPage")
    public List<Team> getTeams(String user, String token) throws IOException {
        log.debug("Requesting all teams page for user {}", user);
        String response = restTemplate.exchange(
                teamListUrl, HttpMethod.GET, new HttpEntity<>(authHeader(token)), String.class).getBody();
        log.debug("userteam/all raw response: {}", response);
        return parseTeams(response);
    }

    @Override
    public void populateTeam(Team team, MarketData marketData, String token) throws IOException {
        String response;
        try {
            response = restTemplate.exchange(
                    team.getApiUrl(), HttpMethod.GET, new HttpEntity<>(authHeader(token)), String.class).getBody();
            log.debug("journeys raw response for team {}: {}", team.getName(), response);
        } catch (RestClientException e) {
            throw new SmException(ErrorCode.TEAM_PAGE_ERROR, e);
        }
        parseAndPopulateTeam(response, team, marketData);
    }

    @Override
    public void mergePlayerChangeIds(Team team, String token) {
        try {
            String json = restTemplate.exchange(
                    "https://supermanager.acb.com/api/basic/userteamplayer/" + team.getTeamId(),
                    HttpMethod.GET, new HttpEntity<>(authHeader(token)), String.class).getBody();
            log.debug("Requesting player details for team {}", team.getTeamId());
            mergeChangeIds(team, json);
        } catch (IOException e) {
            log.warn("Failed to merge player change IDs for team {}: {}", team.getName(), e.getMessage());
        }
    }

    // --- parsing ---

    private List<Team> parseTeams(String response) throws IOException {
        if (response == null) throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        List<TeamsDescriptionResponse> list = objectMapper.readValue(
                response, new TypeReference<List<TeamsDescriptionResponse>>() {});
        if (list.isEmpty()) throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        TeamsDescriptionResponse desc = list.get(0);
        if (desc.getUserTeamList() == null) {
            log.warn("ACB API: userTeamList is null — field may have been renamed");
            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        }
        return desc.getUserTeamList().stream()
                .map(team -> {
                    if (team.getIdUserTeam() == null) log.warn("ACB API: idUserTeam is null for team {}", team.getNameTeam());
                    if (team.getAmount() == null)     log.warn("ACB API: amount is null for team {}", team.getNameTeam());
                    return Team.builder()
                            .name(team.getNameTeam())
                            .teamId(team.getIdUserTeam())
                            .apiUrl("https://supermanager.acb.com/api/basic/userteamplayer/journeys/" + team.getIdUserTeam())
                            .webUrl("https://supermanager.acb.com/#/teams/detail/" + team.getIdUserTeam())
                            .teamBroker(NumberUtils.toInt(team.getBrokerValor()))
                            .cash(team.getAmount() != null ? Float.valueOf(team.getAmount()).intValue() : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void parseAndPopulateTeam(String response, Team team, MarketData marketData) throws IOException {
        List<TeamsDetailsResponse> perCompetition = objectMapper.readValue(
                response, new TypeReference<List<TeamsDetailsResponse>>() {});
        if (perCompetition.isEmpty()) throw new SmException(ErrorCode.TEAM_PAGE_ERROR);
        TeamsDetailsResponse details = perCompetition.get(perCompetition.size() - 1);
        if (details.getPlayerList() == null)
            log.warn("ACB API: playerList is null for team {} — field may have been renamed", team.getName());
        if (details.getTotalStats() == null)
            log.warn("ACB API: totalStats is null for team {} — field may have been renamed", team.getName());
        addPlayers(team, details, marketData);
        if (details.getTotalStats() != null) {
            team.setScore(toFloat(details.getTotalStats().getTotalPoints()));
        }
    }

    private void addPlayers(Team team, TeamsDetailsResponse details, MarketData marketData) {
        List<Player> players = details.getPlayerList().stream()
                .map(p -> buildPlayer(p, marketData))
                .sorted(Comparator.comparingInt(p -> getFromName(p.getPosition())))
                .collect(Collectors.toList());
        team.setPlayerList(players);
    }

    private Player buildPlayer(TeamsDetailsResponse.Player p, MarketData marketData) {
        if (p.getShortName() == null)
            log.warn("ACB API: player shortName is null — API response may have changed");
        Map<String, String> marketMap = marketData.getPlayerMap(p.getShortName());
        boolean injured = marketMap != null && "injured".equals(marketMap.get(FISIC_STATUS.name()));
        boolean spanish = marketMap != null && "true".equals(marketMap.get(IS_SPANISH.name()));
        boolean foreign = marketMap != null && "true".equals(marketMap.get(IS_FOREIGN.name()));
        return Player.builder()
                .name(p.getShortName())
                .position(PlayerPosition.getFromNum(p.getPosition()).getName())
                .score(p.getJourneyPoints())
                .status(PlayerStatus.builder().injured(injured).spanish(spanish).foreign(foreign).build())
                .marketData(marketMap)
                .idUserTeamPlayerChange(p.getIdUserTeamPlayerChange())
                .build();
    }

    private void mergeChangeIds(Team team, String json) throws IOException {
        List<TeamPlayerDetailResponse> details = objectMapper.readValue(
                json, new TypeReference<List<TeamPlayerDetailResponse>>() {});
        Map<String, TeamPlayerDetailResponse> byName = details.stream()
                .filter(d -> d.getShortName() != null)
                .collect(Collectors.toMap(TeamPlayerDetailResponse::getShortName, d -> d, (a, b) -> a));
        team.getPlayerList().forEach(player -> {
            TeamPlayerDetailResponse detail = byName.get(player.getName());
            if (detail != null) {
                player.setIdUserTeamPlayerChange(detail.getIdUserTeamPlayerChange());
                player.setIdPlayer(detail.getIdPlayer());
            }
        });
        long changesUsed = details.stream()
                .filter(d -> "new".equals(d.getStatusTeamSquad()))
                .count();
        team.setChangesUsed((int) changesUsed);
        team.setMaxChanges(3);
        log.debug("Team {}: changesUsed={}, maxChanges=3", team.getName(), changesUsed);
    }

    private MultiValueMap<String, String> authHeader(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }
}
