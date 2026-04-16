package org.amupoti.supermanager.parser.acb.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps one entry from GET /api/basic/userteamplayerchange?_filters=[{idUserTeam=X}].
 * action=1 means a pending sell, action=2 means a pending buy.
 *
 * The API likely returns a status field (e.g. "status", "estado", "executed") and a journey
 * number (e.g. "journeyNumber", "numJornada"). These are captured via @JsonAnySetter so they
 * appear in logs and can be promoted to proper fields once confirmed.
 */
@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingChangeResponse {
    long idUserTeamPlayerChange;
    long idPlayer;
    int action; // 1=sell, 2=buy

    private final Map<String, Object> unknownFields = new HashMap<>();

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
        log.debug("Unmapped field in pendingChange API response: {} = {}", name, value);
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
