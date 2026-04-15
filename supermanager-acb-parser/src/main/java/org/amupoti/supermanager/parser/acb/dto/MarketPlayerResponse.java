package org.amupoti.supermanager.parser.acb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketPlayerResponse {
    String shortName;
    String price;
    String up15;
    String keep;
    String down15;
    String nameTeam;
    String competitionAverage;
    String fisicStatus;
    int idPlayer;
    String position;
    String license;
    String nationality;
    @JsonProperty("isNational")
    boolean spanish;
    @JsonProperty("isExtracomunitario")
    boolean foreign;
}
