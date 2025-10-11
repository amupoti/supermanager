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

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmConfiguration.class)
public class RdmContentParserTest {

    @Autowired
    private RdmContentParser parser;

    @Autowired
    private RdmContentProvider provider;

    @Test
    public void whenRetrievingMatches_thenTeamPlays34Matches() {
        for (RdmTeam team : RdmTeam.values()) {
            String teamPage = provider.getTeamPage(team);
            List<Match> teamMatches = parser.getTeamMatches(teamPage, team);

            assertThat(teamMatches).hasSize(34);
            assertThat(teamMatches.stream()
                    .map(Match::getAgainstTeam)
                    .collect(Collectors.toSet()))
                    .hasSize(17);
        }
    }


    @Test
    public void whenGetMatchNumber_thenItIsANumber() {
        String page = provider.getMainPage();
        String matchNumber = parser.getMatchNumber(page);
        assertThat(Integer.parseInt(matchNumber)).isBetween(1, 35);
    }

}