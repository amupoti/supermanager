package org.amupoti.supermanager.acb.application.service;

import org.amupoti.supermanager.acb.application.port.in.UndoChangeUseCase;
import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;

/**
 * Use case: undo (cancel) a single pending player change.
 */
public class UndoChangeService implements UndoChangeUseCase {

    private final PlayerChangePort playerChangePort;

    public UndoChangeService(PlayerChangePort playerChangePort) {
        this.playerChangePort = playerChangePort;
    }

    @Override
    public void undoChange(long changeId, String token) {
        playerChangePort.cancelChange(changeId, token);
    }
}
