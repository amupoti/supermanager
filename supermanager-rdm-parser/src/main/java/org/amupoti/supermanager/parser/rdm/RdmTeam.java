package org.amupoti.supermanager.parser.rdm;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static org.amupoti.supermanager.parser.rdm.RdmTeam.Quality.*;

/**
 * Created by amupoti on 27/10/2018.
 */
@Getter
@Slf4j
public enum RdmTeam {

    BAS("Cazoo Baskonia", GOOD),
    FCB("Barça", TOP),
    RMA("Real Madrid", TOP),
    VBC("Valencia Basket", AVERAGE),
    CAN("Lenovo Tenerife", GOOD),
    JOV("Joventut Badalona", GOOD),
    UNI("Unicaja", AVERAGE),
    GCA("Gran Canaria", AVERAGE),
    MUR("UCAM Murcia", AVERAGE),
    BRE("Río Breogán", AVERAGE),
    BLB("Bilbao", AVERAGE),
    MAN("BAXI Manresa", POOR),
    ZAR("Zaragoza", POOR),
    OBR("Obradoiro", POOR),
    CBG("Granada", WORST),
    GIR("Bàsquet Girona", WORST),
    FUE("Fuenlabrada", WORST),
    BET("Real Betis", WORST);

    private String teamName;
    private Quality quality;

    RdmTeam(String teamName, Quality quality) {

        this.teamName = teamName;
        this.quality = quality;
    }

    public enum Quality {
        TOP, GOOD, AVERAGE, POOR, WORST
    }

    public static RdmTeam fromTeamName(String teamName) {

        for (RdmTeam t : RdmTeam.values()) {
            if (teamName.contains(t.teamName)) {
                return t;
            }
        }
        log.error("Could not convert team {}", teamName);
        throw new RdmException("No se puede convertir el nombre de equipo "+teamName+" a un nombre del rincon del manager. Es posible que el equipo haya cambiado su nombre");
    }
}
