package org.amupoti.sm.main.services.provider.team;

import org.amupoti.sm.main.services.PlayerPosition;

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
    public String[] getTeamIds() {

        String[] teamIds={"AND","BLB","CAI","CAN","EST","FCB","FUE","GBC","GCA","JOV","LAB","MAN","MUR","OBR","RMA","SEV","UNI","VBC"};
        return teamIds;
    }

    private String getRand(){
        return String.valueOf(random.nextInt(20)-2);
    }

}
