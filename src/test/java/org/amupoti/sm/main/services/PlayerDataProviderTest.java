package org.amupoti.sm.main.services;

import org.amupoti.sm.main.TestConfig;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.services.provider.PlayerDataProvider;
import org.htmlcleaner.XPatherException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

/**
 * Created by Marcel on 25/09/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class PlayerDataProviderTest {

    @Autowired
    private PlayerDataProvider playerDataProvider;
    @Test
    public void testPlayerLoad() throws IOException, XPatherException {
        List<PlayerId> playerIds = playerDataProvider.getPlayerIds();
        Assert.assertTrue(playerIds.size()==60);
        Assert.assertEquals("Granger, Jayson",playerIds.get(0).getId());
    }
}
