package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.beans.ACBSupermanagerTeam;
import org.amupoti.supermanager.parser.acb.config.TestConfig;
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
public class ACBTeamServiceTest {

    private static final String PASSWORD = "testsm_testsm";
    private static final String USER = "testsm_testsm";

    @Autowired
    private ACBTeamService acbTeamsService;

    @Test
    public void getTeams() throws XPatherException {


        List<ACBSupermanagerTeam> teams = acbTeamsService.getTeamsByCredentials(USER, PASSWORD);
        Assert.assertEquals(2,teams.size());
        Assert.assertEquals("El Equipo 1",teams.get(0).getName());
        Assert.assertEquals("El Equipo 2",teams.get(0).getName());


    }

    @Test
    public void getPlayers() throws XPatherException {


        List<ACBSupermanagerTeam> teams = acbTeamsService.getTeamsByCredentials(USER, PASSWORD);
        Assert.assertEquals(11, teams.get(0).getPlayers().size());

    }
}
