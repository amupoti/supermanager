package org.amupoti.supermanager.parser.rdm;

import lombok.Getter;

import static org.amupoti.supermanager.parser.rdm.RdmTeam.Quality.*;

/**
 * Created by amupoti on 27/10/2018.
 */
@Getter
public enum RdmTeam {

    AND("Andorra", AVERAGE),
    BAS("Baskonia", TOP),
    BRE("Breogan", POOR),
    BUR("Burgos", POOR),
    CAN("Iberostar Tenerife", AVERAGE),
    EST("Estudiantes", AVERAGE),
    FCB("Barça", TOP),
    FUE("Fuenlabrada", POOR),
    GBC("Delteco", WORST),
    GCA("Grancanaria", GOOD),
    JOV("Joventut", AVERAGE),
    MAN("Manresa", AVERAGE),
    MUR("UCAM Murcia", POOR),
    OBR("Obradorio", POOR),
    RMA("Real Madrid", TOP),
    UNI("Unicaja", TOP),
    VBC("Valencia", GOOD),
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
}
