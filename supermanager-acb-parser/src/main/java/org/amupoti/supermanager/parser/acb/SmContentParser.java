package org.amupoti.supermanager.parser.acb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.PlayerPosition;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;
import org.amupoti.supermanager.parser.acb.beans.SmPlayerStatus;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;
import org.amupoti.supermanager.parser.acb.dto.MarketPlayerResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamPlayerDetailResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamsDescriptionResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamsDetailsResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.amupoti.supermanager.parser.acb.beans.PlayerPosition.getFromName;
import static org.amupoti.supermanager.parser.acb.beans.market.MarketCategory.*;
import static org.amupoti.supermanager.parser.acb.utils.DataUtils.toFloat;

/**
 * Created by amupoti on 28/08/2017.
 */
@Slf4j
public class SmContentParser {

    private ObjectMapper objectMapper = new ObjectMapper();

    public void populateTeam(String response, SmTeam team, PlayerMarketData playerMarketData) throws IOException {
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
        addPlayers(team, teamsDetailsResponse, playerMarketData);
        addTotalScore(team, teamsDetailsResponse);
    }

    private void addPlayers(SmTeam team, TeamsDetailsResponse teamsDescriptionResponse, PlayerMarketData playerMarketData) {

        List<SmPlayer> players = teamsDescriptionResponse.getPlayerList().stream()
                .map(player -> buildPlayer(player, playerMarketData))
                .sorted(this::comparePosition)
                .collect(Collectors.toList());

        team.setPlayerList(players);
    }

    private int comparePosition(SmPlayer p1, SmPlayer p2) {
        int pos1 = getFromName(p1.getPosition());
        int pos2 = getFromName(p2.getPosition());
        return Integer.compare(pos1, pos2);
    }

    private SmPlayer buildPlayer(TeamsDetailsResponse.Player player, PlayerMarketData playerMarketData) {
        if (player.getShortName() == null) {
            log.warn("ACB API: player shortName is null — API response may have changed");
        }
        Map<String, String> marketMap = playerMarketData.getPlayerMap(player.getShortName());
        boolean injured = marketMap != null && "injured".equals(marketMap.get(FISIC_STATUS.name()));
        boolean spanish = marketMap != null && "true".equals(marketMap.get(IS_SPANISH.name()));
        boolean foreign = marketMap != null && "true".equals(marketMap.get(IS_FOREIGN.name()));
        return SmPlayer.builder()
                .name(player.getShortName())
                .position(PlayerPosition.getFromNum(player.getPosition()).getName())
                .score(player.getJourneyPoints())
                .status(SmPlayerStatus.builder().injured(injured).spanish(spanish).foreign(foreign).build())
                .marketData(marketMap)
                .idUserTeamPlayerChange(player.getIdUserTeamPlayerChange())
                .build();
    }

    private void addTotalScore(SmTeam team, TeamsDetailsResponse teamDetails) {
        if (teamDetails.getTotalStats() != null) {
            team.setScore(toFloat(teamDetails.getTotalStats().getTotalPoints()));
        }
    }

