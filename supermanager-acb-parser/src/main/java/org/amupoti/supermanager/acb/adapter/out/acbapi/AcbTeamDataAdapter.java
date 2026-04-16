package org.amupoti.supermanager.acb.adapter.out.acbapi;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.application.port.out.MarketDataPort;
import org.amupoti.supermanager.acb.application.port.out.TeamDataPort;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Out-adapter: fetches and parses team roster data from the ACB API.
 */
@Component
@Slf4j
public class AcbTeamDataAdapter implements TeamDataPort {

    private final SmContentProvider smContentProvider;
    private final SmContentParser smContentParser;

    public AcbTeamDataAdapter(SmContentProvider smContentProvider, SmContentParser smContentParser) {
        this.smContentProvider = smContentProvider;
        this.smContentParser = smContentParser;
    }

    @Override
    public List<Team> getTeams(String user, String token) throws IOException {
        return smContentParser.getTeams(smContentProvider.getTeamsPage(user, token));
    }

    @Override
    public void populateTeam(Team team, MarketData marketData, String token) throws IOException {
        smContentParser.populateTeam(smContentProvider.getTeamPage(team, token), team, marketData);
    }

    @Override
    public void mergePlayerChangeIds(Team team, String token) {
        try {
            String json = smContentProvider.getTeamPlayerDetails(team.getTeamId(), token);
            smContentParser.mergePlayerChangeIds(team, json);
        } catch (IOException e) {
            log.warn("Failed to merge player change IDs for team {}: {}", team.getName(), e.getMessage());
        }
    }
}
