package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.config.TestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Marcel on 25/09/2015.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class LoginTest {

    Log log = LogFactory.getLog(LoginTest.class);

    @Autowired
    private SmContentProvider smContentProvider;

    @Test
    public void testLogin() {
        String token = smContentProvider.authenticateUser("testsm_testsm@mailinator.com", "testsm_testsm@mailinator.comT1").getJwt();
        String teamsPage = smContentProvider.getTeamsPage("testsm_testsm@mailinator.com", token);
        assertTrue(teamsPage.contains("SuperManager Clásico"));
    }
}
