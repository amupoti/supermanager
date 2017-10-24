package org.amupoti.sm.rdm.parser.config;


import org.amupoti.sm.rdm.parser.provider.HTMLProviderService;
import org.amupoti.sm.rdm.parser.services.scraper.player.PlayerDataService;
import org.amupoti.sm.rdm.parser.services.scraper.player.RDMPlayerDataScraper;
import org.amupoti.sm.rdm.parser.services.scraper.team.RDMTeamDataService;
import org.amupoti.sm.rdm.parser.services.scraper.team.TeamDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Marcel on 06/08/2015.
 */
@Configuration
@PropertySource("classpath:urls.properties")
public class RdmParserApplicationConfig {

    @Bean
    public HTMLProviderService getHtmlProviderService() {
        return new HTMLProviderService();
    }

    @Bean
    public PlayerDataService getPlayerDataService() {
        return new RDMPlayerDataScraper();
        //return new MockPlayerDataService();
    }

    @Bean
    public TeamDataService getTeamDataService() {
        return new RDMTeamDataService();
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}

