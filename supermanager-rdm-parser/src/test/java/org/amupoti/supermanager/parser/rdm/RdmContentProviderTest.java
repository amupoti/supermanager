package org.amupoti.supermanager.parser.rdm;

import org.amupoti.supermanager.parser.rdm.config.RdmConfiguration;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by amupoti on 27/10/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmConfiguration.class)
public class RdmContentProviderTest {

    @Autowired
    private RdmContentProvider rdmContentProvider;

    @Test
    public void whenGettingTeams_thenAre18Teams() {
        Assertions.assertThat(LeagueTeam.values().length).isEqualTo(18);
    }

    @Test
    public void whenRetrievingUrl_thenContainsTeamName() {

        String teamPage = rdmContentProvider.getTeamPage(LeagueTeam.FCB);
        Assertions.assertThat(teamPage).containsIgnoringCase("BARÇA");
    }
}