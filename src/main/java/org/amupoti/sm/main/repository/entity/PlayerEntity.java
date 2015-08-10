package org.amupoti.sm.main.repository.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Marcel on 04/08/2015.
 */
@Getter
@Setter
@ToString
@Entity
public class PlayerEntity {

    @Id
    private PlayerId id;
    private Float localMean;
    private Float visitorMean;
    private Float keepBroker;
}
