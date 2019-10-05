package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.config.TestConfig;
import org.amupoti.supermanager.parser.acb.privateleague.PrivateLeagueCategory;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.hamcrest.core.StringContains;
import org.htmlcleaner.XPatherException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.amupoti.supermanager.parser.acb.privateleague.PrivateLeagueCategory.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Marcel on 02/01/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class SMUserTeamServiceTest {

    private static final String PASSWORD = "testsm_testsm";
    private static final String USER = "testsm_testsm";
    public static final String EL_EQUIPO_2 = "El equipo 2";
    public static final String EL_EQUIPO_1 = "El equipo 1";

    @Autowired
    private SMUserTeamService acbTeamsService;

    @Test
    public void getTeams() throws XPatherException {


        List<SmTeam> teams = acbTeamsService.getTeamsByCredentials(USER, PASSWORD);
        Assert.assertEquals(2, teams.size());
        Assert.assertThat(teams.get(0).getName() + teams.get(1).getName(), StringContains.containsString(EL_EQUIPO_1));
        Assert.assertThat(teams.get(0).getName() + teams.get(1).getName(), StringContains.containsString(EL_EQUIPO_2));

    }


    @Test
    public void getPlayers() throws XPatherException {


        List<SmTeam> teams = acbTeamsService.getTeamsByCredentials(USER, PASSWORD);
        Assert.assertTrue(teams.get(0).getPlayerList().size() > 0);
        teams.get(0).getPlayerList().stream().forEach(p -> System.out.println(p));
    }

    @Test
    public void givenPrivateLeagueWhenGetDetailsThenGetAllStats() throws XPatherException {
        Map<PrivateLeagueCategory, Map<String, Integer>> stats = acbTeamsService.getPrivateLeagueData(USER, PASSWORD);

        //Then there are 3 categories
        assertThat(stats).hasSize(3);

        //Then there are 2 teams in private league
        assertThat(stats.get(ASSISTS).size()).isEqualTo(2);
        assertThat(stats.get(REBOUNDS).size()).isEqualTo(2);
        assertThat(stats.get(THREE_POINTERS).size()).isEqualTo(2);

        //Then names are correct
        assertThat(stats.get(THREE_POINTERS).keySet()).containsExactlyInAnyOrder(EL_EQUIPO_1, EL_EQUIPO_2);
    }
}
