package org.amupoti.sm.main.model;

import lombok.Builder;
import lombok.Getter;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;

import java.util.List;

/**
 * Created by amupoti on 05/11/2018.
 */
@Builder
@Getter
public class ViewerPlayer {

    private SmPlayer player;
    private List<ViewerMatch> matches;
}
