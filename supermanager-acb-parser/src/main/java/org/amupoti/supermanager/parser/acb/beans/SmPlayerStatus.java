package org.amupoti.supermanager.parser.acb.beans;


import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amupoti on 01/08/2017.
 */
@Builder
@Getter
public class SmPlayerStatus {
    @Builder.Default
    private boolean spanish = false;
    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private boolean foreign = false;
    @Builder.Default
    private boolean injured = false;
    @Builder.Default
    private boolean info = false;

    @Override
    public String toString() {
        List<String> sb = new ArrayList<>();
        if (!active) sb.add("INA");
        if (injured) sb.add("LES");
        if (spanish) sb.add("ESP");
        if (foreign) sb.add("EXT");
        if (info) sb.add("Info");
        return String.join(", ", sb);

    }
}
