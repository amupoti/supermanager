package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.config.TestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Marcel on 25/09/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)

public class LoginTest {

    Log log = LogFactory.getLog(LoginTest.class);

    @Autowired
    private SmContentProvider smContentProvider;
    @Test
    public void testLogin() throws IOException, URISyntaxException {


        smContentProvider.authenticateUser("testsm_testsm", "testsm_testsm");
        String teamsPage = smContentProvider.getTeamsPage();
        Assert.assertTrue(teamsPage.contains("crear equipo"));
    }


}

