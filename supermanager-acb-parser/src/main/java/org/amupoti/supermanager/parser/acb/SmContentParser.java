package org.amupoti.supermanager.parser.acb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.PlayerPosition;
import org.amupoti.supermanager.acb.domain.model.PlayerStatus;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.parser.acb.dto.MarketPlayerResponse;
import org.amupoti.supermanager.parser.acb.dto.PlayerStatsResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamPlayerDetailResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamsDescriptionResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamsDetailsResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.amupoti.supermanager.acb.domain.model.PlayerPosition.getFromName;
import static org.amupoti.supermanager.acb.domain.model.MarketCategory.*;
import static org.amupoti.supermanager.parser.acb.utils.DataUtils.toFloat;

/**
 * Created by amupoti on 28/08/2017.
 */
@Slf4j
public class SmContentParser {

    private ObjectMapper objectMapper = new ObjectMapper();

    public void populateTeam(String response, Team team, MarketData marketData) throws IOException {
        List<TeamsDetailsResponse> teamsDetailsPerCompetition = objectMapper.readValue(response, new TypeReference<List<TeamsDetailsResponse>>() {
        });
        if (teamsDetailsPerCompetition.isEmpty()) throw new SmException(ErrorCode.TEAM_PAGE_ERROR);
        TeamsDetailsResponse teamsDetailsResponse = teamsDetailsPerCompetition.get(teamsDetailsPerCompetition.size() - 1);
        if (teamsDetailsResponse.getPlayerList() == null) {
            log.warn("ACB API: playerList is null for team {} — field may have been renamed", team.getName());
        }
        if (teamsDetailsResponse.getTotalStats() == null) {
            log.warn("ACB API: totalStats is null for team {} — field may have been renamed", team.getName());
        }
        addPlayers(team, teamsDetailsResponse, marketData);
        addTotalScore(team, teamsDetailsResponse);
    }

    /**
     * Parses the JSON from /api/basic/playerstats/1/{idPlayer} and returns the
     * average SuperManager score (including 20% win bonus where applicable) across
     * the last 4 completed matches, formatted to one decimal place.
     * Returns null if no played matches are found.
     */
    public String computeLastFourAverage(String playerStatsJson) throws IOException {
        PlayerStatsResponse stats = objectMapper.readValue(playerStatsJson, PlayerStatsResponse.class);
        if (stats.getPlayerStats() == null) return null;

        // The list is ordered newest-first. Filter to entries with a completed match
        // (idJourney present), take the first 4, and compute average.
        List<Double> scores = stats.getPlayerStats().stream()
                .filter(s -> s.getIdJourney() != null)
                .sorted(Comparator.comparingInt(PlayerStatsResponse.JourneyStats::getNumberJourney).reversed())
                .limit(4)
                .map(s -> {
                    // bonusVictory > 0 means the 20% win bonus was applied (total score).
                    // When 0, use pointsJourney (team lost or val ≤ 0).
                    double bonusVictory = s.getBonusVictory() != null ? s.getBonusVictory() : 0.0;
                    double points      = s.getPointsJourney() != null ? s.getPointsJourney() : 0.0;
                    return bonusVictory > 0 ? bonusVictory : points;
                })
                .collect(Collectors.toList());

        if (scores.isEmpty()) return null;
        double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return String.format("%.1f", avg);
    }

    private void addPlayers(Team team, TeamsDetailsResponse teamsDescriptionResponse, MarketData marketData) {

        List<Player> players = teamsDescriptionResponse.getPlayerList().stream()
                .map(player -> buildPlayer(player, marketData))
                .sorted(this::comparePosition)
                .collect(Collectors.toList());

        team.setPlayerList(players);
    }

    private int comparePosition(Player p1, Player p2) {
        int pos1 = getFromName(p1.getPosition());
        int pos2 = getFromName(p2.getPosition());
        return Integer.compare(pos1, pos2);
    }

