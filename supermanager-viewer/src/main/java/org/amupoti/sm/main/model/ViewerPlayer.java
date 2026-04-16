package org.amupoti.sm.main.model;

import lombok.Builder;
import lombok.Getter;
import org.amupoti.supermanager.acb.domain.model.Player;

import java.util.List;

/**
 * Created by amupoti on 05/11/2018.
 */
@Builder
@Getter
public class ViewerPlayer {

    private Player player;
    private List<ViewerMatch> matches;
}
