package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStatsResponse {

    List<JourneyStats> playerStats;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JourneyStats {
        /** Present only when the player had a completed match in this journey. */
        Integer idJourney;
        int numberJourney;
        /** Raw valuation (without win bonus). Null when player didn't play. */
        Double pointsJourney;
        /**
         * Total SuperManager score including the 20% win bonus, or 0.0 when the team
         * didn't win (or player's valuation was ≤ 0). Null when player didn't play.
         */
        Double bonusVictory;
    }
}
