package org.amupoti.supermanager.parser.acb.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * Created by Marcel on 02/01/2016.
 */
@Getter
@Builder
@ToString
public class SmPlayer {
    private String name;
    private String position;
    private String score;
    private SmPlayerStatus status;
    private Map<String, String> marketData;
}
