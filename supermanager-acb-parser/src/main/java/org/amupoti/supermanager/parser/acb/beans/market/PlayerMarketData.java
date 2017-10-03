package org.amupoti.supermanager.parser.acb.beans.market;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amupoti on 29/09/2017.
 */
public class PlayerMarketData {

    private Map<String, Map<String, String>> playerData = new HashMap<>();

    public void addPlayer(String playerName) {
        playerData.putIfAbsent(playerName, new HashMap<>());
    }

    public void addPlayerData(String playerName, String stat, String value) {
        playerData.get(playerName).putIfAbsent(stat, value);
    }

    public Map<String, String> getPlayerMap(String playerName) {
        return playerData.get(playerName);
    }

    public int size() {
        return playerData.size();
    }
}
