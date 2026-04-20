package org.amupoti.supermanager.viewer.application.model;

import lombok.Builder;
import lombok.Getter;
import org.amupoti.supermanager.acb.domain.model.Player;

import java.util.List;

@Builder
@Getter
public class ViewerPlayer {
    private Player player;
    private List<ViewerMatch> matches;
}
