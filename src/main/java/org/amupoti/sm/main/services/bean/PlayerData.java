package org.amupoti.sm.main.services.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Marcel on 04/08/2015.
 */
@Getter
@Setter
@ToString
public class PlayerData {

    private PlayerId playerId;
    private Float localMean;
    private Float visitorMean;
    private Float keepBroker;
}
