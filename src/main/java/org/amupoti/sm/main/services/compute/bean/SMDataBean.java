package org.amupoti.sm.main.services.compute.bean;

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

    Float playerOtherTeamReceivedValShort;
    Float playerOtherTeamReceivedValMedium;
    Float playerOtherTeamReceivedValLong;

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

