package org.amupoti.sm.main.services.provider;

import org.amupoti.sm.main.TestConfig;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.services.PlayerPosition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Marcel on 21/10/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class HTMLProviderServiceITTest {

    @Autowired
    HTMLProviderService htmlProviderService;

    @Test
    public void testParsingPlayersUTF8() throws IOException {
        String html = htmlProviderService.getAllPlayersURL(PlayerPosition.BASE);
        Assert.assertTrue(html.contains("Rodríguez, Sergio"));
    }

    @Test
    public void testParsingPlayerPage() throws IOException, URISyntaxException {
        String html = htmlProviderService.getPlayerURL(new PlayerId("Rodríguez, Sergio"));
        Assert.assertTrue(html.contains("(RMA)"));
    }

    @Test
    public void testParsingPlayerMeans() throws IOException, URISyntaxException {
        String html = htmlProviderService.getPlayerURL(new PlayerId("Rodríguez, Sergio"));
        Assert.assertTrue(html.contains("(RMA)"));
    }
}
