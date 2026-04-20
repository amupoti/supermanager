package org.amupoti.supermanager.viewer.application.model;

import lombok.Getter;
import org.amupoti.supermanager.acb.domain.model.Player;

/**
 * One row in the players table: either a real team player or an empty roster slot
 * (optionally showing the best buyable candidate for that position, or an empty slot if none found).
 */
@Getter
public class PlayerRow {

    /** Non-null for real players; null for empty roster slots. */
    private final ViewerPlayer realPlayer;

    /** Non-null when a buyable candidate was found for this slot. */
    private final Player candidate;

    /** Position label ("B", "A", "P") for empty slots; null for real-player rows. */
    private final String missingPosition;

    private PlayerRow(ViewerPlayer realPlayer, Player candidate, String missingPosition) {
        this.realPlayer = realPlayer;
        this.candidate = candidate;
        this.missingPosition = missingPosition;
    }

    public static PlayerRow ofReal(ViewerPlayer vp) {
        return new PlayerRow(vp, null, null);
    }

    public static PlayerRow ofSlot(String position, Player candidate) {
        return new PlayerRow(null, candidate, position);
    }

    public boolean isSlotRow() {
        return missingPosition != null;
    }
}
