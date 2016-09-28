package org.amupoti.sm.main.services;

import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import org.amupoti.sm.main.TestConfig;
import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.amupoti.sm.main.services.provider.player.RDMPlayerDataScraper;
import org.amupoti.sm.main.services.repository.TeamService;
import org.apache.commons.io.FileUtils;
import org.htmlcleaner.XPatherException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Marcel on 25/09/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)

public class RDMPlayerDataScraperTest {

    @Tested
    private RDMPlayerDataScraper rdmPlayerDataScraper;

    @Injectable
    private HTMLProviderService htmlProviderService;

    @Injectable
    private TeamService teamService;

    @Injectable
    private PlayerRepository playerRepository;

    @Injectable
    private MatchControlService matchControlService;

    @Test
    public void testPlayerLoadWithLocalFile() throws IOException, XPatherException, URISyntaxException {

        //Given
        PlayerId playerId = new PlayerId("Tomic, Ante");
        Set<PlayerId> playerIds = new LinkedHashSet<>();
        playerIds.add(playerId);
        String html = FileUtils.readFileToString(new ClassPathResource("parsing/players/tomic.html").getFile());
        TeamEntity teamEntity = new TeamEntity();
        teamEntity.setName("FCB");
        //When

        new NonStrictExpectations(){{
            htmlProviderService.getPlayerURL(playerId);
            returns(html);
            teamService.getTeam(anyString);
            returns(teamEntity);
            matchControlService.getMatchNumber();
            returns (34);
        }
        };
        List<PlayerEntity> playersData = rdmPlayerDataScraper.getPlayersData(playerIds);


        //Then

        PlayerEntity playerEntity = playersData.get(0);
        Assert.assertEquals(1, playersData.size());
        Assert.assertEquals("Tomic, Ante", playerEntity.getPlayerId().getId());
        Assert.assertEquals(1417500, playerEntity.getBroker(),0.1);
        Assert.assertEquals(20.25, playerEntity.getKeepBroker(),0.1);
        //TODO: implement local and visitor mean for player
        Assert.assertEquals(21.92, playerEntity.getLocalMean(),0.1);
        Assert.assertEquals(18.78, playerEntity.getVisitorMean(),0.1);

    }
}
