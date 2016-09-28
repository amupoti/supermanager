package org.amupoti.sm.main.services.provider.team;

import org.amupoti.sm.main.bean.PlayerPosition;

/**
 * Created by Marcel on 28/09/2015.
 */
public interface TeamDataService {


    String getTeamMean(String teamName, PlayerPosition position);

    String getTeamMeanReceived(String teamName, PlayerPosition position);

    String getTeamMeanLocal(String teamName, PlayerPosition position);

    String getTeamMeanVisitor(String teamName, PlayerPosition position);

    String getTeamMeanLocalReceived(String teamName, PlayerPosition position);

    String getTeamMeanVisitorReceived(String teamName, PlayerPosition position);


    String getTeamMeanPoints(String teamName,PlayerPosition position);

    String getTeamMeanPointsReceived(String teamName,PlayerPosition position);

    String getTeamMeanPointsLocal(String teamName,PlayerPosition position);

    String getTeamMeanPointsVisitor(String teamName,PlayerPosition position);

    String getTeamMeanPointsLocalReceived(String teamName,PlayerPosition position);

    String getTeamMeanPointsVisitorReceived(String teamName,PlayerPosition position);

}