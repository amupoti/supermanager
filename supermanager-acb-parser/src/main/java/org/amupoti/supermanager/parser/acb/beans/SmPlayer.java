package org.amupoti.supermanager.parser.acb.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Created by Marcel on 02/01/2016.
 */
@Getter
@Builder
@ToString
public class SmPlayer {
    String name;
    String position;
    String score;
    SmPlayerStatus status;
    Map<String, String> marketData;
    @Setter long idUserTeamPlayerChange;
    @Setter long idPlayer;
}
