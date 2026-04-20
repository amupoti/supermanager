package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
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
    @JsonProperty("isBlock")
    boolean blocked;

    private final Map<String, Object> unknownFields = new HashMap<>();

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
        log.debug("Unmapped market API field: {} = {}", name, value);
    }
}
