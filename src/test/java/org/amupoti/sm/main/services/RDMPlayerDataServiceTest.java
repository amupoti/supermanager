package org.amupoti.sm.main.services;

import mockit.Injectable;
import mockit.Tested;
import org.amupoti.sm.main.TestConfig;
import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.amupoti.sm.main.services.provider.player.RDMPlayerDataService;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Marcel on 25/09/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)

public class RDMPlayerDataServiceTest {

    @Tested
    private RDMPlayerDataService rdmPlayerDataService;

    @Injectable
    private HTMLProviderService htmlProviderService;

    @Injectable
    private TeamService teamService;

    @Injectable
    private PlayerRepository playerRepository;

    @Test
    public void testPlayerLoad() throws IOException, XPatherException {

        //Given
        PlayerId playerId = new PlayerId("Tomic, Ante");
        Set<PlayerId> playerIds = new LinkedHashSet<>();
        playerIds.add(playerId);
        //When


       // playerIds = rdmPlayerDataService.getPlayersData(playerIds);
        //Then


       // Assert.assertTrue(playerIds.size()==60);
       // Assert.assertEquals("Granger, Jayson",playerIds.iterator().next());
    }
}
