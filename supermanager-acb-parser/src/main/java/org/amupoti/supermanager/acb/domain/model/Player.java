package org.amupoti.supermanager.acb.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@ToString
public class Player {
    String name;
    String position;
    String score;
    PlayerStatus status;
    Map<String, String> marketData;
    @Setter long idUserTeamPlayerChange;
    @Setter long idPlayer;
    /** 0=none, 1=pending sell, 2=pending buy. Set after cross-referencing the pending-changes API. */
    @Setter int pendingAction;
}
