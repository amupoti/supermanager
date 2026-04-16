package org.amupoti.supermanager.acb.application.port.in;

/**
 * Driving port: release a player from the user team.
 */
public interface SellPlayerUseCase {
    void sellPlayer(String userTeamId, long playerId, String token);
}
