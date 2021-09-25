package org.amupoti.supermanager.parser.acb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsDetailsResponse {

    List<Player> playerList;
    int number;
    TotalStats totalStats;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Player {
        String fullName;
        String shortName;
        String position;
        String journeyPoints;
        int number;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TotalStats {
        String totalPoints;
    }
}
