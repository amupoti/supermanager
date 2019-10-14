package org.amupoti.sm.main.service.privateleague;

import org.amupoti.sm.main.Application;
import org.amupoti.supermanager.parser.acb.privateleague.PrivateLeagueCategory;
import org.assertj.core.api.Assertions;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by amupoti on 05/10/2019.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class, TestConfig.class, H2TestJPAConfig.class})
public class PrivateLeagueStatsServiceTest {

    private static final String PASSWORD = "testsm_testsm";
    private static final String USER = "testsm_testsm";


    @Autowired
    private PrivateLeagueStatsService privateLeagueStatsService;

    @Test
    public void whenStoringPlayersForTwoMatchesWithSameDataThenAllDeltasReturnZero() throws XPatherException {
        int matchNumber = 1;
        String stat = PrivateLeagueCategory.ASSISTS.toString();
        privateLeagueStatsService.storeLeagueStatsForMatch(USER, PASSWORD, matchNumber);
        privateLeagueStatsService.storeLeagueStatsForMatch(USER, PASSWORD, matchNumber + 1);
        List<PlayerLeagueStateEntity> playerLeagueStats = privateLeagueStatsService.getLeagueDataByStat(stat, matchNumber + 1);
        Assertions.assertThat(playerLeagueStats.stream().allMatch(e -> e.getPoints() == 0)).isTrue();

    }

    @Test
    public void whenStoringPlayersForFirstMatchThenDataIsPopulated() throws XPatherException {
        String stat = PrivateLeagueCategory.ASSISTS.toString();
        privateLeagueStatsService.storeLeagueStatsForMatch(USER, PASSWORD, 1);

        List<PlayerLeagueStateEntity> playerLeagueStats = privateLeagueStatsService.getLeagueDataByStat(stat, 1);
        Assertions.assertThat(playerLeagueStats.stream().allMatch(e -> e.getPoints() != 0)).isTrue();

    }
}