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

    BAS("KIROLBET Baskonia", TOP),
    FCB("Barça", TOP),
    RMA("Real Madrid", TOP),
    UNI("Unicaja", GOOD),
    VBC("Valencia Basket Club", GOOD),
    CAN("Iberostar Tenerife", GOOD),
    JOV("Club Joventut Badalona", AVERAGE),
    AND("MoraBanc Andorra", AVERAGE),
    OBR("Monbus Obradoiro", AVERAGE),
    GCA("Herbalife Gran Canaria", AVERAGE),
    ZAR("Casademont Zaragoza", AVERAGE),
    EST("Movistar Estudiantes", POOR),
    FUE("Montakit Fuenlabrada", POOR),
    BUR("San Pablo Burgos", POOR),
    MAN("BAXI Manresa", POOR),
    MUR("UCAM Murcia CB", POOR),
    BET("Coosur Real Betis", WORST),
    BLB("RETAbet Bilbao Basket", WORST);

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
