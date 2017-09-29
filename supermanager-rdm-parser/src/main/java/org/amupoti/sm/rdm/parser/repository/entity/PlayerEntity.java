package org.amupoti.sm.rdm.parser.repository.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;

import javax.persistence.*;

/**
 * Created by Marcel on 04/08/2015.
 */
@Getter
@Setter
@ToString
@Entity
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private PlayerId playerId;
    private Float localMean;
    private Float visitorMean;
    private Float keepBroker;
    private Float broker;
    private Float meanLastMatches;
    private PlayerPositionRdm playerPosition;
    @OneToOne
    private TeamEntity team;
}
