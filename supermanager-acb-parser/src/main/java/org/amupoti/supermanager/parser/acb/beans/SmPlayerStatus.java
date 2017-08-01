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
        if (!active) sb.add("Inactivo");
        if (injured) sb.add("Lesionado");
        if (spanish) sb.add("Español");
        if (foreign) sb.add("Extracom");
        if (info) sb.add("Info");
        return String.join(", ", sb);

    }
}
