package org.amupoti.supermanager.acb.application.port.out;

/**
 * Driven port: execute player buy/sell/cancel change operations.
 */
public interface PlayerChangePort {
    void buyPlayer(String userTeamId, long playerId, String token);
    void sellPlayer(String userTeamId, long playerId, String token);
    void cancelChange(long changeId, String token);
    void cancelAllChanges(String userTeamId, String token);
    String getPendingChanges(String userTeamId, String token);
}
