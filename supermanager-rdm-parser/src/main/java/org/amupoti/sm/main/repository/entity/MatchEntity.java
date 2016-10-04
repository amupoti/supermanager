package org.amupoti.sm.main.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static org.amupoti.sm.main.config.SMConstants.NOT_PLAYING_MATCH;

/**
 * Created by Marcel on 16/08/2015.
 */
@Entity
@Getter
@Setter
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private int number;

    private String local;
    private String visitor;

    public boolean isLocal(String teamName){
        return teamName.equals(local);
    }

    public boolean isNotPlayingMatch(){
        return local.equals(NOT_PLAYING_MATCH) || visitor.equals(NOT_PLAYING_MATCH);
    }

}
