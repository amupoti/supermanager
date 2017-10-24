package org.amupoti.sm.rdm.parser.services.scraper.team;

import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;

/**
 * Created by Marcel on 28/09/2015.
 */
public interface TeamDataService {


    String getTeamMean(String teamName, PlayerPositionRdm position);

    String getTeamMeanReceived(String teamName, PlayerPositionRdm position);

    String getTeamMeanLocal(String teamName, PlayerPositionRdm position);

    String getTeamMeanVisitor(String teamName, PlayerPositionRdm position);

    String getTeamMeanLocalReceived(String teamName, PlayerPositionRdm position);

    String getTeamMeanVisitorReceived(String teamName, PlayerPositionRdm position);


    String getTeamMeanPoints(String teamName, PlayerPositionRdm position);

    String getTeamMeanPointsReceived(String teamName, PlayerPositionRdm position);

    String getTeamMeanPointsLocal(String teamName, PlayerPositionRdm position);

    String getTeamMeanPointsVisitor(String teamName, PlayerPositionRdm position);

    String getTeamMeanPointsLocalReceived(String teamName, PlayerPositionRdm position);

    String getTeamMeanPointsVisitorReceived(String teamName, PlayerPositionRdm position);

}