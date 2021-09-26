package org.amupoti.supermanager.parser.acb.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
@Getter
@Setter
@ToString
@Builder
public class SmTeam {

    private String name;
    private String apiUrl;
    private String webUrl;
    private List<SmPlayer> playerList;
    private Float score;
    private Float computedScore;
    private int usedPlayers;
    private Float meanScorePerPlayer;
    private Float scorePrediction;
    private int cash;
    private int teamBroker;
    private int totalBroker;

    public static String buildUrl(String teamId) {
        return "https://supermanager.acb.com/api/basic/userteamplayer/journeys/" + teamId;
    }

    public static String buildWebUrl(String teamId) {
        return "https://supermanager.acb.com/#/teams/detail/" + teamId;
    }
}
