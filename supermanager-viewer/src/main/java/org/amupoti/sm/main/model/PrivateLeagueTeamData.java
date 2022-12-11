package org.amupoti.sm.main.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class PrivateLeagueTeamData extends UserTeamViewData {

    String user;
    String updatedAt;

    @Builder
    public PrivateLeagueTeamData(List<ViewerPlayer> playerList, Float score, Float computedScore, int usedPlayers, Float meanScorePerPlayer, Float scorePrediction, String cash, String teamBroker, String totalBroker, String teamUrl, String user, String updatedAt) {
        super(playerList, score, computedScore, usedPlayers, meanScorePerPlayer, scorePrediction, cash, teamBroker, totalBroker, teamUrl);
        this.user = user;
        this.updatedAt = updatedAt;
    }
}
