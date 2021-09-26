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
import org.amupoti.supermanager.parser.acb.dto.TeamsDescriptionResponse;
import org.amupoti.supermanager.parser.acb.dto.TeamsDetailsResponse;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.List;
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
        TeamsDetailsResponse teamsDetailsResponse = teamsDetailsPerCompetition.get(teamsDetailsPerCompetition.size() - 1);
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
        return SmPlayer.builder()
                .name(player.getShortName())
                .position(PlayerPosition.getFromNum(player.getPosition()).name())
                .score(player.getJourneyPoints())
                .status(SmPlayerStatus.builder().build())
                .marketData(playerMarketData.getPlayerMap(player.getShortName()))
                .build();
    }

    private SmPlayerStatus parseStatuses(List<String> statuses) {
        boolean active = true;
        if (statuses.contains("Icono de inactivo")) active = false;
        boolean spanish = false;
        if (statuses.contains("Icono de español")) spanish = true;
        boolean foreign = false;
        if (statuses.contains("Icono de extracomunitario")) foreign = true;
        boolean info = false;
        if (statuses.contains("Icono de más información")) info = true;
        boolean injured = false;
        if (statuses.contains("Icono de lesionado")) injured = true;

        return SmPlayerStatus.builder()
                .active(active)
                .spanish(spanish)
                .foreign(foreign)
                .injured(injured)
                .info(info)
                .build();

    }

    private void addTotalScore(SmTeam team, TeamsDetailsResponse teamDetails) {

        team.setScore(toFloat(teamDetails.getTotalStats().getTotalPoints()));
    }

    public List<SmTeam> getTeams(String response) throws IOException {

        if (response == null) throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        List<TeamsDescriptionResponse> teamsDescriptionList = objectMapper.readValue(response, new TypeReference<List<TeamsDescriptionResponse>>() {
        });
        //Get teams from first competition
        TeamsDescriptionResponse teamsDescriptionResponse = teamsDescriptionList.get(0);
        return teamsDescriptionResponse.getUserTeamList().stream()
                .map(team -> SmTeam.builder()
                        .name(team.getNameTeam())
                        .apiUrl(SmTeam.buildUrl(team.getIdUserTeam()))
                        .webUrl(SmTeam.buildWebUrl(team.getIdUserTeam()))
                        .teamBroker(NumberUtils.toInt(team.getBrokerValor()))
                        .cash(Float.valueOf(team.getAmount()).intValue())
                        .build())
                .collect(Collectors.toList());
    }


    public void checkGameStatus(String html) {
        String errorMessage = null;
        try {
            if (html != null && html.contains("mostrarMensajeModal")) {
                errorMessage = extractErrorMessage(html);
            }
        } catch (Exception ex) {
            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        }

        if (errorMessage != null) throw new SmException(errorMessage);
    }

    private String extractErrorMessage(String html) {
        return html.split("mostrarMensajeModal\\('")[1].split("'")[0];
    }

    public PlayerMarketData providePlayerData(String response) {

        try {
            PlayerMarketData playerMarketData = new PlayerMarketData();
            List<MarketPlayerResponse> playerList = objectMapper.readValue(response, new TypeReference<List<MarketPlayerResponse>>() {
            });
            playerList.forEach(player -> fillMarketData(player, playerMarketData));
            return playerMarketData;
        } catch (
                Exception e) {
            throw new SmException(ErrorCode.ERROR_PARSING_MARKET, e);
        }

    }

    private void fillMarketData(MarketPlayerResponse player, PlayerMarketData playerMarketData) {
        String playerName = player.getShortName();
        playerMarketData.addPlayer(playerName);
        playerMarketData.addPlayerData(playerName, PRICE.name(), player.getPrice());
        playerMarketData.addPlayerData(playerName, PLUS_15_BROKER.name(), player.getUp15());
        playerMarketData.addPlayerData(playerName, KEEP_BROKER.name(), player.getKeep());
        playerMarketData.addPlayerData(playerName, MEAN_VAL.name(), player.getCompetitionAverage());
        playerMarketData.addPlayerData(playerName, TEAM.name(), player.getNameTeam());

    }


}
