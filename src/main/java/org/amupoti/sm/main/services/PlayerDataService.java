package org.amupoti.sm.main.services;

import org.amupoti.sm.main.services.bean.PlayerData;
import org.amupoti.sm.main.services.bean.PlayerId;
import org.apache.commons.io.IOUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
    private final String PLAYER_URL = "http://www.rincondelmanager.com/smgr/stats.php?nombre=";
    private HtmlCleaner cleaner;

    @PostConstruct
    public void init(){
        cleaner = new HtmlCleaner();

    }

    /**
     * Returns the data available for a player by checking the player page
     * @param playerId
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    public PlayerData getPlayerData(PlayerId playerId) throws IOException, XPatherException {
        PlayerData playerData = new PlayerData();

        String html = IOUtils.toString(getPlayerURL(playerId).openStream());
        String localMean = getValue(html, VAL_MEDIA_LOCAL);
        String visitorMean = getValue(html,VAL_MEDIA_VISITANTE);
        String keepBroker = getValue(html,VAL_MANTENER_BROKER);

        playerData.setLocalMean(Float.parseFloat(localMean));
        playerData.setVisitorMean(Float.parseFloat(visitorMean));
        playerData.setKeepBroker(Float.parseFloat(keepBroker));
        return playerData;
    }


    private String getValue(String html, String xPathExpression) throws XPatherException, IOException {
        TagNode node = cleaner.clean(html);
        Object[] objects = node.evaluateXPath(xPathExpression);
        return ((TagNode) objects[0]).getAllChildren().get(0).toString();
    }

    private URL getPlayerURL(PlayerId playerId) throws MalformedURLException {
        //return new URL(PLAYER_URL+playerId.getId());
        return new URL(PLAYER_URL+"Colom,%20Quino");
    }
}
