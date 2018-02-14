package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by amupoti on 01/10/2017.
 */
@Slf4j
public class SmContentParserTest {


    @Test
    @Ignore("Ignore during copa18")
    public void providePlayerData() throws Exception {

        SmContentParser parser = new SmContentParser();
        parser.init();

        ClassPathResource resource = new ClassPathResource("html/market.html");
        String marketHtml = FileUtils.readFileToString(resource.getFile());
        PlayerMarketData playerMarketData = parser.providePlayerData(marketHtml);
        Assert.assertEquals(194, playerMarketData.size());
    }

}