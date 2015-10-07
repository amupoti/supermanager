package org.amupoti.sm.main.services.provider.team;

import lombok.Getter;

import java.util.HashMap;

/**
 * Created by Marcel on 05/10/2015.
 */
public class TeamConstants {

    @Getter
    private static String[] teamIds={"AND","BLB","CAI","CAN","EST","FCB","FUE","GBC","GCA","JOV","LAB","MAN","MUR","OBR","RMA","SEV","UNI","VBC"};

    @Getter
    private static HashMap<String,Integer> teamBoosts = new HashMap<>();
    private static final Integer TOP = 1;
    private static final Integer HIGH = 5;
    private static final Integer LOW = 20;
    private static final Integer MED = 10;

    static{
        teamBoosts.put("AND",MED);
        teamBoosts.put("BLB",HIGH);
        teamBoosts.put("CAI",MED);
        teamBoosts.put("CAN",MED);
        teamBoosts.put("EST",LOW);
        teamBoosts.put("FCB",TOP);
        teamBoosts.put("FUE",LOW);
        teamBoosts.put("GBC",LOW);
        teamBoosts.put("GCA",HIGH);
        teamBoosts.put("JOV",MED);
        teamBoosts.put("LAB",HIGH);
        teamBoosts.put("MAN",LOW);
        teamBoosts.put("MUR",LOW);
        teamBoosts.put("OBR",LOW);
        teamBoosts.put("RMA",TOP);
        teamBoosts.put("SEV",LOW);
        teamBoosts.put("UNI",TOP);
        teamBoosts.put("VBC",TOP);

    }

}
