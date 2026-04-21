package org.amupoti.supermanager.acb.domain.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Domain index of market data keyed by idPlayer (unique).
 * Contains domain logic for finding candidates based on business rules.
 */
public class MarketData {

    private Map<Long, Map<String, String>> playerData = new HashMap<>();

    public void addPlayer(long idPlayer) {
        playerData.putIfAbsent(idPlayer, new HashMap<>());
    }

    public void addPlayerData(long idPlayer, String stat, String value) {
        playerData.get(idPlayer).putIfAbsent(stat, value);
    }

    public Map<String, String> getPlayerMap(long idPlayer) {
        return playerData.get(idPlayer);
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
     * Returns the idPlayer of the most expensive fit, non-blocked player the team can afford,
     * is not already in the team, fills one of the positions that still have a free slot,
     * and satisfies nationality constraints.
     *
     * @param excludedIds     idPlayer values already on the team
     * @param budget          team's available cash; only players whose price is &lt;= budget are considered
     * @param neededPositions position names ("B", "A", "P") that still have room on the roster
     * @param requireSpanish  if true, only Spanish/homegrown players are considered
     * @param excludeForeign  if true, non-EU players are excluded
     */
    public Optional<Long> findMostExpensiveFitPlayer(Set<Long> excludedIds, int budget,
                                                     Set<String> neededPositions,
                                                     boolean requireSpanish, boolean excludeForeign) {
        return playerData.entrySet().stream()
                .filter(e -> !excludedIds.contains(e.getKey()))
                .filter(e -> {
                    // Exclude only explicitly bad statuses; null/absent means the player is available
                    String status = e.getValue().get(MarketCategory.FISIC_STATUS.name());
                    return !"injured".equals(status) && !"postponed".equals(status) && !"doubtful".equals(status);
                })
                .filter(e -> !"true".equals(e.getValue().get(MarketCategory.IS_BLOCKED.name())))
                .filter(e -> {
                    String price = e.getValue().get(MarketCategory.PRICE.name());
                    return price != null && Double.parseDouble(price) <= budget;
                })
                .filter(e -> e.getKey() > 0)
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
