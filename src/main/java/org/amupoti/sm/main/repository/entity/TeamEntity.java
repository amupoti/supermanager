package org.amupoti.sm.main.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

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
    @GeneratedValue(strategy= GenerationType.AUTO)
    private String id;
    private String name;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @MapKey(name="type")
    private Map<String,ValueEntity> valMap = new HashMap<>();

    @OneToMany
    @MapKey(name="number")
    private Map<Integer,MatchEntity> matchMap = new HashMap<>();

}
