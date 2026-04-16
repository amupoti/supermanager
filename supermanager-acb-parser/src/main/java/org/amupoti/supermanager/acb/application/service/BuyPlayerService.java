package org.amupoti.supermanager.acb.application.service;

import org.amupoti.supermanager.acb.application.port.in.BuyPlayerUseCase;
import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;

/**
 * Use case: buy a player from the market.
 */
public class BuyPlayerService implements BuyPlayerUseCase {

    private final PlayerChangePort playerChangePort;

    public BuyPlayerService(PlayerChangePort playerChangePort) {
        this.playerChangePort = playerChangePort;
    }

    @Override
    public void buyPlayer(String userTeamId, long playerId, String token) {
        playerChangePort.buyPlayer(userTeamId, playerId, token);
    }
}
