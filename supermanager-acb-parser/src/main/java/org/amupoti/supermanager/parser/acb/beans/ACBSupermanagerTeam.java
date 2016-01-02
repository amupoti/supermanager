package org.amupoti.supermanager.parser.acb.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
@Getter
@Setter
@ToString
public class ACBSupermanagerTeam {

    private String name;
    private String url;
    private List<ACBPlayer> players;
}
