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

    AND("MoraBanc Andorra", AVERAGE),
    BAS("KIROLBET Baskonia", TOP),
    BRE("Cafés Candelas Breogán", WORST),
    BUR("San Pablo Burgos", POOR),
    CAN("Iberostar Tenerife", GOOD),
    EST("Movistar Estudiantes", POOR),
    FCB("Barça Lassa", TOP),
    FUE("Montakit Fuenlabrada", POOR),
    GBC("Delteco GBC", POOR),
    GCA("Herbalife Gran Canaria", POOR),
    JOV("Divina Seguros Joventut", GOOD),
    MAN("BAXI Manresa", POOR),
    MUR("UCAM Murcia CB", POOR),
    OBR("Monbus Obradoiro", AVERAGE),
    RMA("Real Madrid", TOP),
    UNI("Unicaja", AVERAGE),
    VBC("Valencia Basket Club", GOOD),
    ZAR("Tecnyconta Zaragoza", POOR);

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
