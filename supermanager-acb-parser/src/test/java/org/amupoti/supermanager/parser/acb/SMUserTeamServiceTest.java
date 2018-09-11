package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.config.TestConfig;
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

/**
 * Created by Marcel on 02/01/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class SMUserTeamServiceTest {

    private static final String PASSWORD = "testsm_testsm";
    private static final String USER = "testsm_testsm";

    @Autowired
    private SMUserTeamService acbTeamsService;

    @Test
    public void getTeams() throws XPatherException {


        List<SmTeam> teams = acbTeamsService.getTeamsByCredentials(USER, PASSWORD);
        Assert.assertEquals(3, teams.size());
        Assert.assertThat(teams.get(0).getName() + teams.get(1).getName(), StringContains.containsString("El equipo 1"));
        Assert.assertThat(teams.get(0).getName() + teams.get(1).getName(), StringContains.containsString("El equipo 2"));

    }


    @Test
    public void getPlayers() throws XPatherException {


        List<SmTeam> teams = acbTeamsService.getTeamsByCredentials(USER, PASSWORD);
        Assert.assertTrue(teams.get(0).getPlayerList().size() > 0);
        teams.get(0).getPlayerList().stream().forEach(p -> System.out.println(p));
    }
}
