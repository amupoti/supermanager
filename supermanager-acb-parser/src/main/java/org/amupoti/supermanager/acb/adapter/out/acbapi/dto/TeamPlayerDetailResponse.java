package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps a player entry from GET /api/basic/userteamplayer/{teamId}.
 * This endpoint includes idUserTeamPlayerChange and statusTeamSquad, which the journeys endpoint does not provide.
 *
 * statusTeamSquad values:
 *   "normal" — original player, no change made
 *   "new"    — player acquired via a change (counts toward changesUsed)
 *   "empty"  — sold player slot
 */
@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamPlayerDetailResponse {
    String shortName;
    long idUserTeamPlayerChange;
    long idPlayer;
    String statusTeamSquad;

    private final Map<String, Object> unknownFields = new HashMap<>();

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
        log.debug("Unmapped field in userteamplayer API response: {} = {}", name, value);
    }
}
