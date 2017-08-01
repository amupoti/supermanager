package org.amupoti.sm.main.model;

import lombok.Data;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;

import java.util.List;

/**
 * Created by amupoti on 01/08/2017.
 */
@Data
public class UserTeamBean {
    private final List<SmPlayer> playerList;
    private final Float score;


}
