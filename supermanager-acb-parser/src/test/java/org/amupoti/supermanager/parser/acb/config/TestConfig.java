package org.amupoti.supermanager.parser.acb.config;

import org.amupoti.supermanager.parser.acb.SMUserTeamService;
import org.amupoti.supermanager.parser.acb.SMUserTeamServiceImpl;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Marcel on 25/09/2015.
 */
@Configuration
public class TestConfig {


    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public SMUserTeamService getAcbTeamsService() {
        return new SMUserTeamServiceImpl();
    }


    @Bean
    public SmContentProvider getSmContentProvider() {
        return new SmContentProvider(SmContentProvider.SUPERMANAGER_HOME_URL);
    }
}
