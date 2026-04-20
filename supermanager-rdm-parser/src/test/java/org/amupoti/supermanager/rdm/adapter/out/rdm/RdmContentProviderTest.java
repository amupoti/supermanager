package org.amupoti.supermanager.rdm.adapter.out.rdm;

import org.amupoti.supermanager.rdm.adapter.out.rdm.RdmConfiguration;
import org.amupoti.supermanager.rdm.adapter.out.rdm.RdmScrapingAdapter;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmConfiguration.class)
public class RdmContentProviderTest {

    @Autowired
    private RdmScrapingAdapter rdmScrapingAdapter;

    @Test
    public void whenGettingTeams_thenAre18Teams() {
        Assertions.assertThat(LeagueTeam.values().length).isEqualTo(18);
    }

    @Test
    public void whenRetrievingUrl_thenContainsTeamName() {
        List<Match> matches = rdmScrapingAdapter.scrapeTeamMatches(LeagueTeam.FCB);
        Assertions.assertThat(matches).isNotEmpty();
    }
}
