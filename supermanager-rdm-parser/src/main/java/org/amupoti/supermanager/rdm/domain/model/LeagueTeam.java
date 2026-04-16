package org.amupoti.supermanager.rdm.domain.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static org.amupoti.supermanager.rdm.domain.model.LeagueTeam.Quality.*;

/**
 * Represents a Liga Endesa basketball club in the SuperManager context.
 * Quality tiers are static assessments used for upcoming fixture difficulty ratings.
 */
@Getter
@Slf4j
public enum LeagueTeam {

    BAS("Baskonia", GOOD),
    FCB("Barça", TOP),
    RMA("Real Madrid", TOP),
    VBC("Valencia Basket", GOOD),
    CAN("Tenerife", GOOD),
    JOV("Joventut", GOOD),
    UNI("Unicaja", AVERAGE),
    GCA("Gran Canaria", AVERAGE),
    MUR("UCAM Murcia", AVERAGE),
    BRE("Río Breogán", POOR),
    BLB("Bilbao", AVERAGE),
    MAN("BAXI Manresa", POOR),
    ZAR("Zaragoza", POOR),
    HIO("Lleida", AVERAGE),
    COV("Granada", POOR),
    GIR("Bàsquet Girona", WORST),
    BUR("Burgos", WORST),
    AND("Andorra", POOR);

    private String teamName;
    private Quality quality;

    LeagueTeam(String teamName, Quality quality) {
        this.teamName = teamName;
        this.quality = quality;
    }

    public static LeagueTeam fromTeamName(String teamName) {
        for (LeagueTeam t : LeagueTeam.values()) {
            if (teamName.contains(t.teamName)) {
                return t;
            }
        }
        log.error("Could not convert team {}", teamName);
        throw new org.amupoti.supermanager.parser.rdm.RdmException(
                "No se puede convertir el nombre de equipo " + teamName
                        + " a un nombre del rincon del manager. Es posible que el equipo haya cambiado su nombre");
    }

    public enum Quality {
        TOP, GOOD, AVERAGE, POOR, WORST
    }
}
