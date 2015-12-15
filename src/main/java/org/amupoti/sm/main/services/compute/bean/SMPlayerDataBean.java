package org.amupoti.sm.main.services.compute.bean;

import lombok.Data;

/**
 * Created by Marcel on 30/09/2015.
 */
@Data
public class SMPlayerDataBean {

    /*
     * Player related data
     */
    String playerId;
    Float playerLocalVal;
    Float playerVisitorVal;
    String playerPosition;

    String playerOtherTeamReceivedValShort;
    String playerOtherTeamReceivedValMedium;
    String playerOtherTeamReceivedValLong;
    String playerOtherNextMatchesVal;

    /*
     * Team related data
     */
    String localOrVisitor;
    String teamVal;
    String otherTeamReceivedVal;
    String teamValAsLV;
    String otherTeamReceivedValAsLV;

    Float broker;
    Float keepBroker;
    Float calendarBoostShort;
    Float calendarBoostMedium;
    Float calendarBoostLong;
    String teamName;
    String otherTeamName;

    //TODO: position values
    String otherTeamPositionReceivedVal;
}

