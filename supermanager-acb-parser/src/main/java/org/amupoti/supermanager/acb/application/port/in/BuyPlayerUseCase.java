package org.amupoti.supermanager.acb.application.port.in;

/**
 * Driving port: buy a market player and add them to the user team.
 */
public interface BuyPlayerUseCase {
    void buyPlayer(String userTeamId, long playerId, String token);
}
