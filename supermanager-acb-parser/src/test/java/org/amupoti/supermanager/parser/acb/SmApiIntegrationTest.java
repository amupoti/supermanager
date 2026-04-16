package org.amupoti.supermanager.parser.acb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.parser.acb.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Integration tests verifying all SM API endpoints used by the application.
 *
 * Uses the test account testsm_testsm@mailinator.com / testsm_testsm@mailinator.comT1
 * (team ID 283709 - "Test1").
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

    @Autowired private SmContentProvider smContentProvider;
    @Autowired private SmContentParser   smContentParser;
    @Autowired private RestTemplate      restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Shared token — obtained once per test run to avoid throttling the auth endpoint. */
    private String cachedToken;

    // =========================================================================
    // Tests
    // =========================================================================

    @Test
    public void testLogin() {
        // when
        String jwt = smContentProvider.authenticateUser(USER, PASSWORD).getJwt();

        // then
        assertNotNull("JWT token must not be null", jwt);
    }

    @Test
    public void testGetTeamsPage() throws Exception {
        // given
        String token = authenticate();

        // when
        List<JsonNode> competitions = fetchCompetitions(token);

        // then
        assertFalse("Should have at least one competition", competitions.isEmpty());
        assertTrue("Test team " + TEAM_ID + " should be in the list",
                teamExistsInCompetitions(competitions, TEAM_ID));
    }

    @Test
    public void testMarketPageContainsFisicStatus() throws Exception {
        // given
        String token = authenticate();

        // when
        List<JsonNode> players = fetchMarketPlayers(token);

        // then
        assertFalse("Market should have players", players.isEmpty());
        players.forEach(p -> assertTrue(
                "Player " + p.path("shortName").asText() + " missing fisicStatus",
                p.has("fisicStatus")));
        assertTrue("Expected at least one injured player in the market",
                players.stream().anyMatch(p -> "injured".equals(p.path("fisicStatus").asText())));
    }

    @Test
    public void testGetTeamJourneysAndParsePlayersWithMarketData() throws Exception {
        // given
        String token = authenticate();
        MarketData marketData = fetchMarketData(token);

        // when
        Team team = buildAndPopulateTeam(token, marketData);

        // then
        assertFalse("Team must have players", team.getPlayerList().isEmpty());
        for (Player player : team.getPlayerList()) {
            assertNotNull("Player name must not be null", player.getName());
            assertNotNull("Player status must not be null", player.getStatus());
        }
    }

    @Test
    public void testGetTeamPlayerDetailsAndMergeChangeIds() throws Exception {
        // given
        String token = authenticate();
        Team team = buildAndPopulateTeam(token, fetchMarketData(token));

        // when
        String playerDetails = smContentProvider.getTeamPlayerDetails(String.valueOf(TEAM_ID), token);
        smContentParser.mergePlayerChangeIds(team, playerDetails);

        // then
        for (Player player : team.getPlayerList()) {
            assertTrue(
                    "idUserTeamPlayerChange must be > 0 for " + player.getName(),
                    player.getIdUserTeamPlayerChange() > 0);
            assertTrue(
                    "idPlayer must be > 0 for " + player.getName(),
                    player.getIdPlayer() > 0);
        }
    }

    @Test
    public void testBuyPlayerAndCancelChange() throws Exception {
        // given
        String token = authenticate();
        cancelAnyPendingChanges(token);
        JsonNode candidate = findCheapestBuyablePlayer(token);
        int idPlayer = candidate.path("idPlayer").asInt();

        // when — buy
        JsonNode buyResult = buyPlayer(token, idPlayer);

        // then — buy succeeded
        long idUserTeamPlayerChange = buyResult.path("idUserTeamPlayerChange").asLong();
        assertTrue("idUserTeamPlayerChange must be > 0 after buy", idUserTeamPlayerChange > 0);
        assertEquals("buy result must reference the correct team",  TEAM_ID, buyResult.path("idUserTeam").asInt());
        assertEquals("buy result must reference the correct player", idPlayer, buyResult.path("idPlayer").asInt());
        assertTrue("amount after buy must be positive", buyResult.path("amount").asDouble() > 0);

        // when — cancel
        Thread.sleep(2000); // let the buy settle before cancelling
        JsonNode cancelResult = cancelPlayerChange(token, idUserTeamPlayerChange);

        // then — cancel succeeded
        assertTrue("Cancel must return a positive amount", cancelResult.path("amount").asDouble() > 0);
    }

    // =========================================================================
    // Private helpers — authentication & data fetching
    // =========================================================================

    private String authenticate() {
        if (cachedToken == null) {
            cachedToken = smContentProvider.authenticateUser(USER, PASSWORD).getJwt();
        }
        return cachedToken;
    }

    private List<JsonNode> fetchCompetitions(String token) throws IOException {
        String json = smContentProvider.getTeamsPage(USER, token);
        return mapper.readValue(json, new TypeReference<>() {});
    }

    /** Fetches market players, retrying up to 3 times on transient API errors. */
    private List<JsonNode> fetchMarketPlayers(String token) throws Exception {
        String json = fetchMarketPageWithRetry(token);
        return mapper.readValue(json, new TypeReference<>() {});
    }

    private MarketData fetchMarketData(String token) throws Exception {
        return smContentParser.providePlayerData(fetchMarketPageWithRetry(token));
    }

    private String fetchMarketPageWithRetry(String token) throws Exception {
        Exception last = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return smContentProvider.getMarketPage(token);
            } catch (Exception e) {
                last = e;
                if (attempt < 2) Thread.sleep(3000);
            }
        }
        throw last;
    }

    private Team buildAndPopulateTeam(String token, MarketData marketData) throws IOException {
        Team team = Team.builder()
                .name("Test1")
                .apiUrl("https://supermanager.acb.com/api/basic/userteamplayerjourney?_filters=[{\"field\":\"idUserTeam\",\"value\":" + TEAM_ID + ",\"operator\":\"=\",\"condition\":\"AND\"}]")
                .webUrl("https://supermanager.acb.com/equipo/" + TEAM_ID)
                .build();
        smContentParser.populateTeam(smContentProvider.getTeamPage(team, token), team, marketData);
        return team;
    }

    // =========================================================================
    // Private helpers — buy / cancel
    // =========================================================================

    private JsonNode findCheapestBuyablePlayer(String token) throws Exception {
        Set<Integer> teamPlayerIds = currentTeamPlayerIds(token);
        return fetchMarketPlayers(token).stream()
                .filter(p -> "fit".equals(p.path("fisicStatus").asText()))
                .filter(p -> !teamPlayerIds.contains(p.path("idPlayer").asInt()))
                .min(Comparator.comparingDouble(p -> p.path("price").asDouble()))
                .orElseThrow(() -> new AssertionError("No suitable player found to buy"));
    }

    private Set<Integer> currentTeamPlayerIds(String token) throws IOException {
        String json = smContentProvider.getTeamPlayerDetails(String.valueOf(TEAM_ID), token);
        List<JsonNode> players = mapper.readValue(json, new TypeReference<>() {});
        Set<Integer> ids = new HashSet<>();
        players.forEach(p -> ids.add(p.path("idPlayer").asInt()));
        return ids;
    }

    /** Buys a player, retrying up to 5 times on 403 throttle responses. */
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

    // =========================================================================
    // Exploration test — discover which API fields carry the changes counter
    // =========================================================================

    /** Real team ID visible at https://supermanager.acb.com/teams/detail/168212 — has actual changes. */
    private static final int REAL_TEAM_ID = TEAM_ID;

    /**
     * Hits every endpoint relevant to the changes counter and prints a summary of all fields
     * whose name contains "cambio", "change", "max", or "used" (case-insensitive).
     * Also probes REAL_TEAM_ID (a real team with known changes) alongside the test team.
     * Run this test and inspect stdout to identify the exact field names.
     */
    @Test
    public void exploreChangesCounterFields() throws Exception {
        String token = authenticate();

        System.out.println("\n========== ENDPOINT: userteam/all ==========");
        String teamsJson = smContentProvider.getTeamsPage(USER, token);
        List<JsonNode> competitions = mapper.readValue(teamsJson, new TypeReference<>() {});
        for (JsonNode competition : competitions) {
            JsonNode teamList = competition.path("userTeamList");
            if (teamList.isArray()) {
                for (JsonNode team : teamList) {
                    int id = team.path("idUserTeam").asInt();
                    if (id == TEAM_ID || id == REAL_TEAM_ID) {
                        System.out.println("  Team " + id + " full JSON: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(team));
                        printCandidateFields("userteam/all team " + id, team);
                    }
                }
            }
            printCandidateFields("userteam/all competition entry", competition);
        }

        for (int teamId : new int[]{TEAM_ID, REAL_TEAM_ID}) {
            System.out.println("\n========== ENDPOINT: journeys for team " + teamId + " ==========");
            Team smTeam = Team.builder().name("team-" + teamId)
                    .apiUrl("https://supermanager.acb.com/api/basic/userteamplayerjourney?_filters=[{\"field\":\"idUserTeam\",\"value\":" + teamId + ",\"operator\":\"=\",\"condition\":\"AND\"}]")
                    .webUrl("https://supermanager.acb.com/equipo/" + teamId).build();
            String journeysJson = smContentProvider.getTeamPage(smTeam, token);
            List<JsonNode> journeys = mapper.readValue(journeysJson, new TypeReference<>() {});
            // Print only the last journey (current round) in full
            if (!journeys.isEmpty()) {
                JsonNode last = journeys.get(journeys.size() - 1);
                System.out.println("  Last journey full JSON: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(last));
                journeys.forEach(j -> printCandidateFields("journey[" + j.path("number").asInt() + "] team " + teamId, j));
            }

            System.out.println("\n========== ENDPOINT: userteamplayer/" + teamId + " ==========");
            String playerDetailsJson = smContentProvider.getTeamPlayerDetails(String.valueOf(teamId), token);
            List<JsonNode> playerDetails = mapper.readValue(playerDetailsJson, new TypeReference<>() {});
            if (!playerDetails.isEmpty()) {
                System.out.println("  First player full JSON: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(playerDetails.get(0)));
                playerDetails.forEach(pd -> printCandidateFields("playerDetail[" + pd.path("shortName").asText() + "] team " + teamId, pd));
            }

            System.out.println("\n========== ENDPOINT: pendingChanges for team " + teamId + " ==========");
            try {
                String pendingJson = smContentProvider.getPendingChanges(String.valueOf(teamId), token);
                System.out.println("  Raw: " + pendingJson);
                List<JsonNode> pending = mapper.readValue(pendingJson, new TypeReference<>() {});
                pending.forEach(c -> printCandidateFields("pendingChange team " + teamId, c));
                if (pending.isEmpty()) System.out.println("  (empty list)");
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
        }

        System.out.println("\n========== SUMMARY ==========");
        System.out.println("Check output above for any field matching cambio/change/max/used.");
    }

    /**
     * Walks all fields of the given node and prints those whose name matches keywords
     * related to the changes counter.
     */
    private void printCandidateFields(String context, JsonNode node) {
        node.fields().forEachRemaining(entry -> {
            String key = entry.getKey().toLowerCase();
            if (key.contains("cambio") || key.contains("change") || key.contains("max") || key.contains("used")) {
                System.out.printf("  [%s] %s = %s%n", context, entry.getKey(), entry.getValue());
            }
        });
    }

    /** Best-effort cleanup of stuck pending changes from previous test runs. */
    private void cancelAnyPendingChanges(String token) {
        try {
            ResponseEntity<String> resp = restTemplate.exchange(PENDING_CHANGES_URL, HttpMethod.GET,
                    new HttpEntity<>(authHeaders(token)), String.class);
            List<JsonNode> pending = mapper.readValue(resp.getBody(), new TypeReference<>() {});
            for (JsonNode change : pending) {
                long changeId = change.path("idUserTeamPlayerChange").asLong();
                if (changeId > 0) {
                    try {
                        cancelPlayerChange(token, changeId);
                        Thread.sleep(2000);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // Private helpers — misc
    // =========================================================================

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
