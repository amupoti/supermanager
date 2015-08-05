package org.amupoti.sm.main.services;

import org.amupoti.sm.main.services.bean.PlayerData;
import org.amupoti.sm.main.services.bean.PlayerId;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Recovers data from the player page
 * Created by Marcel on 04/08/2015.
 */
@Service
public class PlayerDataService {

    //TODO: this XPATH expressions are not valid since the page changes depending on the number of matches played by player

    private static final String VAL_MEDIA_LOCAL = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[38]/td[9]/b";
    private static final String VAL_MEDIA_VISITANTE = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[39]/td[9]/b";
    private static final String VAL_MANTENER_BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[2]";
    private static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    private HtmlCleaner cleaner;

    @Autowired
    private HTMLProviderService htmlProviderService;

    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();

    }

    /**
     * Returns the data available for a player by checking the player page
     *
     * @param playerId
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    public PlayerData getPlayerData(PlayerId playerId) throws IOException, XPatherException, URISyntaxException {
        PlayerData playerData = new PlayerData();
        String html = htmlProviderService.getPlayerURL(playerId);
        String localMean = getValue(html, VAL_MEDIA_LOCAL);
        String visitorMean = getValue(html, VAL_MEDIA_VISITANTE);
        String keepBroker = getValue(html, VAL_MANTENER_BROKER);

        playerData.setPlayerId(playerId);
        playerData.setLocalMean(Float.parseFloat(localMean));
        playerData.setVisitorMean(Float.parseFloat(visitorMean));
        playerData.setKeepBroker(Float.parseFloat(keepBroker));
        return playerData;
    }


    private String getValue(String html, String xPathExpression) throws XPatherException, IOException {
        try{
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return s;
        }
        catch (Exception e){
            return "-1";
        }
    }

    //*[@id="sm_central"]/div/table/tbody/tr[3]/td[1]/a
    ////*[@id="sm_central"]/div/table/tbody/tr[286]/td[1]/a
    public List<PlayerId> getAllPlayers() throws IOException, XPatherException {
        String html = htmlProviderService.getAllPlayersURL();
        TagNode node = cleaner.clean(html);
        Object[] objects = node.evaluateXPath(ALL_PLAYERS);
        List<PlayerId> playerIds = new LinkedList<>();
        for (int i = 0; i < objects.length; i++) {
            TagNode tagNode = (TagNode) objects[i];
            String name = tagNode.getAllChildren().get(0).toString();
            if (name.contains(",")) {
                playerIds.add(new PlayerId(name));
            }
        }
        return playerIds;
    }
}
