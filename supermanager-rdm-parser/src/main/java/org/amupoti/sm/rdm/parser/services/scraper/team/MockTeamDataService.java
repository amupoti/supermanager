package org.amupoti.sm.rdm.parser.services.scraper.team;

import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;

import java.util.Random;

/**
 * Mock service which provides random data for teams
 * Created by Marcel on 28/09/2015.
 */
public class MockTeamDataService implements TeamDataService {

    private Random random = new Random(System.currentTimeMillis());

    @Override
    public String getTeamMean(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanReceived(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanLocal(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanVisitor(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanLocalReceived(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanVisitorReceived(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPoints(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsReceived(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsLocal(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsVisitor(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsLocalReceived(String teamName, PlayerPositionRdm position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsVisitorReceived(String teamName, PlayerPositionRdm position) {
        return getRand();
    }


    private String getRand() {
        return String.valueOf(random.nextInt(20) - 2);
    }

}
