package org.amupoti.sm.main.services.it.rdm.site;

import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.MatchControlService;
import org.amupoti.sm.main.services.repository.TeamService;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.amupoti.sm.main.services.provider.player.RDMPlayerDataScraper;
import org.apache.commons.io.IOUtils;
import org.htmlcleaner.XPatherException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.amupoti.sm.main.services.provider.team.RDMTeamDataService.INVALID_VALUE;

/**
 * Created by Marcel on 07/07/2016.
 */
public class PlayerLoadITTest {

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
    @Ignore
    public void testPlayerLoadFromWeb() throws IOException, XPatherException, URISyntaxException {

        //Given
        PlayerId playerId = new PlayerId("Tomic, Ante");
        Set<PlayerId> playerIds = new LinkedHashSet<>();
        playerIds.add(playerId);
        String html = IOUtils.toString(new URL("http://www.rincondelmanager.com/smgr/stats.php?nombre=Tomic,%20Ante").openStream());
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
        int invalidValue = Integer.parseInt(INVALID_VALUE);
        PlayerEntity playerEntity = playersData.get(0);
        Assert.assertEquals(1, playersData.size());
        Assert.assertEquals("Tomic, Ante", playerEntity.getPlayerId().getId());
        Assert.assertTrue(playerEntity.getBroker()>invalidValue);
        Assert.assertTrue(playerEntity.getLocalMean()>invalidValue);
        Assert.assertTrue(playerEntity.getVisitorMean()>invalidValue);
        Assert.assertTrue(playerEntity.getKeepBroker()>invalidValue);


    }
}
