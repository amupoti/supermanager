package org.amupoti.supermanager.acb.adapter.out.acbapi;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.application.port.out.PlayerStatsPort;
import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.springframework.stereotype.Component;

/**
 * Out-adapter: retrieves per-player statistics from the ACB API.
 */
@Component
@Slf4j
public class AcbPlayerStatsAdapter implements PlayerStatsPort {

    private final SmContentProvider smContentProvider;
    private final SmContentParser smContentParser;

    public AcbPlayerStatsAdapter(SmContentProvider smContentProvider, SmContentParser smContentParser) {
        this.smContentProvider = smContentProvider;
        this.smContentParser = smContentParser;
    }

    @Override
    public String getLastFourAverage(long idPlayer, String token) {
        try {
            String json = smContentProvider.getPlayerStats(idPlayer, token);
            return smContentParser.computeLastFourAverage(json);
        } catch (Exception e) {
            log.warn("Failed to fetch last-4 average for player {}: {}", idPlayer, e.getMessage());
            return null;
        }
    }
}
