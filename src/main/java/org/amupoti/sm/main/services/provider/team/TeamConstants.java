package org.amupoti.sm.main.services.provider.team;

import lombok.Getter;

import java.util.HashMap;

/**
 * Created by Marcel on 05/10/2015.
 */
public class TeamConstants {

    public static final int CURRENT_MATCH_NUMBER = 2;
    @Getter
    private static String[] teamIds={"AND","BLB","CAI","CAN","EST","FCB","FUE","GBC","GCA","JOV","LAB","MAN","MUR","OBR","RMA","SEV","UNI","VBC"};

    @Getter
    private static HashMap<String,Integer> teamBoosts = new HashMap<>();
    private static final Integer TOP = 1;
    private static final Integer HIGH = 5;
    private static final Integer BOTTOM = 30;
    private static final Integer LOW = 20;
    private static final Integer MED = 10;

    public static final Float LOCAL_BOOST = 25.0f;

    static{
        teamBoosts.put("FCB",TOP);
        teamBoosts.put("RMA",TOP);
        teamBoosts.put("UNI",TOP);
        teamBoosts.put("VBC",TOP);
        teamBoosts.put("BLB",HIGH);
        teamBoosts.put("GCA",HIGH);
        teamBoosts.put("LAB",HIGH);
        teamBoosts.put("JOV",MED);
        teamBoosts.put("AND",MED);
        teamBoosts.put("CAI",MED);
        teamBoosts.put("CAN",MED);
        teamBoosts.put("OBR",MED);
        teamBoosts.put("GBC",LOW);
        teamBoosts.put("FUE",LOW);
        teamBoosts.put("MUR",LOW);
        teamBoosts.put("EST",BOTTOM);
        teamBoosts.put("MAN",BOTTOM);
        teamBoosts.put("SEV",BOTTOM);

    }

}
