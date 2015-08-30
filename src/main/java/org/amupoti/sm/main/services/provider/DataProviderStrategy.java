package org.amupoti.sm.main.services.provider;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marcel on 17/08/2015.
 */
public interface DataProviderStrategy {


    String getTeamNameByMatchNumber(int matchNumber, boolean local);

    String getTeamPage(String teamId, String position) throws IOException;

    String getTeamMean(String html, String position, String teamName);

    String getTeamMeanReceived(String html, String position, String teamName);

    String getTeamMeanLocal(String html, String position, String teamName);

    String getTeamMeanVisitor(String html, String position, String teamName);

    String getTeamMeanLocalReceived(String html, String position, String teamName);

    /**
     * Returns the mean value for the points received as visitor for the given position of the
     * team represented by the teamName
     *
     * @param html
     * @param position
     * @param teamName
     * @return
     */
    String getTeamMeanVisitorReceived(String html, String position, String teamName);

    List<PlayerId> getPlayerIds() throws IOException, XPatherException;

    List<PlayerEntity> getPlayersData(List<PlayerId> playerIdList) throws XPatherException, IOException, URISyntaxException, InterruptedException, ExecutionException;
}