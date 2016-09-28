package org.amupoti.sm.main.services.provider.team;

import org.amupoti.sm.main.bean.PlayerPosition;

import java.util.Random;

/**
 * Mock service which provides random data for teams
 * Created by Marcel on 28/09/2015.
 */
public class MockTeamDataService implements TeamDataService{

    private Random random = new Random(System.currentTimeMillis());

    @Override
    public String getTeamMean(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanReceived(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanLocal(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanVisitor(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanLocalReceived(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanVisitorReceived(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPoints(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsReceived(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsLocal(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsVisitor(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsLocalReceived(String teamName, PlayerPosition position) {
        return getRand();
    }

    @Override
    public String getTeamMeanPointsVisitorReceived(String teamName, PlayerPosition position) {
        return getRand();
    }


    private String getRand(){
        return String.valueOf(random.nextInt(20)-2);
    }

}
