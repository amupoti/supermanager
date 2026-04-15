package org.amupoti.supermanager.parser.acb.beans.market;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    /**
     * Returns true if at least one market player has a known Spanish/homegrown flag.
     * Used to guard against applying the quota constraint when the API field is absent.
     */
    public boolean hasSpanishData() {
        return playerData.values().stream()
                .anyMatch(d -> "true".equals(d.get(MarketCategory.IS_SPANISH.name())));
    }

    /**
     * Returns the name of the most expensive fit player the team can afford, is not already in the team,
     * fills one of the positions that still have a free slot, and satisfies nationality constraints.
     *
     * @param excludedNames   player names already on the team
     * @param budget          team's available cash; only players whose price is &lt;= budget are considered
     * @param neededPositions position names ("B", "A", "P") that still have room on the roster
     * @param requireSpanish  if true, only Spanish/homegrown players are considered
     * @param excludeForeign  if true, non-EU players are excluded
     */
    public Optional<String> findMostExpensiveFitPlayerName(Set<String> excludedNames, int budget,
                                                           Set<String> neededPositions,
                                                           boolean requireSpanish, boolean excludeForeign) {
        return playerData.entrySet().stream()
                .filter(e -> !excludedNames.contains(e.getKey()))
                .filter(e -> "fit".equals(e.getValue().get(MarketCategory.FISIC_STATUS.name())))
                .filter(e -> {
                    String price = e.getValue().get(MarketCategory.PRICE.name());
                    return price != null && Double.parseDouble(price) <= budget;
                })
                .filter(e -> {
                    String idPlayer = e.getValue().get(MarketCategory.ID_PLAYER.name());
                    return idPlayer != null && Long.parseLong(idPlayer) > 0;
                })
                .filter(e -> {
                    String posName = e.getValue().get(MarketCategory.POSITION.name());
                    return posName != null && neededPositions.contains(posName);
                })
                .filter(e -> !requireSpanish || "true".equals(e.getValue().get(MarketCategory.IS_SPANISH.name())))
                .filter(e -> !excludeForeign || !"true".equals(e.getValue().get(MarketCategory.IS_FOREIGN.name())))
                .max(Comparator.comparingDouble(e -> Double.parseDouble(e.getValue().get(MarketCategory.PRICE.name()))))
                .map(Map.Entry::getKey);
    }
}
