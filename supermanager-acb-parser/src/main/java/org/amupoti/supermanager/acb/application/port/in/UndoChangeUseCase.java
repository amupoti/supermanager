package org.amupoti.supermanager.acb.application.port.in;

/**
 * Driving port: cancel a single pending player change by its change ID.
 */
public interface UndoChangeUseCase {
    void undoChange(long changeId, String token);
}
