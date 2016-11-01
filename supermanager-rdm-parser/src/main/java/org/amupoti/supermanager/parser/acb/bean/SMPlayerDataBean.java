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
    Float meanLastMatches;
    String teamName;
    String otherTeamName;

    //TODO: position values
    String otherTeamPositionReceivedVal;

    String mvp;
    String ranking;
}

