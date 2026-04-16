package org.amupoti.supermanager.acb.application.port.out;

/**
 * Driven port: retrieve per-player statistics from the ACB API.
 */
public interface PlayerStatsPort {
    /** Returns the last-4-matches average score string, or null if no data. */
    String getLastFourAverage(long idPlayer, String token);
}
