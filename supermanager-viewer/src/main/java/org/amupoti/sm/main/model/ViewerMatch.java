package org.amupoti.sm.main.model;

import lombok.Builder;
import lombok.Getter;
import org.amupoti.supermanager.parser.rdm.RdmTeam;

/**
 * Created by amupoti on 05/11/2018.
 */
@Builder
@Getter
public class ViewerMatch {

    private RdmTeam againstTeam;
    private boolean local;
}
