package org.amupoti.supermanager.acb.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Domain aggregate representing a user's SuperManager team.
 * URL construction is intentionally absent — that is an adapter concern.
 */
@Getter
@Setter
@ToString
@Builder
public class Team {
    private String name;
    private String teamId;
    private String apiUrl;
    private String webUrl;
    private List<Player> playerList;
    private Float score;
    private Float computedScore;
    private int usedPlayers;
    private Float meanScorePerPlayer;
    private Float scorePrediction;
    private int cash;
    private int teamBroker;
    private int totalBroker;
    /** Best affordable buyable candidate per position ("B"/"A"/"P"). */
    @Setter private Map<String, Player> candidatesByPosition;
    @Setter private int changesUsed;
    @Setter private int maxChanges;
}
