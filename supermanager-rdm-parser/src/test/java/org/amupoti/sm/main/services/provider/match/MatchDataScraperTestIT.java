package org.amupoti.sm.main.services.provider.match;

import org.amupoti.sm.main.TestConfig;
import org.amupoti.sm.main.config.SMConstants;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.htmlcleaner.XPatherException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.stream.StreamSupport;

import static org.amupoti.sm.main.config.SMConstants.NOT_PLAYING_MATCH;

/**
 * Created by Marcel on 28/09/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class MatchDataScraperTestIT {

    @Autowired
    HTMLProviderService htmlProviderService;
    private MatchDataScraper scraper;

    @Before
    public void init(){
        scraper = new MatchDataScraper();
        scraper.setHtmlProviderService(htmlProviderService);
        scraper.init();
    }

    @Test
    public void testCalendarHasAllMatches() throws IOException, XPatherException {

        Iterable<MatchEntity> matches = scraper.getTeamMatches("FCB");
        Assert.assertEquals(SMConstants.MAX_GAMES,matches.spliterator().getExactSizeIfKnown());
    }

    @Test
    public void testTwoMatchesNotPlaying() throws IOException, XPatherException {

        Iterable<MatchEntity> matches = scraper.getTeamMatches("FCB");
        long notPlayingMatches = StreamSupport.stream(matches.spliterator(),false).filter(m -> m.getLocal().equals(NOT_PLAYING_MATCH)||m.getVisitor().equals(NOT_PLAYING_MATCH)).count();
        Assert.assertEquals(2,notPlayingMatches);
    }

}