package org.amupoti.supermanager.acb.application.service;

import org.amupoti.supermanager.acb.application.port.in.CancelAllChangesUseCase;
import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;

/**
 * Use case: cancel all pending player changes for a team.
 */
public class CancelAllChangesService implements CancelAllChangesUseCase {

    private final PlayerChangePort playerChangePort;

    public CancelAllChangesService(PlayerChangePort playerChangePort) {
        this.playerChangePort = playerChangePort;
    }

    @Override
    public void cancelAllChanges(String userTeamId, String token) {
        playerChangePort.cancelAllChanges(userTeamId, token);
    }
}
