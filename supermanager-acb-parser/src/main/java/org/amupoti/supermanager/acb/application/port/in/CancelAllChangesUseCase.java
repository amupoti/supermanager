package org.amupoti.supermanager.acb.application.port.in;

/**
 * Driving port: cancel all pending player changes for a team.
 */
public interface CancelAllChangesUseCase {
    void cancelAllChanges(String userTeamId, String token);
}
