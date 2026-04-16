package org.amupoti.supermanager.acb.application.service;

import org.amupoti.supermanager.acb.application.port.in.SellPlayerUseCase;
import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;

/**
 * Use case: release a player from the team.
 */
public class SellPlayerService implements SellPlayerUseCase {

    private final PlayerChangePort playerChangePort;

    public SellPlayerService(PlayerChangePort playerChangePort) {
        this.playerChangePort = playerChangePort;
    }

    @Override
    public void sellPlayer(String userTeamId, long playerId, String token) {
        playerChangePort.sellPlayer(userTeamId, playerId, token);
    }
}
