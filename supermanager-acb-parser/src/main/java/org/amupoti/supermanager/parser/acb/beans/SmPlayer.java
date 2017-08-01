package org.amupoti.supermanager.parser.acb.beans;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Builder;

/**
 * Created by Marcel on 02/01/2016.
 */
@Getter
@Builder
@ToString
public class SmPlayer {
    String name;
    String position;
    String score;
    SmPlayerStatus status;
}
