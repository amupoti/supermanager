package org.amupoti.sm.main.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by Marcel on 09/08/2015.
 */
@Getter
@Setter
@Entity
public class TeamEntity {

    /**
     * We need here the mean value obtained as local, visitor and also the value received as local and visitor
     * We need this by position and by team
     * TODO: create Entity
     */
    @Id
    private String id;
    private String name;
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ValueEntity valB;
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ValueEntity valA;
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ValueEntity valP;
//    private ValueEntity valTeam;

}
