package org.amupoti.sm.main.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by amupoti on 01/08/2017.
 */
@Getter
@AllArgsConstructor
public class UserTeamViewData {
    private final List<ViewerPlayer> playerList;
    private final Float score;
    private final Float computedScore;
    private final int usedPlayers;
    private final Float meanScorePerPlayer;
    private final Float scorePrediction;
    private String cash;
    private String teamBroker;
    private String totalBroker;
    private String teamUrl;
}
