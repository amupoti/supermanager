package org.amupoti.supermanager.parser.acb.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;


@Data
@AllArgsConstructor
public class SMUserTeamDataBean {

    private List<SMPlayerDataBean> playerList = new LinkedList<>();
    private Float score;
}
