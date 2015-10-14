package org.amupoti.sm.main;

import lombok.Data;

/**
 * Created by Marcel on 30/09/2015.
 */
@Data
public class SMDataBean {

    /*
     * Player related data
     */
    String playerId;
    Float playerLocalVal;
    Float playerVisitorVal;
    String playerPosition;

    /*
     * Team related data
     */
    String localOrVisitor;
    String teamVal;
    String otherTeamReceivedVal;
    String teamValAsLV;
    String otherTeamReceivedValAsLV;

    Float keepBroker;
    Float calendarBoostShort;
    Float calendarBoostMedium;
    Float calendarBoostLong;
    String teamName;
    String otherTeamName;

    //TODO: position values
    String otherTeamPositionReceivedVal;
}

