package org.amupoti.supermanager.viewer.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

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
