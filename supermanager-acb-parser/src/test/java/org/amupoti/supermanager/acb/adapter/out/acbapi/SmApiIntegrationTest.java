package org.amupoti.supermanager.acb.adapter.out.acbapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbAuthenticationAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbMarketDataAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbPlayerChangeAdapter;
import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbTeamDataAdapter;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.acb.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Integration tests verifying all SM API endpoints used by the application.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class SmApiIntegrationTest {

    private static final String USER     = "amupoti@gmail.com";
    private static final String PASSWORD = "1Contrasenyadelasmalas!";
    private static final int    TEAM_ID  = 168212;

    private static final String BASE_URL   = "https://supermanager.acb.com";
    private static final String BUY_URL    = BASE_URL + "/api/basic/userteamplayerchange/do";
    private static final String CANCEL_URL = BASE_URL + "/api/basic/userteamplayerchange/cancel";
    private static final String PENDING_CHANGES_URL = BASE_URL +
            "/api/basic/userteamplayerchange?_filters=%5B%7B%22field%22%3A%22idUserTeam%22" +
            "%2C%22value%22%3A" + TEAM_ID + "%2C%22operator%22%3A%22%3D%22%2C%22condition%22%3A%22AND%22%7D%5D";

    @Autowired private AcbAuthenticationAdapter acbAuthenticationAdapter;
    @Autowired private AcbTeamDataAdapter acbTeamDataAdapter;
    @Autowired private AcbMarketDataAdapter acbMarketDataAdapter;
    @Autowired private AcbPlayerChangeAdapter acbPlayerChangeAdapter;
    @Autowired private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();
    private String cachedToken;

    @Test
    public void testLogin() {
        String jwt = acbAuthenticationAdapter.authenticate(USER, PASSWORD);
        assertNotNull("JWT token must not be null", jwt);
    }

    @Test
    public void testGetTeamsPage() throws Exception {
        String token = authenticate();
        String json = fetchRaw(BASE_URL + "/api/basic/userteam/all", token);
        List<JsonNode> competitions = mapper.readValue(json, new TypeReference<>() {});
        assertFalse("Should have at least one competition", competitions.isEmpty());
        assertTrue("Test team " + TEAM_ID + " should be in the list",
                teamExistsInCompetitions(competitions, TEAM_ID));
    }

    @Test
    public void testMarketPageContainsFisicStatus() throws Exception {
        String token = authenticate();
        MarketData marketData = fetchMarketData(token);
        assertNotNull("Market data should not be null", marketData);
    }

    @Test
    public void testGetTeamJourneysAndParsePlayersWithMarketData() throws Exception {
        String token = authenticate();
        MarketData marketData = fetchMarketData(token);
        Team team = buildAndPopulateTeam(token, marketData);
        assertFalse("Team must have players", team.getPlayerList().isEmpty());
        for (Player player : team.getPlayerList()) {
            assertNotNull("Player name must not be null", player.getName());
            assertNotNull("Player status must not be null", player.getStatus());
        }
    }

    @Test
    public void testGetTeamPlayerDetailsAndMergeChangeIds() throws Exception {
        String token = authenticate();
        Team team = buildAndPopulateTeam(token, fetchMarketData(token));
        acbTeamDataAdapter.mergePlayerChangeIds(team, token);
        for (Player player : team.getPlayerList()) {
            assertTrue("idPlayer must be > 0 for " + player.getName(), player.getIdPlayer() > 0);
        }
    }

    @Test
    public void testBuyPlayerAndCancelChange() throws Exception {
        String token = authenticate();
        cancelAnyPendingChanges(token);
        JsonNode candidate = findCheapestBuyablePlayer(token);
        int idPlayer = candidate.path("idPlayer").asInt();

        JsonNode buyResult = buyPlayer(token, idPlayer);
        long idUserTeamPlayerChange = buyResult.path("idUserTeamPlayerChange").asLong();
        assertTrue("idUserTeamPlayerChange must be > 0 after buy", idUserTeamPlayerChange > 0);

        Thread.sleep(2000);
        JsonNode cancelResult = cancelPlayerChange(token, idUserTeamPlayerChange);
        assertTrue("Cancel must return a positive amount", cancelResult.path("amount").asDouble() > 0);
    }

    // --- helpers ---

    private String authenticate() {
        if (cachedToken == null) {
            cachedToken = acbAuthenticationAdapter.authenticate(USER, PASSWORD);
        }
        return cachedToken;
    }

    private String fetchRaw(String url, String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
    }

    private MarketData fetchMarketData(String token) {
        return acbMarketDataAdapter.getMarketData(token);
    }

    private Team buildAndPopulateTeam(String token, MarketData marketData) throws IOException {
        Team team = Team.builder()
                .name("Test1")
                .teamId(String.valueOf(TEAM_ID))
                .apiUrl("https://supermanager.acb.com/api/basic/userteamplayer/journeys/" + TEAM_ID)
                .webUrl("https://supermanager.acb.com/#/teams/detail/" + TEAM_ID)
                .build();
        acbTeamDataAdapter.populateTeam(team, marketData, token);
        return team;
    }

    private JsonNode findCheapestBuyablePlayer(String token) throws Exception {
        String marketJson = fetchRaw(
                "https://supermanager.acb.com/api/basic/player?_filters=[{\"field\":\"competition.idCompetition\",\"value\":1,\"operator\":\"=\",\"condition\":\"AND\"},{\"field\":\"edition.isActive\",\"value\":true,\"operator\":\"=\",\"condition\":\"AND\"}]&_page=1&_perPage=300",
                token);
        List<JsonNode> players = mapper.readValue(marketJson, new TypeReference<>() {});
        Set<Integer> teamPlayerIds = currentTeamPlayerIds(token);
        return players.stream()
                .filter(p -> "fit".equals(p.path("fisicStatus").asText()))
                .filter(p -> !teamPlayerIds.contains(p.path("idPlayer").asInt()))
                .min(Comparator.comparingDouble(p -> p.path("price").asDouble()))
                .orElseThrow(() -> new AssertionError("No suitable player found to buy"));
    }

    private Set<Integer> currentTeamPlayerIds(String token) throws IOException {
        String json = fetchRaw(BASE_URL + "/api/basic/userteamplayer/" + TEAM_ID, token);
        List<JsonNode> players = mapper.readValue(json, new TypeReference<>() {});
        Set<Integer> ids = new HashSet<>();
        players.forEach(p -> ids.add(p.path("idPlayer").asInt()));
        return ids;
    }

    private JsonNode buyPlayer(String token, int idPlayer) throws Exception {
        String body = "{\"idUserTeam\":" + TEAM_ID + ",\"idPlayer\":" + idPlayer + ",\"action\":2}";
        ResponseEntity<String> response = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                response = restTemplate.exchange(BUY_URL, HttpMethod.POST,
                        new HttpEntity<>(body, authHeaders(token)), String.class);
                break;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.FORBIDDEN && attempt < 4) {
                    Thread.sleep(3000);
                } else {
                    throw e;
                }
            }
        }
        assertNotNull("Buy should have succeeded", response);
        return mapper.readTree(response.getBody());
    }

    private JsonNode cancelPlayerChange(String token, long idUserTeamPlayerChange) throws IOException {
        String body = "{\"idUserTeamPlayerChange\":" + idUserTeamPlayerChange + "}";
        ResponseEntity<String> response = restTemplate.exchange(CANCEL_URL, HttpMethod.PUT,
                new HttpEntity<>(body, authHeaders(token)), String.class);
        assertEquals("Cancel should return 200", HttpStatus.OK, response.getStatusCode());
        return mapper.readTree(response.getBody());
    }

    private void cancelAnyPendingChanges(String token) {
        try {
            String json = acbPlayerChangeAdapter.getPendingChanges(String.valueOf(TEAM_ID), token);
            List<JsonNode> pending = mapper.readValue(json, new TypeReference<>() {});
            for (JsonNode change : pending) {
                long changeId = change.path("idUserTeamPlayerChange").asLong();
                if (changeId > 0) {
                    try { cancelPlayerChange(token, changeId); Thread.sleep(2000); } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private boolean teamExistsInCompetitions(List<JsonNode> competitions, int teamId) {
        return competitions.stream()
                .flatMap(c -> { List<JsonNode> teams = new ArrayList<>(); c.path("userTeamList").forEach(teams::add); return teams.stream(); })
                .anyMatch(t -> teamId == t.path("idUserTeam").asInt());
    }
}
