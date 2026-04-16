package org.amupoti.sm.main.model;

import lombok.Getter;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;

/**
 * One row in the players table: either a real team player or an empty roster slot
 * (optionally showing the best buyable candidate for that position).
 */
@Getter
public class PlayerRow {

    /** Non-null for real players; null for empty roster slots. */
    private final ViewerPlayer realPlayer;

    /**
     * Non-null when a buyable candidate was found for this slot.
     * Null when the slot is empty but nothing affordable / in the right position is available.
     */
    private final SmPlayer candidate;

    /** Position label ("B", "A", "P") for empty slots; null for real-player rows. */
    private final String missingPosition;

    private PlayerRow(ViewerPlayer realPlayer, SmPlayer candidate, String missingPosition) {
        this.realPlayer = realPlayer;
        this.candidate = candidate;
        this.missingPosition = missingPosition;
    }

    public static PlayerRow ofReal(ViewerPlayer vp) {
        return new PlayerRow(vp, null, null);
    }

    public static PlayerRow ofSlot(String position, SmPlayer candidate) {
        return new PlayerRow(null, candidate, position);
    }

    /** True when this row represents an empty roster slot (not a real player). */
    public boolean isSlotRow() {
        return missingPosition != null;
    }
}
