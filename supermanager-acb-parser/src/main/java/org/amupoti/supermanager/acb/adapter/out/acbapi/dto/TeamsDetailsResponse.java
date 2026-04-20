package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsDetailsResponse {

    List<Player> playerList;
    int number;
    TotalStats totalStats;

    /**
     * Captures any fields returned by the journeys API that are not yet explicitly mapped.
     * Logged at INFO so we can identify candidate fields (e.g. numCambios, maxCambios, numChanges, maxChanges).
     */
    private final Map<String, Object> unknownFields = new HashMap<>();

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
        log.debug("Unmapped field in journeys API response: {} = {}", name, value);
    }

    /** Returns the integer value of the first matching named unknown field, or -1 if absent/unparseable. */
    public int getUnknownInt(String... names) {
        for (String name : names) {
            Object v = unknownFields.get(name);
            if (v != null) {
                try { return Integer.parseInt(v.toString()); } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Player {
        String fullName;
        String shortName;
        String position;
        String journeyPoints;
        int number;
        long idUserTeamPlayerChange;
        long idPlayer;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TotalStats {
        String totalPoints;
    }
}
