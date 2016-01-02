package org.amupoti.supermanager.parser.acb.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Marcel on 15/12/2015.
 */
@Getter
@Setter
public class SMTeamDataBean {

    //TODO: simplify structure to store data
    private String team;
    private String teamVs;
    private String pointsLocal;
    private String pointsReceivedLocal;
    private String pointsVisitor;
    private String pointsReceivedVisitor;
    private String pointsExpected;
    private String pointsReceivedExpected;

}