    private Player buildPlayer(TeamsDetailsResponse.Player player, MarketData marketData) {
        if (player.getShortName() == null) {
            log.warn("ACB API: player shortName is null — API response may have changed");
        }
        Map<String, String> marketMap = marketData.getPlayerMap(player.getShortName());
        boolean injured = marketMap != null && "injured".equals(marketMap.get(FISIC_STATUS.name()));
        boolean spanish = marketMap != null && "true".equals(marketMap.get(IS_SPANISH.name()));
        boolean foreign = marketMap != null && "true".equals(marketMap.get(IS_FOREIGN.name()));
        return Player.builder()
                .name(player.getShortName())
                .position(PlayerPosition.getFromNum(player.getPosition()).getName())
                .score(player.getJourneyPoints())
                .status(PlayerStatus.builder().injured(injured).spanish(spanish).foreign(foreign).build())
                .marketData(marketMap)
                .idUserTeamPlayerChange(player.getIdUserTeamPlayerChange())
                .build();
    }

    private void addTotalScore(Team team, TeamsDetailsResponse teamDetails) {
        if (teamDetails.getTotalStats() != null) {
            team.setScore(toFloat(teamDetails.getTotalStats().getTotalPoints()));
        }
    }

    public List<Team> getTeams(String response) throws IOException {

        if (response == null) throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        List<TeamsDescriptionResponse> teamsDescriptionList = objectMapper.readValue(response, new TypeReference<List<TeamsDescriptionResponse>>() {
        });
        if (teamsDescriptionList.isEmpty()) throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        TeamsDescriptionResponse teamsDescriptionResponse = teamsDescriptionList.get(0);
        if (teamsDescriptionResponse.getUserTeamList() == null) {
            log.warn("ACB API: userTeamList is null — field may have been renamed");
            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        }
        return teamsDescriptionResponse.getUserTeamList().stream()
                .map(team -> {
                    if (team.getIdUserTeam() == null) log.warn("ACB API: idUserTeam is null for team {}", team.getNameTeam());
                    if (team.getAmount() == null) log.warn("ACB API: amount is null for team {}", team.getNameTeam());
                    Team.TeamBuilder builder = Team.builder()
                            .name(team.getNameTeam())
                            .teamId(team.getIdUserTeam())
                            .apiUrl("https://supermanager.acb.com/api/basic/userteamplayer/journeys/" + team.getIdUserTeam())
                            .webUrl("https://supermanager.acb.com/#/teams/detail/" + team.getIdUserTeam())
                            .teamBroker(NumberUtils.toInt(team.getBrokerValor()))
                            .cash(team.getAmount() != null ? Float.valueOf(team.getAmount()).intValue() : 0);
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    public MarketData providePlayerData(String response) {
        try {
            MarketData marketData = new MarketData();
            List<MarketPlayerResponse> playerList = objectMapper.readValue(response, new TypeReference<List<MarketPlayerResponse>>() {
            });
            log.debug("Market API returned {} players", playerList.size());
            if (!playerList.isEmpty()) {
                MarketPlayerResponse sample = playerList.get(0);
                log.debug("Market sample player: name={} spanish={} foreign={} position={} idPlayer={}",
                        sample.getShortName(), sample.isSpanish(), sample.isForeign(),
                        sample.getPosition(), sample.getIdPlayer());
                // Log raw JSON of first player at debug level
                int firstEnd = response.indexOf('}');
                log.debug("Market first player raw JSON: {}",
                        firstEnd > 0 ? response.substring(1, firstEnd + 1) : response.substring(0, Math.min(500, response.length())));
                if (!sample.getUnknownFields().isEmpty()) {
                    log.debug("Market first player unknown fields (not yet mapped): {}", sample.getUnknownFields());
                }
            }
            long spanishInMarket = playerList.stream().filter(MarketPlayerResponse::isSpanish).count();
            long foreignInMarket = playerList.stream().filter(MarketPlayerResponse::isForeign).count();
            log.debug("Market nationality summary: {} Spanish, {} foreign out of {} total",
                    spanishInMarket, foreignInMarket, playerList.size());
            // Log license codes from non-national players to identify the foreign indicator
            playerList.stream()
                    .filter(p -> !p.isSpanish())
                    .limit(10)
                    .forEach(p -> log.debug("Non-national player: name={} license={} nationality={}",
                            p.getShortName(), p.getLicense(), p.getNationality()));
            playerList.forEach(player -> fillMarketData(player, marketData));
            return marketData;
        } catch (Exception e) {
            throw new SmException(ErrorCode.ERROR_PARSING_MARKET, e);
        }
    }

    private void fillMarketData(MarketPlayerResponse player, MarketData marketData) {
        if (player.getShortName() == null) {
            log.warn("ACB API: market player has null shortName — skipping");
            return;
        }
        if (player.getPrice() == null) {
            log.warn("ACB API: market player {} has null price — API field may have changed", player.getShortName());
        }
        String playerName = player.getShortName();
        marketData.addPlayer(playerName);
        marketData.addPlayerData(playerName, PRICE.name(), player.getPrice());
        marketData.addPlayerData(playerName, PRICE_FORMATTED.name(), formatPrice(player.getPrice()));
        marketData.addPlayerData(playerName, PLUS_15_BROKER.name(), player.getUp15());
        marketData.addPlayerData(playerName, KEEP_BROKER.name(), player.getKeep());
        marketData.addPlayerData(playerName, MEAN_VAL.name(), player.getCompetitionAverage());
        marketData.addPlayerData(playerName, TEAM.name(), player.getNameTeam());
        marketData.addPlayerData(playerName, FISIC_STATUS.name(), player.getFisicStatus());
        marketData.addPlayerData(playerName, ID_PLAYER.name(), String.valueOf(player.getIdPlayer()));
        marketData.addPlayerData(playerName, POSITION.name(), toPositionName(player.getPosition()));
        marketData.addPlayerData(playerName, IS_SPANISH.name(), String.valueOf(player.isSpanish()));
        marketData.addPlayerData(playerName, IS_FOREIGN.name(), String.valueOf(player.isForeign()));
    }

    /** Converts a position number from the API ("1", "3", "5") to its short name ("B", "A", "P"). */
    private String toPositionName(String position) {
        if (position == null) return null;
        try {
            PlayerPosition pos = PlayerPosition.getFromNum(position);
            return pos != null ? pos.getName() : null;
        } catch (Exception e) {
            log.warn("Unknown position value from market API: {}", position);
            return null;
        }
    }

    private String formatPrice(String price) {
        if (price == null) return "0k";
        return (Float.valueOf(price).intValue() / 1000) + "k";
    }

    /**
     * Merges idPlayer and idUserTeamPlayerChange from the direct player endpoint into each Player,
     * and computes changesUsed / maxChanges from the statusTeamSquad field.
     *
     * statusTeamSquad == "new" means the player was acquired via a change this season.
     * The game allows a maximum of 3 changes; this is a fixed rule not returned by the API.
     */
    public void mergePlayerChangeIds(Team team, String playerDetailsJson) throws IOException {
        List<TeamPlayerDetailResponse> details = objectMapper.readValue(
            playerDetailsJson, new TypeReference<List<TeamPlayerDetailResponse>>() {});
        Map<String, TeamPlayerDetailResponse> byName = details.stream()
            .filter(d -> d.getShortName() != null)
            .collect(java.util.stream.Collectors.toMap(
                TeamPlayerDetailResponse::getShortName,
                d -> d,
                (a, b) -> a));
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
        log.debug("Team {}: changesUsed={} (from statusTeamSquad=new), maxChanges=3", team.getName(), changesUsed);
    }
}
