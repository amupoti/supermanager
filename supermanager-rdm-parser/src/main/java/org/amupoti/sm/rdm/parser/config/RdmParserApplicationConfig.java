package org.amupoti.sm.rdm.parser.config;


import org.amupoti.sm.rdm.parser.provider.HTMLProviderServiceV2;
import org.amupoti.sm.rdm.parser.services.scraper.player.PlayerDataService;
import org.amupoti.sm.rdm.parser.services.scraper.player.RdmPlayerDataScraperV2;
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
    public HTMLProviderServiceV2 getHtmlProviderService() {
        return new HTMLProviderServiceV2();
    }

    @Bean
    public PlayerDataService getPlayerDataService() {
        return new RdmPlayerDataScraperV2();
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

