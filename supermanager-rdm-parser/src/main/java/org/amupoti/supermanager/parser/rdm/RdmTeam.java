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

    BAS("Baskonia", TOP),
    FCB("Barça", TOP),
    RMA("Real Madrid", TOP),
    UNI("Unicaja", GOOD),
    VBC("Valencia Basket", GOOD),
    CAN("Lenovo Tenerife", GOOD),
    JOV("Joventut Badalona", AVERAGE),
    AND("MoraBanc Andorra", AVERAGE),
    OBR("Monbus Obradoiro", AVERAGE),
    BRE("RÍO BREOGÁN", AVERAGE),
    GCA("Gran Canaria", AVERAGE),
    ZAR("Casademont Zaragoza", AVERAGE),
    FUE("Urbas Fuenlabrada", POOR),
    BUR("Hereda San Pablo Burgos", POOR),
    MAN("BAXI Manresa", POOR),
    MUR("UCAM Murcia", POOR),
    BET("Coosur Real Betis", WORST),
    BLB("Surne Bilbao Basket", WORST);

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
            if (t.teamName.equalsIgnoreCase(teamName)) {
                return t;
            }
        }
        log.info("Could not convert team {}", teamName);
        return null;
    }
}
