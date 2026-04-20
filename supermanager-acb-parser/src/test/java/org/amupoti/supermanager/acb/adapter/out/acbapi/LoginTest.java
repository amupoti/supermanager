package org.amupoti.supermanager.acb.adapter.out.acbapi;

import org.amupoti.supermanager.acb.adapter.out.acbapi.AcbAuthenticationAdapter;
import org.amupoti.supermanager.acb.config.TestConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class LoginTest {

    Log log = LogFactory.getLog(LoginTest.class);

    @Autowired
    private AcbAuthenticationAdapter acbAuthenticationAdapter;

    @Test
    public void testLogin() {
        String token = acbAuthenticationAdapter.authenticate("testsm_testsm@mailinator.com", "testsm_testsm@mailinator.comT1");
        Assert.assertNotNull("JWT token must not be null", token);
    }
}

