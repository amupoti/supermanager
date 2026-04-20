package org.amupoti.supermanager.acb.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class PlayerStatus {
    @Builder.Default private boolean spanish = false;
    @Builder.Default private boolean active = true;
    @Builder.Default private boolean foreign = false;
    @Builder.Default private boolean injured = false;
    @Builder.Default private boolean postponed = false;
    @Builder.Default private boolean doubtful = false;
    @Builder.Default private boolean blocked = false;
    @Builder.Default private boolean info = false;

    @Override
    public String toString() {
        List<String> sb = new ArrayList<>();
        if (!active) sb.add("INA");
        if (injured) sb.add("LES");
        if (postponed) sb.add("APL");
        if (doubtful) sb.add("DUD");
        if (blocked) sb.add("BLQ");
        if (spanish) sb.add("ESP");
        if (foreign) sb.add("EXT");
        if (info) sb.add("Info");
        return String.join(", ", sb);
    }
}