    public List<SmTeam> getTeams(String response) throws IOException {

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
                    SmTeam.SmTeamBuilder builder = SmTeam.builder()
                            .name(team.getNameTeam())
                            .teamId(team.getIdUserTeam())
                            .apiUrl(SmTeam.buildUrl(team.getIdUserTeam()))
                            .webUrl(SmTeam.buildWebUrl(team.getIdUserTeam()))
                            .teamBroker(NumberUtils.toInt(team.getBrokerValor()))
                            .cash(team.getAmount() != null ? Float.valueOf(team.getAmount()).intValue() : 0);
                    SmTeam smTeam = builder.build();
                    return smTeam;
                })
                .collect(Collectors.toList());
    }

    public PlayerMarketData providePlayerData(String response) {
        try {
            PlayerMarketData playerMarketData = new PlayerMarketData();
            List<MarketPlayerResponse> playerList = objectMapper.readValue(response, new TypeReference<List<MarketPlayerResponse>>() {
            });
            log.info("Market API returned {} players", playerList.size());
            if (!playerList.isEmpty()) {
                MarketPlayerResponse sample = playerList.get(0);
                log.info("Market sample player: name={} spanish={} foreign={} position={} idPlayer={}",
                        sample.getShortName(), sample.isSpanish(), sample.isForeign(),
                        sample.getPosition(), sample.getIdPlayer());
                // Log raw JSON of first player to verify field names
                int firstEnd = response.indexOf('}');
                log.info("Market first player raw JSON: {}",
                        firstEnd > 0 ? response.substring(1, firstEnd + 1) : response.substring(0, Math.min(300, response.length())));
            }
            long spanishInMarket = playerList.stream().filter(MarketPlayerResponse::isSpanish).count();
            long foreignInMarket = playerList.stream().filter(MarketPlayerResponse::isForeign).count();
            log.info("Market nationality summary: {} Spanish, {} foreign out of {} total",
                    spanishInMarket, foreignInMarket, playerList.size());
            // Log license codes from non-national players to identify the foreign indicator
            playerList.stream()
                    .filter(p -> !p.isSpanish())
                    .limit(10)
                    .forEach(p -> log.info("Non-national player: name={} license={} nationality={}",
                            p.getShortName(), p.getLicense(), p.getNationality()));
            playerList.forEach(player -> fillMarketData(player, playerMarketData));
            return playerMarketData;
        } catch (Exception e) {
            throw new SmException(ErrorCode.ERROR_PARSING_MARKET, e);
        }
    }

    private void fillMarketData(MarketPlayerResponse player, PlayerMarketData playerMarketData) {
        if (player.getShortName() == null) {
            log.warn("ACB API: market player has null shortName — skipping");
            return;
        }
        if (player.getPrice() == null) {
            log.warn("ACB API: market player {} has null price — API field may have changed", player.getShortName());
        }
        String playerName = player.getShortName();
        playerMarketData.addPlayer(playerName);
        playerMarketData.addPlayerData(playerName, PRICE.name(), player.getPrice());
        playerMarketData.addPlayerData(playerName, PRICE_FORMATTED.name(), formatPrice(player.getPrice()));
        playerMarketData.addPlayerData(playerName, PLUS_15_BROKER.name(), player.getUp15());
        playerMarketData.addPlayerData(playerName, KEEP_BROKER.name(), player.getKeep());
        playerMarketData.addPlayerData(playerName, MEAN_VAL.name(), player.getCompetitionAverage());
        playerMarketData.addPlayerData(playerName, TEAM.name(), player.getNameTeam());
        playerMarketData.addPlayerData(playerName, FISIC_STATUS.name(), player.getFisicStatus());
        playerMarketData.addPlayerData(playerName, ID_PLAYER.name(), String.valueOf(player.getIdPlayer()));
        playerMarketData.addPlayerData(playerName, POSITION.name(), toPositionName(player.getPosition()));
        playerMarketData.addPlayerData(playerName, IS_SPANISH.name(), String.valueOf(player.isSpanish()));
        playerMarketData.addPlayerData(playerName, IS_FOREIGN.name(), String.valueOf(player.isForeign()));
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
     * Merges idPlayer and idUserTeamPlayerChange from the direct player endpoint into each SmPlayer,
     * and computes changesUsed / maxChanges from the statusTeamSquad field.
     *
     * statusTeamSquad == "new" means the player was acquired via a change this season.
     * The game allows a maximum of 3 changes; this is a fixed rule not returned by the API.
     */
    public void mergePlayerChangeIds(SmTeam team, String playerDetailsJson) throws IOException {
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
        log.info("Team {}: changesUsed={} (from statusTeamSquad=new), maxChanges=3", team.getName(), changesUsed);
    }
}
