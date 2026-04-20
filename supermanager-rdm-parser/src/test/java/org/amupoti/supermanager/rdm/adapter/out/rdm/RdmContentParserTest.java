package org.amupoti.supermanager.rdm.adapter.out.rdm;

import org.amupoti.supermanager.rdm.adapter.out.rdm.RdmConfiguration;
import org.amupoti.supermanager.rdm.adapter.out.rdm.RdmScrapingAdapter;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmConfiguration.class)
public class RdmContentParserTest {

    @Autowired
    private RdmScrapingAdapter rdmScrapingAdapter;

    @Test
    public void whenRetrievingMatches_thenTeamPlays34Matches() {
        for (LeagueTeam team : LeagueTeam.values()) {
            List<Match> teamMatches = rdmScrapingAdapter.scrapeTeamMatches(team);
            assertThat(teamMatches).hasSize(34);
            assertThat(teamMatches.stream()
                    .map(Match::getAgainstTeam)
                    .collect(Collectors.toSet()))
                    .hasSize(17);
        }
    }

    @Test
    public void whenGetMatchNumber_thenItIsANumber() {
        int matchNumber = rdmScrapingAdapter.scrapeCurrentMatchNumber();
        assertThat(matchNumber).isBetween(1, 35);
    }
}
