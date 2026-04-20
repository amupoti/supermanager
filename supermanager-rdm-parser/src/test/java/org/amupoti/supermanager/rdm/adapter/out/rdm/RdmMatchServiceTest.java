package org.amupoti.supermanager.rdm.adapter.out.rdm;

import org.amupoti.supermanager.rdm.adapter.out.rdm.RdmConfiguration;
import org.amupoti.supermanager.rdm.adapter.out.rdm.RdmScrapingAdapter;
import org.amupoti.supermanager.rdm.application.service.TeamScheduleService;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RdmMatchServiceTest.TestConfig.class)
public class RdmMatchServiceTest {

    @Configuration
    @Import(RdmConfiguration.class)
    static class TestConfig {
        @Bean
        public TeamScheduleService teamScheduleService(RdmScrapingAdapter adapter) {
            return new TeamScheduleService(adapter);
        }
    }

    @Autowired
    private TeamScheduleService teamScheduleService;

    @Test
    public void whenGettingMatchesForATeam_thenReturnMatches() {
        TeamSchedule teamData = teamScheduleService.getTeamSchedule(LeagueTeam.FCB, 1, 3);
        Assertions.assertThat(teamData.getMatches()).hasSize(3);
    }

    @Test
    public void whenGettingMatchesForLastMatch_thenReturnOneMatch() {
        TeamSchedule teamData = teamScheduleService.getTeamSchedule(LeagueTeam.FCB, 34, 3);
        Assertions.assertThat(teamData.getMatches()).hasSize(1);
    }
}
