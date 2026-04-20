package org.amupoti.supermanager.acb.application.service;

import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds the best candidate player to buy for each under-filled roster position.
 */
public class FindCandidateService {

    private static final int MAX_TEAM_SIZE = 10;
    private static final Map<String, Integer> POSITION_QUOTA = Map.of("B", 2, "A", 4, "P", 4);

    public void findCandidateBuyPlayer(Team team, MarketData marketData) {
        if (team.getPlayerList().size() >= MAX_TEAM_SIZE) return;

        Set<Long> teamIds = team.getPlayerList().stream()
                .map(Player::getIdPlayer)
                .collect(Collectors.toSet());

        Map<String, Long> positionCounts = team.getPlayerList().stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        long spanishCount = team.getPlayerList().stream()
                .filter(p -> p.getStatus().isSpanish()).count();
        long foreignCount = team.getPlayerList().stream()
                .filter(p -> p.getStatus().isForeign()).count();

        boolean requireSpanish = marketData.hasSpanishData() && spanishCount < 4;
        boolean excludeForeign = foreignCount >= 2;

        Map<String, Player> candidatesByPosition = new HashMap<>();
        POSITION_QUOTA.forEach((pos, quota) -> {
            if (positionCounts.getOrDefault(pos, 0L) < quota) {
                marketData.findMostExpensiveFitPlayer(teamIds, team.getCash(), Set.of(pos),
                                requireSpanish, excludeForeign)
                        .ifPresent(idPlayer -> {
                            Map<String, String> data = marketData.getPlayerMap(idPlayer);
                            String name = data != null ? data.get(MarketCategory.NAME.name()) : null;
                            candidatesByPosition.put(pos, Player.builder()
                                    .name(name).position(pos)
                                    .marketData(data).idPlayer(idPlayer).build());
                        });
            }
        });

        if (!candidatesByPosition.isEmpty()) {
            team.setCandidatesByPosition(candidatesByPosition);
        }
    }

}
