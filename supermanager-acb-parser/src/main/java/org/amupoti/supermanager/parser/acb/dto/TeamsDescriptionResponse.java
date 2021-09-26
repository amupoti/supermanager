package org.amupoti.supermanager.parser.acb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsDescriptionResponse {

    List<Team> userTeamList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        String nameTeam;
        String idUserTeam;
        String brokerValor;
        String totalPlayerPoints;
        String license;
        String amount;
    }
}
