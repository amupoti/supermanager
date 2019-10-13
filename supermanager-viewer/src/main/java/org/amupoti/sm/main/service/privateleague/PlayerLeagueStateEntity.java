package org.amupoti.sm.main.service.privateleague;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@IdClass(PlayerLeagueStateId.class)
public class PlayerLeagueStateEntity {

    @Id
    private int matchNumber;
    @Id
    private String team;
    @Id
    private String stat;
    
    private Integer points;

    public PlayerLeagueStateEntity(int matchNumber, String team, String stat, Integer points) {
        this.matchNumber = matchNumber;
        this.team = team;
        this.stat = stat;
        this.points = points;
    }
}