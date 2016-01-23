package org.amupoti.sm.main.config;

import org.amupoti.sm.main.services.provider.team.TeamDataService;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.amupoti.sm.main.services.provider.player.PlayerDataService;
import org.amupoti.sm.main.services.provider.player.RDMPlayerDataService;
import org.amupoti.sm.main.services.provider.team.RDMTeamDataService;
import org.amupoti.supermanager.parser.acb.ACBTeamService;
import org.amupoti.supermanager.parser.acb.ACBTeamServiceDefault;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Marcel on 06/08/2015.
 */
@Configuration
@PropertySource("classpath:urls.properties")
public class ApplicationConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("playerData");
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    public HTMLProviderService getHtmlProviderService(){
        return new HTMLProviderService();
    }

    @Bean
    public PlayerDataService getPlayerDataService(){
        return new RDMPlayerDataService();
        //return new MockPlayerDataService();
    }

    @Bean
    public TeamDataService getTeamDataService(){
        return new RDMTeamDataService();
    }

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ACBTeamService getAcbTeamService(){
        return new ACBTeamServiceDefault();
    }
}

