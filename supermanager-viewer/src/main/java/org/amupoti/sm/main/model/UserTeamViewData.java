package org.amupoti.sm.main.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Created by amupoti on 01/08/2017.
 */
@Getter
@Builder
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
}
