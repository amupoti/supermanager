package org.amupoti.supermanager.parser.rdm;

import org.amupoti.supermanager.parser.rdm.config.RdmConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by amupoti on 06/11/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmConfiguration.class)
public class RdmMatchServiceTest {

    @Autowired
    private RdmMatchService matchService;

    @Test
    public void whenGettingMatchesForATeam_thenReturnMatches() {
        RdmTeamData teamData = matchService.getTeamDataFromMatchNumber(RdmTeam.AND, 1, 3);
        Assertions.assertThat(teamData.getMatches()).hasSize(3);
    }

    @Test
    public void whenGettingMatchesForLastMatch_thenReturnOneMatch() {
        RdmTeamData teamData = matchService.getTeamDataFromMatchNumber(RdmTeam.AND, 34, 3);
        Assertions.assertThat(teamData.getMatches()).hasSize(1);
    }
}