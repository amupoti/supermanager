package org.amupoti.supermanager.parser.acb.bean;

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

    //TODO: change to ints
    Float playerOtherTeamReceivedValShort;
    Float playerOtherTeamReceivedValMedium;
    Float playerOtherTeamReceivedValLong;
    Float playerOtherNextMatchesVal;

    /*
     * Team related data
     */
    String localOrVisitor;
    Float teamVal;
    Float otherTeamReceivedVal;
    Float teamValAsLV;
    Float otherTeamReceivedValAsLV;

    Float broker;
    Float keepBroker;
    Float meanLastMatches;
    String teamName;
    String otherTeamName;

    //TODO: position values
    Float otherTeamPositionReceivedVal;

    Float mvp;
    Float ranking;
}

