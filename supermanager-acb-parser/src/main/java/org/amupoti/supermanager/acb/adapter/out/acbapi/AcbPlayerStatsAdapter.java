package org.amupoti.supermanager.acb.adapter.out.acbapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.PlayerStatsResponse;
import org.amupoti.supermanager.acb.application.port.out.PlayerStatsPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Out-adapter: retrieves per-player statistics from the ACB API.
 */
@Component
@Slf4j
public class AcbPlayerStatsAdapter implements PlayerStatsPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AcbPlayerStatsAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "playerStats", key = "#idPlayer")
    public String getLastFourAverage(long idPlayer, String token) {
        try {
            log.debug("Requesting player stats for idPlayer {}", idPlayer);
            String json = restTemplate.exchange(
                    "https://supermanager.acb.com/api/basic/playerstats/1/" + idPlayer,
                    HttpMethod.GET, new HttpEntity<>(authHeader(token)), String.class).getBody();
            return computeLastFourAverage(json);
        } catch (Exception e) {
            log.warn("Failed to fetch last-4 average for player {}: {}", idPlayer, e.getMessage());
            return null;
        }
    }

    private String computeLastFourAverage(String playerStatsJson) throws Exception {
        PlayerStatsResponse stats = objectMapper.readValue(playerStatsJson, PlayerStatsResponse.class);
        if (stats.getPlayerStats() == null) return null;
        List<Double> scores = stats.getPlayerStats().stream()
                .filter(s -> s.getIdJourney() != null)
                .sorted(Comparator.comparingInt(PlayerStatsResponse.JourneyStats::getNumberJourney).reversed())
                .limit(4)
                .map(s -> {
                    double bonus  = s.getBonusVictory() != null ? s.getBonusVictory() : 0.0;
                    double points = s.getPointsJourney() != null ? s.getPointsJourney() : 0.0;
                    return bonus > 0 ? bonus : points;
                })
                .collect(Collectors.toList());
        if (scores.isEmpty()) return null;
        double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return String.format("%.1f", avg);
    }

    private MultiValueMap<String, String> authHeader(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }
}
