package org.amupoti.supermanager.parser.rdm;

import org.amupoti.supermanager.parser.rdm.config.RdmConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by amupoti on 28/10/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmConfiguration.class)
public class RdmContentParserTest {

    @Autowired
    private RdmContentParser parser;

    @Autowired
    private RdmContentProvider provider;

    @Test
    public void whenRetrievingMatches_thenTeamPlays34Matches() {
        RdmTeam team = RdmTeam.AND;
        String teamPage = provider.getTeamPage(team);
        List<Match> teamMatches = parser.getTeamMatches(teamPage, team);

        Assertions.assertThat(teamMatches).hasSize(34);

        Set<RdmTeam> teams = teamMatches.stream().map(Match::getAgainstTeam).collect(Collectors.toSet());
        Assertions.assertThat(teams).hasSize(17);
    }

}