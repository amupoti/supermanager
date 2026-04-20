package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsDescriptionResponse {

    List<Team> userTeamList;

    @Data
    @Slf4j
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        String nameTeam;
        String idUserTeam;
        String brokerValor;
        String totalPlayerPoints;
        String license;
        String amount;

        /**
         * Captures any extra fields from the userteam/all API that are not yet mapped.
         * Candidate change-count fields: numCambios, maxCambios, numChanges, maxChanges, cambios.
         */
        private final Map<String, Object> unknownFields = new HashMap<>();

        @JsonAnySetter
        public void setUnknownField(String name, Object value) {
            unknownFields.put(name, value);
            log.debug("Unmapped field in userteam/all API response: {} = {}", name, value);
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
    }
}
